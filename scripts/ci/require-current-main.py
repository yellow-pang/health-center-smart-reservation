#!/usr/bin/env python3
"""Fail before publishing/deploying a stale main run (including manual reruns)."""
import json
import os
import re
import urllib.request


def verify(repository, ref, expected_sha, actual_sha):
    if repository != 'yellow-pang/health-center-smart-reservation':
        raise ValueError('Deployment is restricted to the Health Center repository')
    if ref != 'refs/heads/main' or not re.fullmatch(r'[0-9a-f]{40}', expected_sha):
        raise ValueError('Deployment requires a main commit SHA')
    if expected_sha != actual_sha:
        raise ValueError('This run is outdated: main has advanced. Deploy the current main run.')


def main():
    repository = os.environ['GITHUB_REPOSITORY']
    # Validate before using a repository name in the network request.
    verify(repository, os.environ['GITHUB_REF'], os.environ['GITHUB_SHA'], os.environ['GITHUB_SHA'])
    request = urllib.request.Request(
        f'https://api.github.com/repos/{repository}/git/ref/heads/main',
        headers={'Accept': 'application/vnd.github+json',
                 'Authorization': 'Bearer ' + os.environ['GH_TOKEN'],
                 'X-GitHub-Api-Version': '2022-11-28'},
    )
    with urllib.request.urlopen(request, timeout=20) as response:
        actual_sha = json.load(response)['object']['sha']
    verify(repository, os.environ['GITHUB_REF'], os.environ['GITHUB_SHA'], actual_sha)
    print('Current main commit verified.')


if __name__ == '__main__':
    main()
