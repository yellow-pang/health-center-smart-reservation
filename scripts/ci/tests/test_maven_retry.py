import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


RETRY = Path(__file__).resolve().parents[3] / 'backend/scripts/maven-retry.sh'


class MavenRetryTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.bin = self.root / 'bin'
        self.bin.mkdir()
        self.log = self.root / 'commands.jsonl'
        self.responses = self.root / 'responses.json'
        self.env = dict(os.environ, PATH=str(self.bin) + os.pathsep + os.environ['PATH'],
                        MAVEN_TEST_LOG=str(self.log), MAVEN_TEST_RESPONSES=str(self.responses))
        self.write_tool('mvn', '''
import json, os, sys
from pathlib import Path
log = Path(os.environ['MAVEN_TEST_LOG'])
commands = [json.loads(line) for line in log.read_text().splitlines()] if log.exists() else []
attempt = sum(command['tool'] == 'mvn' for command in commands)
with log.open('a') as stream:
    stream.write(json.dumps({'tool': 'mvn', 'args': sys.argv[1:]}) + '\\n')
responses = json.loads(Path(os.environ['MAVEN_TEST_RESPONSES']).read_text())
if attempt >= len(responses):
    print('Unexpected extra Maven attempt', file=sys.stderr)
    raise SystemExit(98)
response = responses[attempt]
if response.get('requires_update') and '-U' not in sys.argv[1:]:
    print('Cached failed resolution was not refreshed', file=sys.stderr)
    raise SystemExit(97)
sys.stdout.write(response.get('stdout', ''))
sys.stderr.write(response.get('stderr', ''))
raise SystemExit(response['code'])
''')
        self.write_tool('sleep', '''
import json, os, sys
with open(os.environ['MAVEN_TEST_LOG'], 'a') as stream:
    stream.write(json.dumps({'tool': 'sleep', 'args': sys.argv[1:]}) + '\\n')
''')

    def write_tool(self, name, source):
        path = self.bin / name
        path.write_text(f'#!{sys.executable}\n' + source)
        path.chmod(0o700)

    def run_retry(self, responses, *args):
        self.responses.write_text(json.dumps(responses))
        if self.log.exists():
            self.log.unlink()
        return subprocess.run(['/bin/sh', str(RETRY), *args], env=self.env, cwd=self.root,
                              text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=10)

    def commands(self, tool):
        return [entry['args'] for entry in map(json.loads, self.log.read_text().splitlines())
                if entry['tool'] == tool]

    @staticmethod
    def transfer_error(reason):
        return ("[ERROR] Failed to execute goal org.apache.maven.plugins:maven-dependency-plugin:3.8.1:go-offline "
                "(default-cli) on project egovframe-boot-simple-backend: org.eclipse.aether.resolution.DependencyResolutionException: "
                "Failed to read artifact descriptor for org.egovframe.rte:egovframe-rte-fdl-property:jar:5.0.0: "
                "Could not transfer artifact org.egovframe.rte:egovframe-rte-fdl-property:pom:5.0.0 "
                "from/to egovframe2 (https://maven.egovframe.go.kr/maven/): " + reason + '\n')

    def test_first_success_preserves_caller_arguments_without_sleep(self):
        args = ['dependency:go-offline', '-DskipTests', '-Dexample=value with spaces']
        result = self.run_retry([{'code': 0, 'stdout': '[INFO] BUILD SUCCESS\n'}], *args)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual([['-B', '--no-transfer-progress', '-U', *args]], self.commands('mvn'))
        self.assertEqual([], self.commands('sleep'))
        self.assertIn('[INFO] BUILD SUCCESS\n', result.stdout + result.stderr)

    def test_descriptor_502_retries_refreshes_cache_and_retains_complete_logs(self):
        failure = self.transfer_error('status code: 502, reason phrase: Bad Gateway (502)')
        first_log = '[INFO] Resolving dependencies\n' + failure + '[INFO] BUILD FAILURE\n'
        second_log = '[INFO] Previously failed artifact downloaded\n[INFO] BUILD SUCCESS\n'
        result = self.run_retry([
            {'code': 1, 'stdout': first_log, 'stderr': 'First attempt diagnostic\n'},
            {'code': 0, 'stdout': second_log, 'requires_update': True},
        ], 'dependency:go-offline', '-DskipTests')
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual(2, len(self.commands('mvn')))
        self.assertEqual([['10']], self.commands('sleep'))
        output = result.stdout + result.stderr
        self.assertIn(first_log, output)
        self.assertIn('First attempt diagnostic\n', output)
        self.assertIn(second_log, output)
        self.assertLess(output.index(first_log), output.index(second_log))
        self.assertTrue(all('-U' in args for args in self.commands('mvn')))

    def test_persistent_transfer_failures_stop_at_three_and_preserve_last_exit(self):
        responses = [{'code': code, 'stderr': self.transfer_error(f'status code: {status}')}
                     for code, status in [(11, 500), (23, 503), (37, 504)]]
        result = self.run_retry(responses, 'package', '-DskipTests')
        self.assertEqual(37, result.returncode)
        self.assertEqual(3, len(self.commands('mvn')))
        self.assertEqual([['10'], ['20']], self.commands('sleep'))
        for response in responses:
            self.assertIn(response['stderr'], result.stdout + result.stderr)

    def test_deterministic_errors_fail_immediately(self):
        failures = [
            '[ERROR] COMPILATION ERROR: ReservationService.java:[10,5] cannot find symbol\n',
            self.transfer_error('status code: 404, reason phrase: Not Found (404)'),
            self.transfer_error('status code: 401, reason phrase: Unauthorized (401)'),
            '[ERROR] Application test failed: remote business API returned status code: 502\n',
        ]
        for failure in failures:
            with self.subTest(failure=failure):
                result = self.run_retry([{'code': 7, 'stderr': failure}], 'verify')
                self.assertEqual(7, result.returncode)
                self.assertEqual(1, len(self.commands('mvn')))
                self.assertEqual([], self.commands('sleep'))
                self.assertIn(failure, result.stdout + result.stderr)

    def test_package_metadata_429_uses_the_same_retry_policy(self):
        failure = ('[ERROR] Could not transfer metadata org.example:artifact/maven-metadata.xml '
                   'from/to central (https://repo.maven.apache.org/maven2): '
                   'status code: 429, reason phrase: Too Many Requests (429)\n')
        args = ['package', '-DskipTests', '-Dexample=value with spaces']
        result = self.run_retry([{'code': 1, 'stderr': failure}, {'code': 0}], *args)
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertEqual([['-B', '--no-transfer-progress', '-U', *args]] * 2, self.commands('mvn'))
        self.assertEqual([['10']], self.commands('sleep'))
        self.assertIn(failure, result.stdout + result.stderr)

    def test_unrelated_502_does_not_make_a_missing_artifact_retryable(self):
        failure = self.transfer_error('status code: 404, reason phrase: Not Found (404)')
        failure += '[ERROR] Application test failed: remote business API returned status code: 502\n'
        result = self.run_retry([{'code': 7, 'stderr': failure}], 'verify')
        self.assertEqual(7, result.returncode)
        self.assertEqual(1, len(self.commands('mvn')))
        self.assertEqual([], self.commands('sleep'))

    def test_transient_transfer_connections_and_http_408_are_retried(self):
        reasons = ['status code: 408, reason phrase: Request Timeout (408)',
                   'java.net.SocketTimeoutException: Read timed out',
                   'java.net.SocketException: Connection reset',
                   'java.net.ConnectException: Connection refused',
                   'java.net.UnknownHostException: maven.egovframe.go.kr']
        for reason in reasons:
            with self.subTest(reason=reason):
                result = self.run_retry([{'code': 1, 'stderr': self.transfer_error(reason)},
                                         {'code': 0}], 'dependency:go-offline')
                self.assertEqual(0, result.returncode, result.stderr)
                self.assertEqual(2, len(self.commands('mvn')))
                self.assertEqual([['10']], self.commands('sleep'))

    def test_interrupted_maven_does_not_retry_even_with_transfer_errors(self):
        for code in (130, 143):
            with self.subTest(exit_code=code):
                result = self.run_retry([{'code': code, 'stderr': self.transfer_error('Read timed out')}],
                                        'package')
                self.assertEqual(code, result.returncode)
                self.assertEqual(1, len(self.commands('mvn')))
                self.assertEqual([], self.commands('sleep'))


if __name__ == '__main__':
    unittest.main()
