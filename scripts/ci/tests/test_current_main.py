import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('current_main', Path(__file__).parents[1] / 'require-current-main.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class CurrentMainTest(unittest.TestCase):
    def test_only_current_main_of_expected_repository_is_accepted(self):
        module.verify('yellow-pang/health-center-smart-reservation', 'refs/heads/main', 'a' * 40, 'a' * 40)

    def test_stale_rerun_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'outdated'):
            module.verify('yellow-pang/health-center-smart-reservation', 'refs/heads/main', 'a' * 40, 'b' * 40)

    def test_other_branches_and_forks_are_rejected(self):
        for repository, ref in [('fork/health-center', 'refs/heads/main'),
                                ('yellow-pang/health-center-smart-reservation', 'refs/heads/dev')]:
            with self.subTest(repository=repository, ref=ref), self.assertRaises(ValueError):
                module.verify(repository, ref, 'a' * 40, 'a' * 40)
