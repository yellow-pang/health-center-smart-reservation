#!/usr/bin/env python3
"""Deploy two immutable app images; never own or recreate the production DB.

Requires RELEASE_SHA, BACKEND_IMAGE, FRONTEND_IMAGE. First adoption requires
BOOTSTRAP_EXISTING_DB=1 and always takes and validates a PostgreSQL dump. Runtime
secrets live in DEPLOY_ROOT/shared/production.env; they are never shell-sourced
or printed. A failed rollout restores prior app images, not database contents.
"""

import contextlib
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import signal
import stat
import subprocess
import sys
import time
import urllib.request
import uuid


PROJECT = "health-center"
NETWORK = "health-center_health-center-network"
DB_VOLUME = "health-center_health-center-postgres-data"
DB_CONTAINER = "health-center-postgres"
SERVICES = ("backend", "frontend")
DIGEST_RE = re.compile(r"ghcr\.io/[a-z0-9._/-]+@sha256:[a-f0-9]{64}\Z")
SHA_RE = re.compile(r"[a-f0-9]{40}\Z")
SAFE_HOST_ENV = {
    "PATH", "HOME", "USER", "TMPDIR", "LANG", "LC_ALL", "DOCKER_HOST",
    "DOCKER_CONTEXT", "DOCKER_CONFIG", "DOCKER_TLS_VERIFY", "DOCKER_CERT_PATH",
    "SSL_CERT_FILE", "SSL_CERT_DIR",
}


class DeployError(RuntimeError):
    pass


def log(message):
    print(message, flush=True)


def write_private(path, content):
    with open(path, "w", encoding="utf-8") as handle:
        os.chmod(path, 0o600)
        handle.write(content)


def write_json(path, value):
    write_private(path, json.dumps(value, indent=2) + "\n")


def secure_file(path):
    if path.is_symlink() or not path.is_file():
        raise DeployError("Production environment must be a regular, non-symlink file")
    if stat.S_IMODE(path.stat().st_mode) & 0o077:
        raise DeployError("Production environment must have mode 600 (no group/other access)")


class Deployer:
    def __init__(self, environment=None):
        source = dict(os.environ if environment is None else environment)
        self.sha = source.get("RELEASE_SHA", "")
        self.images = {service: source.get(service.upper() + "_IMAGE", "") for service in SERVICES}
        if not SHA_RE.fullmatch(self.sha):
            raise DeployError("RELEASE_SHA must be a full lowercase 40-character commit SHA")
        if not all(DIGEST_RE.fullmatch(image) for image in self.images.values()):
            raise DeployError("Both app images must be GHCR references pinned by sha256 digest")
        self.root = Path(source.get("DEPLOY_ROOT", "/Users/tro/services/health-center"))
        if not self.root.is_absolute() or self.root == Path("/") or self.root.is_symlink():
            raise DeployError("DEPLOY_ROOT must be a dedicated absolute directory, not a symlink")
        self.bootstrap = source.get("BOOTSTRAP_EXISTING_DB", "0") == "1"
        try:
            self.timeout = int(source.get("HEALTH_TIMEOUT_SECONDS", "180"))
        except ValueError as error:
            raise DeployError("HEALTH_TIMEOUT_SECONDS must be an integer") from error
        if not 1 <= self.timeout <= 900:
            raise DeployError("HEALTH_TIMEOUT_SECONDS must be between 1 and 900")
        self.env = {key: value for key, value in source.items() if key in SAFE_HOST_ENV}
        self.env["COMPOSE_IGNORE_ORPHANS"] = "true"
        self.source_compose = Path(__file__).resolve().parents[2] / "docker-compose.deploy.yml"
        self.release = self.root / "releases" / self.sha
        self.runtime_env = self.root / "shared" / "production.env"
        self.adoption_marker = self.root / "shared" / "allow-initial-adoption"
        self.current = self.root / "current"
        self.attempt = None
        self.expected_ids = {}
        self.previous_ids = {}
        self.previous_environment = {}
        self.db_name = None

    def run_command(self, args, purpose, **kwargs):
        # Capture output rather than leak resolved Compose environment on errors.
        options = {"env": self.env, "stdout": subprocess.PIPE, "stderr": subprocess.PIPE, "timeout": 600}
        options.update(kwargs)
        try:
            result = subprocess.run(args, **options)
        except subprocess.TimeoutExpired as error:
            raise DeployError(f"{purpose} exceeded its time limit; command output suppressed") from error
        if result.returncode:
            raise DeployError(f"{purpose} failed (exit {result.returncode}); command output suppressed")
        return result.stdout or b""

    def docker_json(self, args, purpose):
        try:
            return json.loads(self.run_command(["docker", *args], purpose))
        except (ValueError, TypeError) as error:
            raise DeployError(f"{purpose} returned invalid JSON") from error

    def inspect(self, container):
        return self.docker_json(["inspect", container], "Inspect required container")[0]

    def compose(self, directory, *args):
        return self.run_command([
            "docker", "compose", "--project-name", PROJECT,
            "--env-file", str(directory / "runtime.env"),
            "--env-file", str(directory / "images.env"),
            "--file", str(directory / "compose.yml"), *args,
        ], "Compose " + args[0])

    @contextlib.contextmanager
    def lock(self):
        self.root.mkdir(parents=True, exist_ok=True, mode=0o700)
        lock = self.root / ".deploy-lock"
        try:
            lock.mkdir(mode=0o700)
        except FileExistsError as error:
            raise DeployError("Another deployment owns .deploy-lock; investigate before removing a stale lock") from error
        try:
            write_private(lock / "owner", f"pid={os.getpid()}\nsha={self.sha}\n")
            yield
        finally:
            (lock / "owner").unlink(missing_ok=True)
            lock.rmdir()

    def preflight(self, check_adoption=True):
        secure_file(self.runtime_env)
        managed_current = self.current.exists() or self.current.is_symlink()
        if not managed_current and check_adoption and not self.bootstrap and self.adoption_marker.exists():
            secure_file(self.adoption_marker)
            self.bootstrap = True
        self.run_command(["docker", "compose", "version"], "Docker Compose availability")
        self.run_command(["docker", "network", "inspect", NETWORK], "Existing production network")
        self.run_command(["docker", "volume", "inspect", DB_VOLUME], "Existing database volume")
        db = self.inspect(DB_CONTAINER)
        db_environment = dict(item.split("=", 1) for item in db.get("Config", {}).get("Env", []) if "=" in item)
        self.db_name = db_environment.get("POSTGRES_DB")
        if not self.db_name:
            raise DeployError("Existing PostgreSQL must identify its POSTGRES_DB for the backup")
        if db.get("State", {}).get("Health", {}).get("Status") != "healthy":
            raise DeployError("Existing PostgreSQL must be healthy before app deployment")
        if db.get("Config", {}).get("Labels", {}).get("com.docker.compose.project") != PROJECT:
            raise DeployError("PostgreSQL belongs to a different Compose project")
        if not any(mount.get("Name") == DB_VOLUME and mount.get("Destination") == "/var/lib/postgresql"
                   for mount in db.get("Mounts", [])):
            raise DeployError("PostgreSQL is not attached to the expected existing data volume")
        network = db.get("NetworkSettings", {}).get("Networks", {}).get(NETWORK)
        if not network or "postgresql" not in (network.get("Aliases") or []):
            raise DeployError("PostgreSQL must retain its postgresql alias on the existing network")
        for service in SERVICES:
            container = self.inspect(PROJECT + "-" + service)
            if container.get("Config", {}).get("Labels", {}).get("com.docker.compose.project") != PROJECT:
                raise DeployError("Existing app container belongs to a different Compose project")
            self.previous_ids[service] = container["Image"]
            self.previous_environment[service] = dict(
                item.split("=", 1) for item in container.get("Config", {}).get("Env", []) if "=" in item)
        if managed_current:
            target = self.current.resolve()
            if target.parent != (self.root / "releases").resolve() or not (target / "release.json").is_file():
                raise DeployError("Current release does not point to a valid managed release")
            if self.bootstrap and check_adoption:
                raise DeployError("BOOTSTRAP_EXISTING_DB is permitted only for the first adoption")
        elif not self.bootstrap and check_adoption:
            raise DeployError("First adoption requires BOOTSTRAP_EXISTING_DB=1 after reviewing existing-schema compatibility")

    def image_env(self, directory, images, version, baseline=False):
        write_private(directory / "images.env", "\n".join([
            f"BACKEND_IMAGE={images['backend']}", f"FRONTEND_IMAGE={images['frontend']}",
            f"RELEASE_SHA={version}", f"FLYWAY_BASELINE_ON_MIGRATE={'true' if baseline else 'false'}", "",
        ]))

    def prepare_release(self):
        # A rebuild of the same commit may legitimately change a digest. Include
        # both images and deployment config so retries never overwrite a release.
        fingerprint = hashlib.sha256(json.dumps(self.images, sort_keys=True).encode())
        fingerprint.update(self.source_compose.read_bytes())
        fingerprint.update(self.runtime_env.read_bytes())
        self.release = self.root / "releases" / (self.sha + "-" + fingerprint.hexdigest()[:12])
        if self.release.exists():
            metadata_path = self.release / "release.json"
            if not metadata_path.is_file():
                raise DeployError("Incomplete release directory exists; investigate it before retrying")
            metadata = json.loads(metadata_path.read_text())
            mismatch = (metadata.get("sha") != self.sha or metadata.get("images") != self.images
                    or (self.release / "compose.yml").read_bytes() != self.source_compose.read_bytes()
                    or (self.release / "runtime.env").read_bytes() != self.runtime_env.read_bytes())
            if mismatch:
                raise DeployError("Existing release contents differ from their fingerprint; investigate before retrying")
        if not self.release.exists():
            self.release.mkdir(parents=True, mode=0o700)
            shutil.copyfile(self.source_compose, self.release / "compose.yml")
            shutil.copyfile(self.runtime_env, self.release / "runtime.env")
            os.chmod(self.release / "runtime.env", 0o600)
            write_json(self.release / "release.json", {
                "sha": self.sha, "images": self.images, "status": "prepared",
                "created_at": datetime.datetime.now(datetime.timezone.utc).isoformat(),
            })
        self.image_env(self.release, self.images, self.sha, self.bootstrap)
        self.compose(self.release, "config", "--quiet")
        self.validate_config()
        attempt_name = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
        self.attempt = self.root / "attempts" / f"{attempt_name}-{self.sha[:12]}-{uuid.uuid4().hex[:8]}"
        self.attempt.mkdir(parents=True, mode=0o700)
        write_json(self.attempt / "attempt.json", {"target_sha": self.sha, "target_release": self.release.name})

    def validate_config(self):
        # Values are inspected only in memory; never log config or subprocess stderr.
        resolved = json.loads(self.compose(self.release, "config", "--format", "json"))
        backend = resolved["services"]["backend"]["environment"]
        frontend = resolved["services"]["frontend"]["environment"]
        if backend.get("DB_NAME") != self.db_name:
            raise DeployError("App DB_NAME must match the existing PostgreSQL database being backed up")
        if backend.get("SPRING_PROFILES_ACTIVE") != "prod" or backend.get("SPRING_SQL_INIT_MODE") != "never":
            raise DeployError("Deployment must use prod with legacy SQL initialization disabled")
        if str(backend.get("ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN")).lower() != "false":
            raise DeployError("Development recovery tokens must remain disabled")
        if set(frontend) - {"NODE_ENV", "NEXT_PUBLIC_API_BASE_URL", "NEXT_PUBLIC_APP_URL", "TZ"}:
            raise DeployError("Unexpected secret-capable frontend environment variable")
        if len(str(backend.get("JWT_SECRET", ""))) < 32:
            raise DeployError("Production JWT_SECRET must contain at least 32 characters")

    def pull_images(self):
        for service, reference in self.images.items():
            self.run_command(["docker", "pull", reference], "Pull immutable " + service + " image")
            data = self.docker_json(["image", "inspect", reference], "Inspect downloaded image")[0]
            if data.get("Architecture") != "arm64" or data.get("Os") != "linux":
                raise DeployError("Deployment images must support linux/arm64 on this Mac")
            if data.get("Config", {}).get("Labels", {}).get("org.opencontainers.image.revision") != self.sha:
                raise DeployError("Downloaded image revision does not match RELEASE_SHA")
            self.expected_ids[service] = data["Id"]

    def snapshot_rollback(self):
        previous = self.current.resolve() if self.current.exists() else None
        rollback = self.attempt / "rollback"
        rollback.mkdir(mode=0o700)
        template = previous or self.release
        if previous:
            shutil.copyfile(template / "compose.yml", rollback / "compose.yml")
            shutil.copyfile(template / "runtime.env", rollback / "runtime.env")
            os.chmod(rollback / "runtime.env", 0o600)
        else:
            # The first release's new .env may be wrong. Preserve the actual
            # prior running container values rather than reuse new credentials.
            resolved = json.loads(self.compose(self.release, "config", "--format", "json"))
            required = {"backend": {"DB_NAME", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET", "CORS_ALLOWED_ORIGINS",
                                    "OAUTH_FRONTEND_CALLBACK_URL"},
                        "frontend": {"NEXT_PUBLIC_API_BASE_URL", "NEXT_PUBLIC_APP_URL"}}
            for service in SERVICES:
                prior = self.previous_environment[service]
                if required[service] - set(prior):
                    raise DeployError("Initial rollback cannot capture required prior " + service + " configuration")
                environment = resolved["services"][service]["environment"]
                # Keep exactly the app-specific allowlist, never copy every old
                # env_file entry (the old frontend used to receive all secrets).
                for key in environment:
                    if key in prior:
                        environment[key] = prior[key]
                    if isinstance(environment[key], str):
                        environment[key] = environment[key].replace("$", "$$")
                resolved["services"][service]["image"] = "${" + service.upper() + "_IMAGE:?}"
            resolved["services"]["backend"]["environment"].update({
                "SPRING_PROFILES_ACTIVE": "prod", "SPRING_SQL_INIT_MODE": "never",
                "ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN": "false", "FLYWAY_BASELINE_ON_MIGRATE": "false",
                "DB_HOST": "postgresql", "DB_PORT": "5432", "INFO_APP_VERSION": "${RELEASE_SHA:?}",
            })
            write_json(rollback / "compose.yml", resolved)
            write_private(rollback / "runtime.env", "# Prior values are captured in the private rollback Compose file.\n")
        images = {}
        for service, image_id in self.previous_ids.items():
            tag = f"health-center-rollback-{service}:{image_id.removeprefix('sha256:')}"
            self.run_command(["docker", "image", "tag", image_id, tag], "Retain prior " + service + " image")
            images[service] = tag
        old_sha = json.loads((previous / "release.json").read_text())["sha"] if previous else "legacy"
        self.image_env(rollback, images, old_sha, baseline=False)
        self.compose(rollback, "config", "--quiet")
        write_json(self.attempt / "rollback.json", {"images": self.previous_ids, "sha": old_sha,
                                                     "previous_release": str(previous) if previous else None,
                                                     "database_rollback": False})

    def backup_database(self):
        dump = self.attempt / "database.dump"
        with open(dump, "wb") as handle:
            os.chmod(dump, 0o600)
            self.run_command([
                "docker", "exec", DB_CONTAINER, "sh", "-c",
                'exec pg_dump --username="$POSTGRES_USER" --dbname="$POSTGRES_DB" --format=custom',
            ], "Pre-deployment PostgreSQL backup", stdout=handle)
            handle.flush()
            os.fsync(handle.fileno())
        if dump.stat().st_size == 0:
            raise DeployError("Database backup is empty; app deployment refused")
        with open(dump, "rb") as handle:
            self.run_command(["docker", "exec", "-i", DB_CONTAINER, "pg_restore", "--list"],
                             "Validate PostgreSQL backup archive", stdin=handle)
        log("PostgreSQL backup saved and archive verified; it will not be automatically restored")

    def http_json(self, url):
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        with opener.open(url, timeout=5) as response:
            return json.loads(response.read())

    def http_frontend(self, url="http://127.0.0.1:3000/"):
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        with opener.open(url, timeout=5) as response:
            return 200 <= response.status < 400

    def wait_healthy(self, expected_ids, version=None):
        deadline = time.monotonic() + self.timeout
        while time.monotonic() < deadline:
            try:
                matches = True
                for service in SERVICES:
                    container = self.inspect(PROJECT + "-" + service)
                    state = container.get("State", {})
                    matches &= container.get("Image") == expected_ids[service] and state.get("Running") is True
                    matches &= state.get("Health", {}).get("Status", "healthy") == "healthy"
                health = self.http_json("http://127.0.0.1:8080/actuator/health")
                info = self.http_json("http://127.0.0.1:8080/actuator/info") if version else {}
                if (matches and health.get("status") == "UP" and self.http_frontend()
                        and (version is None or info.get("app", {}).get("version") == version)):
                    return
            except (DeployError, OSError, ValueError, KeyError):
                pass
            time.sleep(2)
        raise DeployError("App readiness/image/version verification timed out")

    def verify_public(self):
        deadline = time.monotonic() + min(self.timeout, 60)
        while time.monotonic() < deadline:
            try:
                health = self.http_json("https://api.healthq.store/actuator/health")
                info = self.http_json("https://api.healthq.store/actuator/info")
                if (health.get("status") == "UP" and info.get("app", {}).get("version") == self.sha
                        and self.http_frontend("https://demo.healthq.store/")):
                    return
            except (OSError, ValueError):
                pass
            time.sleep(2)
        raise DeployError("Origin is healthy but public route verification failed; the healthy release remains active")

    def up(self, directory, services=SERVICES):
        self.compose(directory, "up", "--detach", "--no-deps", "--no-build", "--pull", "never", *services)

    def deploy(self):
        os.umask(0o077)
        with self.lock():
            self.preflight()
            self.prepare_release()
            self.pull_images()
            self.snapshot_rollback()
            self.backup_database()
            try:
                self.up(self.release)
                self.wait_healthy(self.expected_ids, self.sha)
                if self.bootstrap:
                    # Baseline permission is a one-time transition, not persistent
                    # runtime configuration. A second start verifies normal mode.
                    self.image_env(self.release, self.images, self.sha, baseline=False)
                    self.up(self.release, ("backend",))
                    self.wait_healthy(self.expected_ids, self.sha)
            except (DeployError, OSError) as failure:
                log("Deployment failed; restoring prior app images. Database changes are not rolled back")
                self.image_env(self.release, self.images, self.sha, baseline=False)
                try:
                    self.up(self.attempt / "rollback")
                    self.wait_healthy(self.previous_ids)
                    write_json(self.attempt / "result.json", {"status": "rolled-back", "sha": self.sha})
                except (DeployError, OSError) as rollback_failure:
                    write_json(self.attempt / "result.json", {"status": "rollback-failed", "sha": self.sha})
                    raise DeployError("Deployment and app rollback failed; operator intervention required; DB backup retained") from rollback_failure
                raise DeployError("Deployment failed; prior app images restored; DB was not restored") from failure
            metadata = json.loads((self.release / "release.json").read_text())
            metadata["status"] = "healthy"
            write_json(self.release / "release.json", metadata)
            next_link = self.root / (".current-" + uuid.uuid4().hex)
            next_link.symlink_to(self.release)
            os.replace(next_link, self.current)
            if self.bootstrap:
                self.adoption_marker.unlink(missing_ok=True)
            write_json(self.attempt / "result.json", {"status": "healthy", "sha": self.sha})
            log(f"Deployed {self.sha}; backend/frontend healthy and backend version verified")
            try:
                self.verify_public()
            except DeployError:
                write_json(self.attempt / "result.json", {"status": "origin-healthy-public-check-failed", "sha": self.sha})
                raise
            log("Public frontend and API health/version checks passed")

    def manual_rollback(self, target_attempt):
        """Restore an existing snapshot, preserving a new backup and recovery snapshot."""
        os.umask(0o077)
        with self.lock():
            self.preflight(check_adoption=False)
            metadata = json.loads((target_attempt / "rollback.json").read_text())
            target = target_attempt / "rollback"
            previous = metadata.get("previous_release")
            if previous:
                previous = Path(previous).resolve()
                if (previous.parent != (self.root / "releases").resolve()
                        or not (previous / "release.json").is_file()):
                    raise DeployError("Rollback metadata references an invalid managed release")
            secure_file(target / "runtime.env")
            for service, image_id in metadata["images"].items():
                tag = f"health-center-rollback-{service}:{image_id.removeprefix('sha256:')}"
                image = self.docker_json(["image", "inspect", tag], "Check retained rollback image")[0]
                if image["Id"] != image_id:
                    raise DeployError("Retained rollback image no longer matches its snapshot")
            self.compose(target, "config", "--quiet")
            self.attempt = self.root / "attempts" / (datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%dT%H%M%SZ")
                                                    + "-rollback-" + uuid.uuid4().hex[:8])
            self.attempt.mkdir(parents=True, mode=0o700)
            write_json(self.attempt / "attempt.json", {"target_sha": self.sha, "target_release": self.release.name})
            self.snapshot_rollback()
            self.backup_database()
            try:
                self.up(target)
                version = metadata["sha"] if metadata["sha"] != "legacy" else None
                self.wait_healthy(metadata["images"], version)
            except (DeployError, OSError) as failure:
                try:
                    self.up(self.attempt / "rollback")
                    self.wait_healthy(self.previous_ids)
                except (DeployError, OSError) as recovery_failure:
                    raise DeployError("Manual rollback and recovery failed; operator intervention required") from recovery_failure
                raise DeployError("Manual rollback failed; previous running app images were recovered") from failure
            if previous:
                next_link = self.root / (".current-" + uuid.uuid4().hex)
                next_link.symlink_to(previous)
                os.replace(next_link, self.current)
            else:
                self.current.unlink(missing_ok=True)
            write_json(self.attempt / "result.json", {"status": "manual-rollback-healthy", "sha": metadata["sha"]})
            log(f"App rollback verified ({metadata['sha']}); database was not restored")


def main():
    def interrupted(signum, _frame):
        raise DeployError(f"Deployment interrupted by signal {signum}")
    signal.signal(signal.SIGTERM, interrupted)
    signal.signal(signal.SIGINT, interrupted)
    try:
        if len(sys.argv) == 3 and sys.argv[1] == "--rollback":
            attempt_name = sys.argv[2]
            if not re.fullmatch(r"[A-Za-z0-9_-]+", attempt_name):
                raise DeployError("Rollback accepts an attempt directory name, not a path")
            root = Path(os.environ.get("DEPLOY_ROOT", "/Users/tro/services/health-center"))
            attempt = root / "attempts" / attempt_name
            if attempt.is_symlink() or attempt.resolve().parent != (root / "attempts").resolve():
                raise DeployError("Rollback attempt must be inside the managed attempts directory")
            attempt_metadata = json.loads((attempt / "attempt.json").read_text())
            release_name = attempt_metadata["target_release"]
            if not re.fullmatch(r"[a-f0-9]{40}-[a-f0-9]{12}", release_name):
                raise DeployError("Rollback attempt has an invalid release identifier")
            release_path = root / "releases" / release_name
            release = json.loads((release_path / "release.json").read_text())
            environment = {**os.environ, "RELEASE_SHA": release["sha"], "BACKEND_IMAGE": release["images"]["backend"],
                           "FRONTEND_IMAGE": release["images"]["frontend"], "BOOTSTRAP_EXISTING_DB": "0"}
            worker = Deployer(environment)
            worker.release = release_path
            worker.manual_rollback(attempt)
        elif len(sys.argv) == 1:
            Deployer().deploy()
        else:
            raise DeployError("Usage: deploy.sh [--rollback ATTEMPT_DIRECTORY_NAME]")
    except (DeployError, OSError, ValueError, KeyError) as error:
        # OS/JSON errors can contain paths, but never render command output or env.
        log(f"Deployment stopped: {error}" if isinstance(error, DeployError)
            else "Deployment stopped by a filesystem/runtime error; inspect deployment state")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
