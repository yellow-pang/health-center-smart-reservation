"""Exercise the Mac workflow's credential setup/cleanup without Docker or Keychain."""

import json
import os
from pathlib import Path
import re
import stat
import subprocess
import tempfile
import unittest


WORKFLOW = Path(__file__).resolve().parents[3] / '.github/workflows/deploy.yml'
PREPARE = 'Prepare Mac tools and temporary registry credentials'
CLEANUP = 'Remove temporary registry credentials'
FAKE_TOKEN = 'registry-test-token-that-must-not-appear-in-logs'


def mac_steps():
    """Read step blocks from the Mac job, so tests execute the checked-in shell."""
    workflow = WORKFLOW.read_text()
    match = re.search(r'^  deploy:\s*\n', workflow, re.MULTILINE)
    if match is None:
        raise AssertionError('Mac deploy job is missing')
    job = workflow[match.end():]
    next_job = re.search(r'^  [A-Za-z_][A-Za-z0-9_-]*:\s*\n', job, re.MULTILINE)
    if next_job:
        job = job[:next_job.start()]
    return [block for block in re.split(r'(?m)(?=^      - )', job)
            if block.startswith('      - ')]


def named_step(name):
    matches = [block for block in mac_steps()
               if block.splitlines()[0] == f'      - name: {name}']
    if len(matches) != 1:
        raise AssertionError(f'Expected one Mac step named {name!r}')
    return matches[0]


def shell_body(step):
    lines = step.splitlines()
    try:
        start = lines.index('        run: |') + 1
    except ValueError as error:
        raise AssertionError('Expected a literal workflow shell block') from error
    body = []
    for line in lines[start:]:
        if not line.strip():
            body.append('')
        elif line.startswith('          '):
            body.append(line[10:])
        else:
            break
    if not body:
        raise AssertionError('Workflow shell block is empty')
    return '\n'.join(body) + '\n'


class RegistryCredentialsTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.runner_temp = self.root / 'runner temporary'
        self.runner_temp.mkdir()
        self.user_home = self.root / 'user home'
        self.user_docker = self.user_home / '.docker'
        self.user_plugins = self.user_docker / 'cli-plugins'
        self.user_plugins.mkdir(parents=True)
        self.user_config = self.user_docker / 'config.json'
        self.original_config = json.dumps({
            'credsStore': 'osxkeychain',
            'auths': {'example.test': {'auth': FAKE_TOKEN}},
        }).encode()
        self.user_config.write_bytes(self.original_config)
        self.user_config.chmod(0o600)
        self.github_env = self.root / 'github-env'
        self.github_path = self.root / 'github-path'
        self.github_env.touch()
        self.github_path.touch()
        # HOME is scoped to this subprocess fixture; no host Docker files are used.
        self.env = dict(os.environ, HOME=str(self.user_home), RUNNER_TEMP=str(self.runner_temp),
                        GITHUB_RUN_ID='31415', GITHUB_RUN_ATTEMPT='2',
                        GITHUB_ENV=str(self.github_env), GITHUB_PATH=str(self.github_path))
        self.env.pop('DOCKER_CONFIG', None)
        self.prepare_step = named_step(PREPARE)
        self.cleanup_step = named_step(CLEANUP)

    def run_shell(self, script, **overrides):
        return subprocess.run(['/bin/bash', '--noprofile', '--norc', '-e', '-o', 'pipefail', '-c', script],
                              cwd=self.root, env={**self.env, **overrides}, text=True,
                              stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=10)

    def prepare(self):
        result = self.run_shell(shell_body(self.prepare_step))
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertNotIn(FAKE_TOKEN, result.stdout + result.stderr)
        exported = dict(line.split('=', 1) for line in self.github_env.read_text().splitlines())
        self.assertIn('DOCKER_CONFIG', exported)
        directory = Path(exported['DOCKER_CONFIG'])
        self.assertEqual(self.runner_temp, directory.parent)
        return directory, exported

    def assert_user_config_unchanged(self):
        self.assertEqual(self.original_config, self.user_config.read_bytes())
        self.assertEqual(0o600, stat.S_IMODE(self.user_config.stat().st_mode))

    def test_prepare_prevents_default_keychain_selection_and_preserves_user_config(self):
        directory, _ = self.prepare()
        config = directory / 'config.json'
        settings = json.loads(config.read_text())
        # Docker skips automatic helper detection for nonempty auths, even when
        # the GHCR entry contains no credentials yet. Empty credsStore alone fails.
        self.assertTrue(settings.get('auths'))
        self.assertIn('ghcr.io', settings['auths'])
        self.assertEqual({}, settings['auths']['ghcr.io'])
        self.assertFalse(settings.get('credsStore'))
        self.assertFalse(settings.get('credHelpers'))
        self.assertEqual(0o700, stat.S_IMODE(directory.stat().st_mode))
        self.assertEqual(0o600, stat.S_IMODE(config.stat().st_mode))
        self.assertEqual(self.user_plugins.resolve(), (directory / 'cli-plugins').resolve())
        self.assert_user_config_unchanged()

    def test_cleanup_removes_temporary_credentials_and_preserves_user_config(self):
        self.assertRegex(self.cleanup_step, r'(?m)^        if: always\(\)\s*$')
        directory, exported = self.prepare()
        config = directory / 'config.json'
        config.write_text(json.dumps({'auths': {'ghcr.io': {'auth': FAKE_TOKEN}}}))
        marker = directory / 'keep-this-file'
        marker.write_text('Unrelated fixture\n')
        result = self.run_shell(shell_body(self.cleanup_step), **exported)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertNotIn(FAKE_TOKEN, result.stdout + result.stderr)
        self.assertFalse(config.exists())
        self.assertTrue(marker.exists())
        self.assertTrue((directory / 'cli-plugins').is_symlink())
        self.assert_user_config_unchanged()

        # Cleanup must refuse the user's permanent Docker configuration.
        result = self.run_shell(shell_body(self.cleanup_step), DOCKER_CONFIG=str(self.user_docker))
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertNotIn(FAKE_TOKEN, result.stdout + result.stderr)
        self.assert_user_config_unchanged()

    def test_mac_login_disables_post_logout_and_uses_the_generated_github_token(self):
        login = [step for step in mac_steps() if 'uses: docker/login-action@' in step]
        self.assertEqual(1, len(login))
        self.assertRegex(login[0], r'(?m)^          logout: false\s*$')
        self.assertRegex(login[0], r'(?m)^          registry: ghcr\.io\s*$')
        self.assertIn('password: ${{ secrets.GITHUB_TOKEN }}', login[0])
        self.assertIn('if: always()', self.cleanup_step)


if __name__ == '__main__':
    unittest.main()
