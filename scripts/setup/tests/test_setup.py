import importlib.util
import json
import os
from pathlib import Path
import stat
import subprocess
import sys
import tempfile
import unittest


SETUP = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('setup_directory', SETUP / 'validate-directory.py')
directory = importlib.util.module_from_spec(spec)
spec.loader.exec_module(directory)


class SetupTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name).resolve()
        self.bin = self.root / 'bin'
        self.bin.mkdir()
        self.log = self.root / 'commands.jsonl'
        self.responses = self.root / 'responses.json'
        self.env = dict(os.environ, PATH=str(self.bin) + os.pathsep + os.environ['PATH'],
                        MOCK_LOG=str(self.log), MOCK_RESPONSES=str(self.responses),
                        DEPLOY_ROOT=str(self.root / 'deployment'), RUNNER_ROOT=str(self.root / 'runner'))
        self.write_tool('uname', 'import sys\nprint("Darwin" if sys.argv[1] == "-s" else "arm64")')
        self.write_tool('id', 'print("1000")')
        self.write_tool('docker', '''
import json, os, sys
with open(os.environ['MOCK_LOG'], 'a') as stream:
    stream.write(json.dumps({'tool': 'docker', 'args': sys.argv[1:]}) + '\\n')
if sys.argv[1] == 'info': print('linux/arm64')
elif sys.argv[1] == 'inspect': print('healthy')
else: raise SystemExit('Unexpected Docker mutation')
''')
        self.write_tool('gh', '''
import json, os, sys
args = sys.argv[1:]
endpoint = next((a for a in args if a.startswith(('repos/', 'apps/'))), '')
method = args[args.index('--method') + 1] if '--method' in args else 'GET'
entry = {'tool': 'gh', 'method': method, 'endpoint': endpoint, 'args': args}
if '--input' in args:
    entry['payload'] = json.load(open(args[args.index('--input') + 1]))
with open(os.environ['MOCK_LOG'], 'a') as stream:
    stream.write(json.dumps(entry) + '\\n')
responses = json.load(open(os.environ['MOCK_RESPONSES']))
status, response = responses.get(endpoint, [403, {}]) if method == 'GET' else [200, {}]
if status != 200:
    print(f'gh: rejected (HTTP {status})', file=sys.stderr)
    raise SystemExit(1)
if '--jq' in args: print(response['approval_policy'])
else: print(json.dumps(response))
''')
        self.prefix = 'repos/yellow-pang/health-center-smart-reservation'

    def write_tool(self, name, code):
        path = self.bin / name
        path.write_text(f'#!{sys.executable}\n' + code + '\n')
        path.chmod(0o700)

    def run_script(self, name, *args):
        return subprocess.run(['bash', str(SETUP / name), *args], env=self.env,
                              text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    def commands(self):
        return [json.loads(line) for line in self.log.read_text().splitlines()] if self.log.exists() else []

    def writes(self):
        return [entry for entry in self.commands() if entry.get('method', 'GET') != 'GET']

    def github_state(self, environment=None, protection=None, policies=None, approval='first_time_contributors'):
        state = {
            f'{self.prefix}/contents/.github/workflows/ci.yml?ref=dev': [200, {}],
            'apps/github-actions': [200, {'id': 15368, 'slug': 'github-actions'}],
            f'{self.prefix}/environments/production': [404, {}] if environment is None else [200, environment],
            f'{self.prefix}/branches/main/protection': [404, {}] if protection is None else [200, protection],
            f'{self.prefix}/environments/production/deployment-branch-policies?per_page=100':
                [200, {'branch_policies': policies or []}],
            f'{self.prefix}/actions/permissions/fork-pr-contributor-approval': [200, {'approval_policy': approval}],
        }
        self.responses.write_text(json.dumps(state))
        return state

    @staticmethod
    def protection():
        return {'required_status_checks': {'strict': True, 'checks': [{'context': 'CI required', 'app_id': 15368}]},
                'enforce_admins': {'enabled': True}, 'required_pull_request_reviews': {'required_approving_review_count': 2},
                'allow_force_pushes': {'enabled': False}, 'allow_deletions': {'enabled': False}}

    @staticmethod
    def environment():
        return {'deployment_branch_policy': {'protected_branches': False, 'custom_branch_policies': True},
                'protection_rules': [{'type': 'required_reviewers', 'reviewers': [{'id': 123}]}]}

    def test_new_github_configuration_binds_check_and_gates_all_external_prs(self):
        self.github_state()
        result = self.run_script('configure-github.sh', '--apply')
        self.assertEqual(0, result.returncode, result.stderr)
        writes = self.writes()
        self.assertEqual(4, len(writes))
        self.assertIn('fork-pr-contributor-approval', writes[0]['endpoint'])
        self.assertIn('approval_policy=all_external_contributors', writes[0]['args'])
        self.assertEqual([{'context': 'CI required', 'app_id': 15368}],
                         writes[1]['payload']['required_status_checks']['checks'])

    def test_compatible_existing_settings_are_not_overwritten(self):
        self.github_state(self.environment(), self.protection(), [{'name': 'main', 'type': 'branch'}],
                          'all_external_contributors')
        result = self.run_script('configure-github.sh', '--apply')
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual([], self.writes())

    def test_incompatible_environment_fails_before_any_writes(self):
        self.github_state({'deployment_branch_policy': {'protected_branches': True, 'custom_branch_policies': False}})
        result = self.run_script('configure-github.sh', '--apply')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual([], self.writes())

    def test_existing_protection_without_app_binding_fails_before_any_writes(self):
        protection = self.protection()
        protection['required_status_checks']['checks'][0]['app_id'] = None
        self.github_state(protection=protection)
        result = self.run_script('configure-github.sh', '--apply')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual([], self.writes())

    def test_extra_environment_branch_fails_before_any_writes(self):
        self.github_state(self.environment(), policies=[{'name': '*', 'type': 'branch'}])
        result = self.run_script('configure-github.sh', '--apply')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual([], self.writes())

    def test_read_denial_is_not_treated_as_missing_configuration(self):
        state = self.github_state()
        state[f'{self.prefix}/environments/production'] = [403, {}]
        self.responses.write_text(json.dumps(state))
        result = self.run_script('configure-github.sh', '--apply')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual([], self.writes())

    def test_unknown_fork_approval_policy_fails_closed(self):
        self.github_state(approval='unknown-policy')
        result = self.run_script('configure-github.sh', '--apply')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual([], self.writes())

    def test_runner_install_does_not_overwrite_occupied_directory(self):
        runner = Path(self.env['RUNNER_ROOT'])
        runner.mkdir()
        original = runner / 'existing-file'
        original.write_text('preserve me')
        result = self.run_script('install-mac-runner.sh')
        self.assertNotEqual(0, result.returncode)
        self.assertEqual('preserve me', original.read_text())
        self.assertEqual([], self.commands())

    def test_paths_reject_shared_roots_and_symlinks(self):
        for path in ('/', str(Path.home()), str(SETUP.parents[1]), str(SETUP.parents[2]), 'relative/path'):
            with self.subTest(path=path), self.assertRaises(ValueError):
                directory.validate_directory(path, 'deploy')
        link = self.root / 'linked'
        link.symlink_to(self.bin, target_is_directory=True)
        with self.assertRaises(ValueError):
            directory.validate_directory(str(link / 'child'), 'runner')

    def prepare_source(self):
        source = self.root / 'source.env'
        source.write_text('EXAMPLE=private-value\n')
        source.chmod(0o600)
        return source

    def test_prepare_requires_explicit_adoption_flag(self):
        source = self.prepare_source()
        result = self.run_script('prepare-mac.sh', str(source))
        self.assertEqual(0, result.returncode, result.stderr)
        shared = Path(self.env['DEPLOY_ROOT']) / 'shared'
        self.assertFalse((shared / 'allow-initial-adoption').exists())
        self.assertEqual(source.read_text(), (shared / 'production.env').read_text())
        self.assertEqual(0o600, stat.S_IMODE((shared / 'production.env').stat().st_mode))

    def test_prepare_authorizes_once_without_changing_containers(self):
        source = self.prepare_source()
        result = self.run_script('prepare-mac.sh', '--allow-initial-adoption', str(source))
        self.assertEqual(0, result.returncode, result.stderr)
        marker = Path(self.env['DEPLOY_ROOT']) / 'shared/allow-initial-adoption'
        self.assertTrue(marker.is_file())
        self.assertEqual(0o600, stat.S_IMODE(marker.stat().st_mode))
        self.assertEqual(['info', 'inspect'], [entry['args'][0] for entry in self.commands()])
        result = self.run_script('prepare-mac.sh', '--allow-initial-adoption', str(source))
        self.assertEqual(0, result.returncode, result.stderr)

    def test_prepare_refuses_adoption_of_managed_release(self):
        source = self.prepare_source()
        deploy = Path(self.env['DEPLOY_ROOT'])
        deploy.mkdir()
        (deploy / 'current').symlink_to(deploy / 'releases/missing')
        result = self.run_script('prepare-mac.sh', '--allow-initial-adoption', str(source))
        self.assertNotEqual(0, result.returncode)
        self.assertFalse((deploy / 'shared').exists())
        self.assertEqual([], self.commands())

    def test_prepare_preserves_existing_environment(self):
        source = self.prepare_source()
        shared = Path(self.env['DEPLOY_ROOT']) / 'shared'
        shared.mkdir(parents=True)
        environment = shared / 'production.env'
        environment.write_text('EXISTING=preserved\n')
        environment.chmod(0o600)
        result = self.run_script('prepare-mac.sh', str(source))
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual('EXISTING=preserved\n', environment.read_text())


if __name__ == '__main__':
    unittest.main()
