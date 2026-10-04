"""No live Docker changes: exercise deployment ordering and recovery with fakes."""

import importlib.util
import io
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest import mock


MODULE = Path(__file__).resolve().parents[1] / "deploy.py"
SPEC = importlib.util.spec_from_file_location("health_center_deploy", MODULE)
deploy = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(deploy)
SHA = "a" * 40
IMAGES = {service: f"ghcr.io/example/health-center-{service}@sha256:{digit * 64}"
          for service, digit in (("backend", "b"), ("frontend", "c"))}
OLD_IDS = {"backend": "sha256:" + "1" * 64, "frontend": "sha256:" + "2" * 64}
NEW_IDS = {"backend": "sha256:" + "3" * 64, "frontend": "sha256:" + "4" * 64}


class FakeDocker:
    def __init__(self):
        self.calls = []
        self.up_calls = []
        self.fail_backup = False
        self.fail_up = False
        self.fail_rollback = False
        self.db_health = "healthy"
        self.volume = deploy.DB_VOLUME
        self.arch = "arm64"
        self.db_name = "health_center"
        self.container_ids = dict(OLD_IDS)
        self.revision = SHA

    def __call__(self, args, purpose, **kwargs):
        self.calls.append((args, purpose))
        if args[:2] == ["docker", "inspect"]:
            container = args[2]
            if container == deploy.DB_CONTAINER:
                return json.dumps([{
                    "State": {"Health": {"Status": self.db_health}},
                    "Config": {"Labels": {"com.docker.compose.project": deploy.PROJECT},
                               "Env": ["POSTGRES_DB=health_center"]},
                    "Mounts": [{"Name": self.volume, "Destination": "/var/lib/postgresql"}],
                    "NetworkSettings": {"Networks": {deploy.NETWORK: {"Aliases": ["postgresql"]}}},
                }]).encode()
            service = container.removeprefix("health-center-")
            prior_env = ["DB_NAME=health_center", "DB_USERNAME=health", "DB_PASSWORD=old-private-password",
                         "JWT_SECRET=" + "s" * 32, "CORS_ALLOWED_ORIGINS=https://demo.healthq.store",
                         "OAUTH_FRONTEND_CALLBACK_URL=https://demo.healthq.store/login/social/callback",
                         "NEXT_PUBLIC_API_BASE_URL=https://api.healthq.store", "NEXT_PUBLIC_APP_URL=https://demo.healthq.store",
                         "SPRING_PROFILES_ACTIVE=dev", "SPRING_SQL_INIT_MODE=always",
                         "ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN=true"]
            return json.dumps([{
                "Image": self.container_ids[service],
                "Config": {"Labels": {"com.docker.compose.project": deploy.PROJECT}, "Env": prior_env},
                "State": {"Running": True, "Health": {"Status": "healthy"}},
            }]).encode()
        if args[:3] == ["docker", "image", "inspect"]:
            if args[3].startswith("health-center-rollback-"):
                return json.dumps([{"Id": "sha256:" + args[3].split(":")[1]}]).encode()
            service = next(service for service in IMAGES if IMAGES[service] == args[3])
            return json.dumps([{"Architecture": self.arch, "Os": "linux", "Id": NEW_IDS[service],
                                "Config": {"Labels": {"org.opencontainers.image.revision": self.revision}}}]).encode()
        if "pg_dump" in " ".join(args):
            if self.fail_backup:
                raise deploy.DeployError("Backup failed")
            kwargs["stdout"].write(b"PGDMP fixture archive")
            return b""
        if args[:2] == ["docker", "compose"] and "--file" in args:
            directory = Path(args[args.index("--file") + 1]).parent
            command = args[args.index("--file") + 2:]
            if command == ["config", "--format", "json"]:
                return json.dumps({"services": {
                    "backend": {"environment": {
                        "DB_NAME": self.db_name,
                        "DB_PASSWORD": "new-private-password", "DB_USERNAME": "health",
                        "CORS_ALLOWED_ORIGINS": "https://demo.healthq.store",
                        "OAUTH_FRONTEND_CALLBACK_URL": "https://demo.healthq.store/login/social/callback",
                        "SPRING_PROFILES_ACTIVE": "prod", "SPRING_SQL_INIT_MODE": "never",
                        "ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN": "false", "JWT_SECRET": "x" * 32,
                    }},
                    "frontend": {"environment": {"NODE_ENV": "production",
                                   "NEXT_PUBLIC_API_BASE_URL": "https://api.healthq.store",
                                   "NEXT_PUBLIC_APP_URL": "https://demo.healthq.store"}},
                }}).encode()
            if command[0] == "up":
                self.up_calls.append((directory, command, (directory / "images.env").read_text()))
                if directory.name == "rollback" and self.fail_rollback:
                    raise deploy.DeployError("Rollback failed")
                if directory.name != "rollback" and self.fail_up:
                    raise deploy.DeployError("Rollout failed")
                image_env = dict(line.split("=", 1) for line in (directory / "images.env").read_text().splitlines() if "=" in line)
                for service in deploy.SERVICES:
                    ref = image_env[service.upper() + "_IMAGE"]
                    self.container_ids[service] = ("sha256:" + ref.split(":")[1] if ref.startswith("health-center-rollback-")
                                                   else NEW_IDS[service])
        return b""


class DeployTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.root = Path(self.temp.name) / "service"
        shared = self.root / "shared"
        shared.mkdir(parents=True)
        self.runtime = shared / "production.env"
        self.runtime.write_text("DB_PASSWORD=private-not-for-logs\n")
        self.runtime.chmod(0o600)
        self.environment = {
            "DEPLOY_ROOT": str(self.root), "RELEASE_SHA": SHA, "BOOTSTRAP_EXISTING_DB": "1",
            "BACKEND_IMAGE": IMAGES["backend"], "FRONTEND_IMAGE": IMAGES["frontend"],
            "PATH": os.environ["PATH"], "HOME": str(self.root),
            "DB_PASSWORD": "host-value-must-not-override-file", "COMPOSE_PROFILES": "observability",
        }
        self.worker = deploy.Deployer(self.environment)
        self.docker = FakeDocker()
        self.command_patch = mock.patch.object(self.worker, "run_command", side_effect=self.docker)
        self.command_patch.start()
        self.health_patch = mock.patch.object(self.worker, "wait_healthy")
        self.health = self.health_patch.start()
        self.public_patch = mock.patch.object(self.worker, "verify_public")
        self.public = self.public_patch.start()

    def tearDown(self):
        self.health_patch.stop()
        self.public_patch.stop()
        self.command_patch.stop()
        self.temp.cleanup()

    def assert_no_up(self):
        self.assertEqual([], self.docker.up_calls)

    def test_first_adoption_backs_up_then_deploys_and_disables_baseline(self):
        self.worker.deploy()
        self.assertEqual(self.worker.release.resolve(), (self.root / "current").resolve())
        first, second = self.docker.up_calls
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=true", first[2])
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=false", second[2])
        self.assertEqual(["backend", "frontend"], first[1][-2:])
        self.assertEqual("backend", second[1][-1])
        calls = [args for args, _ in self.docker.calls]
        backup_index = next(i for i, args in enumerate(calls) if "pg_dump" in " ".join(args))
        validate_index = next(i for i, args in enumerate(calls) if "pg_restore" in args)
        up_index = next(i for i, args in enumerate(calls) if "up" in args)
        self.assertLess(backup_index, validate_index)
        self.assertLess(validate_index, up_index)
        self.assertEqual(0o600, (self.worker.attempt / "database.dump").stat().st_mode & 0o777)
        self.assertNotIn("--remove-orphans", str(calls))
        self.assertNotIn("prune", str(calls))
        self.assertNotIn("down", str(calls))
        for _, command, _ in self.docker.up_calls:
            self.assertIn("--no-deps", command)
            self.assertNotIn("postgresql", command)
        self.assertEqual(2, self.health.call_count)
        self.health.assert_called_with(NEW_IDS, SHA)

    def test_bad_volume_refuses_to_change_apps(self):
        self.docker.volume = "some-new-empty-volume"
        with self.assertRaisesRegex(deploy.DeployError, "expected existing data volume"):
            self.worker.deploy()
        self.assert_no_up()
        self.assertFalse((self.root / ".deploy-lock").exists())

    def test_unhealthy_db_refuses_to_change_apps(self):
        self.docker.db_health = "unhealthy"
        with self.assertRaisesRegex(deploy.DeployError, "PostgreSQL must be healthy"):
            self.worker.deploy()
        self.assert_no_up()

    def test_first_adoption_requires_explicit_flag(self):
        self.worker.bootstrap = False
        with self.assertRaisesRegex(deploy.DeployError, "First adoption requires"):
            self.worker.deploy()
        self.assert_no_up()

    def test_secure_initial_adoption_marker_authorizes_once_and_is_consumed(self):
        self.worker.bootstrap = False
        self.worker.adoption_marker.write_text("Initial database adoption was explicitly authorized\n")
        self.worker.adoption_marker.chmod(0o600)
        self.worker.deploy()
        self.assertFalse(self.worker.adoption_marker.exists())
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=true", self.docker.up_calls[0][2])
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=false", self.docker.up_calls[1][2])

    def test_insecure_adoption_marker_is_rejected(self):
        self.worker.bootstrap = False
        self.worker.adoption_marker.write_text("authorized\n")
        self.worker.adoption_marker.chmod(0o644)
        with self.assertRaisesRegex(deploy.DeployError, "mode 600"):
            self.worker.deploy()
        self.assert_no_up()

    def test_failed_initial_rollout_preserves_adoption_marker_for_retry(self):
        self.worker.bootstrap = False
        self.worker.adoption_marker.write_text("authorized\n")
        self.worker.adoption_marker.chmod(0o600)
        self.docker.fail_up = True
        with self.assertRaises(deploy.DeployError):
            self.worker.deploy()
        self.assertTrue(self.worker.adoption_marker.exists())

    def test_managed_release_does_not_reenable_baseline_from_stale_marker(self):
        self.worker.deploy()
        self.worker.bootstrap = False
        self.worker.adoption_marker.write_text("stale authorization\n")
        self.worker.adoption_marker.chmod(0o600)
        self.worker.deploy()
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=false", self.docker.up_calls[-1][2])

    def test_backup_failure_never_starts_apps(self):
        self.docker.fail_backup = True
        with self.assertRaisesRegex(deploy.DeployError, "Backup failed"):
            self.worker.deploy()
        self.assert_no_up()
        self.assertFalse((self.root / "current").exists())

    def test_wrong_database_is_not_backed_up_or_deployed(self):
        self.docker.db_name = "another_database"
        with self.assertRaisesRegex(deploy.DeployError, "DB_NAME must match"):
            self.worker.deploy()
        self.assert_no_up()
        self.assertFalse(any("pg_dump" in " ".join(args) for args, _ in self.docker.calls))

    def test_initial_failure_restores_actual_local_image_ids_without_db_restore(self):
        self.docker.fail_up = True
        with self.assertRaisesRegex(deploy.DeployError, "prior app images restored"):
            self.worker.deploy()
        rollback = self.docker.up_calls[-1]
        self.assertEqual("rollback", rollback[0].name)
        for service in deploy.SERVICES:
            self.assertIn(f"health-center-rollback-{service}:{OLD_IDS[service][7:]}", rollback[2])
        self.assertIn("FLYWAY_BASELINE_ON_MIGRATE=false", rollback[2])
        rollback_compose = json.loads((rollback[0] / "compose.yml").read_text())
        backend_env = rollback_compose["services"]["backend"]["environment"]
        self.assertEqual("old-private-password", backend_env["DB_PASSWORD"])
        self.assertEqual("never", backend_env["SPRING_SQL_INIT_MODE"])
        self.assertEqual("prod", backend_env["SPRING_PROFILES_ACTIVE"])
        self.assertEqual("false", backend_env["ACCOUNT_RECOVERY_EXPOSE_DEVELOPMENT_TOKEN"])
        self.assertNotIn("DB_PASSWORD", rollback_compose["services"]["frontend"]["environment"])
        self.assertFalse((self.root / "current").exists())
        self.health.assert_called_once_with(OLD_IDS)
        restores = [args for args, _ in self.docker.calls if "pg_restore" in args]
        self.assertEqual(1, len(restores))
        self.assertIn("--list", restores[0])

    def test_readiness_failure_triggers_rollback(self):
        self.health.side_effect = [deploy.DeployError("Wrong release version"), None]
        with self.assertRaisesRegex(deploy.DeployError, "prior app images restored"):
            self.worker.deploy()
        self.assertEqual("rollback", self.docker.up_calls[-1][0].name)
        self.assertFalse((self.root / "current").exists())

    def test_rollback_failure_is_reported_and_backup_is_preserved(self):
        self.docker.fail_up = True
        self.docker.fail_rollback = True
        with self.assertRaisesRegex(deploy.DeployError, "operator intervention required"):
            self.worker.deploy()
        result = json.loads((self.worker.attempt / "result.json").read_text())
        self.assertEqual("rollback-failed", result["status"])
        self.assertTrue((self.worker.attempt / "database.dump").is_file())
        self.assertFalse((self.root / "current").exists())

    def test_managed_release_cannot_reuse_bootstrap_permission(self):
        self.worker.deploy()
        with self.assertRaisesRegex(deploy.DeployError, "only for the first adoption"):
            self.worker.deploy()

    def test_failed_release_can_be_retried_with_identical_inputs(self):
        self.docker.fail_backup = True
        with self.assertRaises(deploy.DeployError):
            self.worker.deploy()
        self.docker.fail_backup = False
        self.worker.deploy()
        self.assertEqual(self.worker.release.resolve(), self.current_target())

    def current_target(self):
        return (self.root / "current").resolve()

    def test_changed_config_gets_another_immutable_release_directory(self):
        self.docker.fail_backup = True
        with self.assertRaises(deploy.DeployError):
            self.worker.deploy()
        old_release = self.worker.release
        self.runtime.write_text("DB_PASSWORD=different-secret\n")
        self.docker.fail_backup = False
        self.worker.deploy()
        self.assertNotEqual(old_release, self.worker.release)
        self.assertIn("private-not-for-logs", (old_release / "runtime.env").read_text())
        self.assertEqual(self.worker.release.resolve(), self.current_target())

    def test_public_failure_keeps_healthy_origin_and_reports_failure(self):
        self.public.side_effect = deploy.DeployError("Public route failed")
        with self.assertRaisesRegex(deploy.DeployError, "Public route failed"):
            self.worker.deploy()
        self.assertEqual(self.worker.release.resolve(), self.current_target())
        self.assertFalse(any(path.name == "rollback" for path, _, _ in self.docker.up_calls))
        result = json.loads((self.worker.attempt / "result.json").read_text())
        self.assertEqual("origin-healthy-public-check-failed", result["status"])

    def test_manual_rollback_preserves_backup_and_reverts_to_initial_images(self):
        self.worker.deploy()
        original_attempt = self.worker.attempt
        self.worker.bootstrap = False
        self.worker.manual_rollback(original_attempt)
        self.assertFalse((self.root / "current").exists())
        self.assertEqual(original_attempt / "rollback", self.docker.up_calls[-1][0])
        self.assertNotEqual(original_attempt, self.worker.attempt)
        self.assertTrue((self.worker.attempt / "database.dump").exists())
        self.health.assert_called_with(OLD_IDS, None)

    def test_rollback_of_rollback_restores_managed_release_and_image_ids(self):
        self.worker.deploy()
        managed_release = self.worker.release.resolve()
        first_attempt = self.worker.attempt
        self.worker.bootstrap = False
        self.worker.manual_rollback(first_attempt)
        rollback_attempt = self.worker.attempt
        self.assertEqual(OLD_IDS, self.docker.container_ids)
        self.worker.manual_rollback(rollback_attempt)
        self.assertEqual(NEW_IDS, self.docker.container_ids)
        self.assertEqual(managed_release, self.current_target())
        self.health.assert_called_with(NEW_IDS, SHA)

    def test_image_revision_must_match_requested_commit(self):
        self.docker.revision = "d" * 40
        with self.assertRaisesRegex(deploy.DeployError, "revision does not match"):
            self.worker.deploy()
        self.assert_no_up()

    def test_rejects_non_arm_image_before_any_app_changes(self):
        self.docker.arch = "amd64"
        with self.assertRaisesRegex(deploy.DeployError, "linux/arm64"):
            self.worker.deploy()
        self.assert_no_up()

    def test_env_file_permissions_are_enforced(self):
        self.runtime.chmod(0o644)
        with self.assertRaisesRegex(deploy.DeployError, "mode 600"):
            self.worker.deploy()
        self.assert_no_up()

    def test_lock_serializes_deployments(self):
        with self.worker.lock():
            with self.assertRaisesRegex(deploy.DeployError, "Another deployment"):
                with self.worker.lock():
                    self.fail("Must not enter a second deployment")
        self.assertFalse((self.root / ".deploy-lock").exists())

    def test_only_allowlisted_host_environment_reaches_compose(self):
        self.assertNotIn("DB_PASSWORD", self.worker.env)
        self.assertNotIn("COMPOSE_PROFILES", self.worker.env)
        self.assertNotIn("BOOTSTRAP_EXISTING_DB", self.worker.env)
        self.assertEqual(self.environment["PATH"], self.worker.env["PATH"])

    def test_tags_and_invalid_shas_are_rejected(self):
        for key, value in (("BACKEND_IMAGE", "ghcr.io/example/backend:latest"),
                           ("RELEASE_SHA", "main")):
            with self.subTest(key=key):
                with self.assertRaises(deploy.DeployError):
                    deploy.Deployer({**self.environment, key: value})


class ReadinessTests(unittest.TestCase):
    def worker(self):
        return deploy.Deployer({"RELEASE_SHA": SHA, "BACKEND_IMAGE": IMAGES["backend"],
                                "FRONTEND_IMAGE": IMAGES["frontend"]})

    def test_wrong_version_does_not_count_as_healthy(self):
        worker = self.worker()
        def inspected(container):
            service = container.removeprefix("health-center-")
            return {"Image": NEW_IDS[service], "State": {"Running": True}}
        with mock.patch.object(worker, "inspect", side_effect=inspected), \
                mock.patch.object(worker, "http_json", side_effect=[{"status": "UP"}, {"app": {"version": "old"}}]), \
                mock.patch.object(worker, "http_frontend", return_value=True), \
                mock.patch.object(deploy.time, "monotonic", side_effect=[0, 0, 181]), \
                mock.patch.object(deploy.time, "sleep"):
            with self.assertRaisesRegex(deploy.DeployError, "verification timed out"):
                worker.wait_healthy(NEW_IDS, SHA)

    def test_wrong_image_does_not_count_as_healthy(self):
        worker = self.worker()
        with mock.patch.object(worker, "inspect", return_value={"Image": "old-id", "State": {"Running": True}}), \
                mock.patch.object(worker, "http_json", side_effect=[{"status": "UP"}, {"app": {"version": SHA}}]), \
                mock.patch.object(worker, "http_frontend", return_value=True), \
                mock.patch.object(deploy.time, "monotonic", side_effect=[0, 0, 181]), \
                mock.patch.object(deploy.time, "sleep"):
            with self.assertRaises(deploy.DeployError):
                worker.wait_healthy(NEW_IDS, SHA)


class PublicRouteTests(unittest.TestCase):
    def setUp(self):
        self.worker = deploy.Deployer({"RELEASE_SHA": SHA, "BACKEND_IMAGE": IMAGES["backend"],
                                       "FRONTEND_IMAGE": IMAGES["frontend"]})

    def response(self, payload=None, body=None):
        response = mock.MagicMock()
        response.__enter__.return_value = response
        response.status = 200
        response.read.return_value = json.dumps(payload).encode() if body is None else body
        return response

    def healthy_responses(self):
        return [self.response({"status": "UP"}), self.response({"app": {"version": SHA}}),
                self.response(body=b"frontend HTML")]

    def failure(self, responses):
        with mock.patch.object(deploy.urllib.request, "build_opener") as build_opener, \
                mock.patch.object(deploy.time, "monotonic", side_effect=[0, 0, 61]), \
                mock.patch.object(deploy.time, "sleep"), \
                self.assertRaises(deploy.DeployError) as raised:
            build_opener.return_value.open.side_effect = responses
            self.worker.verify_public()
        return str(raised.exception)

    def test_common_requests_use_an_honest_agent_and_keep_proxy_and_timeout_policy(self):
        responses = self.healthy_responses() + [self.response({"status": "UP"}), self.response()]
        with mock.patch.object(deploy.urllib.request, "build_opener") as build_opener, \
                mock.patch.object(deploy.time, "monotonic", side_effect=[0, 0, 61]):
            build_opener.return_value.open.side_effect = responses
            self.worker.verify_public()
            self.worker.http_json("http://127.0.0.1:8080/actuator/health")
            self.assertTrue(self.worker.http_frontend())
        requests = [call.args[0] for call in build_opener.return_value.open.call_args_list]
        self.assertEqual([
            "https://api.healthq.store/actuator/health", "https://api.healthq.store/actuator/info",
            "https://demo.healthq.store/", "http://127.0.0.1:8080/actuator/health", "http://127.0.0.1:3000/",
        ], [request.full_url for request in requests])
        for request in requests:
            self.assertEqual("HealthCenter-Deployment/1.0", request.get_header("User-agent"))
            self.assertEqual("GET", request.get_method())
        for call in build_opener.call_args_list:
            self.assertEqual(1, len(call.args))
            self.assertIsInstance(call.args[0], deploy.urllib.request.ProxyHandler)
            self.assertEqual({}, call.args[0].proxies)
        for call in build_opener.return_value.open.call_args_list:
            self.assertEqual({"timeout": 5}, call.kwargs)

    def test_http_failures_report_all_endpoints_without_remote_secrets(self):
        secret = "private-response-token"
        errors = [deploy.urllib.error.HTTPError(
            "https://access.example/login?token=" + secret, 403, secret,
            {"cf-mitigated": "challenge", "Set-Cookie": secret}, io.BytesIO(secret.encode()),
        ) for _ in range(3)]
        errors[1].headers["cf-mitigated"] = secret
        message = self.failure(errors)
        for endpoint in ("api-health", "api-info", "frontend"):
            self.assertIn(endpoint + ": HTTP 403", message)
        self.assertEqual(2, message.count("cf-mitigated=challenge"))
        self.assertNotIn(secret, message)
        self.assertNotIn("access.example", message)
        self.assertNotIn("Set-Cookie", message)
        self.assertTrue(all(error.fp.closed for error in errors))

    def test_invalid_json_health_and_version_failures_remain_failed_and_redacted(self):
        secret = "unexpected-private-value"
        cases = (
            (0, self.response({"status": "DOWN"}), "api-health: health status DOWN"),
            (0, self.response({"status": secret}), "api-health: health status unexpected"),
            (0, self.response(body=secret.encode()), "api-health: invalid JSON"),
            (0, self.response([]), "api-health: invalid JSON object"),
            (1, self.response({"app": None}), "api-info: backend version mismatch"),
            (1, self.response({"app": {"version": secret}}), "api-info: backend version mismatch"),
            (1, self.response(body=secret.encode()), "api-info: invalid JSON"),
        )
        for index, response, expected in cases:
            with self.subTest(expected=expected):
                responses = self.healthy_responses()
                responses[index] = response
                message = self.failure(responses)
                self.assertIn(expected, message)
                self.assertNotIn(secret, message)

    def test_network_failures_are_classified_without_raw_exception_details(self):
        secret = "private-error-detail"
        cases = (
            (0, deploy.ssl.SSLCertVerificationError(1, secret), "api-health: TLS certificate verification failed"),
            (1, deploy.ssl.SSLError(1, secret), "api-info: TLS error"),
            (2, TimeoutError(secret), "frontend: request timed out"),
            (0, OSError(secret), "api-health: network error"),
            (1, secret, "api-info: network error"),
        )
        for index, reason, expected in cases:
            with self.subTest(expected=expected):
                responses = self.healthy_responses()
                responses[index] = deploy.urllib.error.URLError(reason)
                message = self.failure(responses)
                self.assertIn(expected, message)
                self.assertNotIn(secret, message)

    def test_transient_public_failure_can_recover_without_failure_logs(self):
        first = self.healthy_responses()
        first[0] = deploy.urllib.error.HTTPError("https://api.healthq.store/actuator/health", 403,
                                                "Forbidden", {}, None)
        with mock.patch.object(deploy.urllib.request, "build_opener") as build_opener, \
                mock.patch.object(deploy.time, "monotonic", side_effect=[0, 0, 2]), \
                mock.patch.object(deploy.time, "sleep") as sleep, \
                mock.patch.object(deploy, "log") as log:
            build_opener.return_value.open.side_effect = first + self.healthy_responses()
            self.worker.verify_public()
            self.assertEqual(6, build_opener.return_value.open.call_count)
            sleep.assert_called_once_with(2)
            log.assert_not_called()


class WrapperTests(unittest.TestCase):
    def test_shell_wrapper_forwards_rollback_arguments(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            fake_python = root / "python3"
            captured = root / "arguments.txt"
            fake_python.write_text('#!/bin/sh\nprintf "%s\\n" "$@" > "$CAPTURE"\n')
            fake_python.chmod(0o700)
            environment = {**os.environ, "PATH": str(root) + os.pathsep + os.environ["PATH"], "CAPTURE": str(captured)}
            subprocess.run([str(MODULE.with_name("deploy.sh")), "--rollback", "an-attempt"],
                           env=environment, check=True, capture_output=True)
            arguments = captured.read_text().splitlines()
            self.assertEqual(str(MODULE), arguments[0])
            self.assertEqual(["--rollback", "an-attempt"], arguments[1:])

if __name__ == "__main__":
    unittest.main()
