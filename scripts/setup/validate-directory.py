#!/usr/bin/env python3
"""Reject shared roots, symlinks and occupied runner directories before setup."""
from pathlib import Path
import sys


def validate_directory(raw, mode, home=None, repository=None):
    path = Path(raw)
    home = Path.home() if home is None else Path(home)
    repository = Path(__file__).resolve().parents[2] if repository is None else Path(repository)
    shared_roots = {Path('/'), home.resolve(), repository.resolve(), *repository.resolve().parents}
    if not path.is_absolute() or path.resolve() in shared_roots:
        raise ValueError('Use a dedicated absolute directory, not a shared root, your home or the repository')
    # Check each existing ancestor too, so a symlink cannot redirect extraction or chmod.
    if any(part.is_symlink() for part in (path, *path.parents)):
        raise ValueError('Setup directory and its ancestors must not be symlinks')
    if path.exists() and not path.is_dir():
        raise ValueError('Setup directory path is occupied by a file')
    if mode == 'runner':
        if path.exists() and any(path.iterdir()):
            raise ValueError('Runner directory must be empty; do not overwrite an existing installation')
    elif mode == 'deploy':
        for name in ('shared', 'releases', 'backups'):
            child = path / name
            if child.is_symlink() or (child.exists() and not child.is_dir()):
                raise ValueError(f'Deployment {name} must be a regular directory')
        environment = path / 'shared' / 'production.env'
        if environment.is_symlink() or (environment.exists() and not environment.is_file()):
            raise ValueError('Existing production.env must be a regular file')
    else:
        raise ValueError('Unknown setup directory mode')


if __name__ == '__main__':
    try:
        validate_directory(sys.argv[2], sys.argv[1])
    except (ValueError, IndexError) as error:
        raise SystemExit(str(error))
