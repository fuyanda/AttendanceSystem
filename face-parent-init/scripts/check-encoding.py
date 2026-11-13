#!/usr/bin/env python3
"""Verify face-parent-init source files are valid UTF-8 without BOM."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SKIP_DIRS = {'.git', 'node_modules', 'dist', 'build'}
EXTS = {'.vue', '.js', '.ts', '.json', '.html', '.css', '.scss', '.md', '.mdc', '.env'}
BAD_PATTERNS = [
    (re.compile(r'\?\?\?'), 'contains ???'),
    (re.compile(r'\ufffd'), 'contains replacement char U+FFFD'),
    (re.compile(r'[\u0400-\u04ff]'), 'possible mojibake (Cyrillic)'),
]


def iter_files() -> list[Path]:
    files: list[Path] = []
    for path in ROOT.rglob('*'):
        if not path.is_file():
            continue
        if path.name.startswith('.env') or path.suffix in EXTS:
            pass
        else:
            continue
        if any(part in SKIP_DIRS for part in path.parts):
            continue
        files.append(path)
    return sorted(files)


def check_file(path: Path) -> list[str]:
    errors: list[str] = []
    raw = path.read_bytes()
    if raw.startswith(b'\xef\xbb\xbf'):
        errors.append('UTF-8 BOM detected')
    try:
        text = raw.decode('utf-8')
    except UnicodeDecodeError as exc:
        errors.append(f'invalid UTF-8: {exc}')
        return errors
    for pattern, message in BAD_PATTERNS:
        if pattern.search(text):
            errors.append(message)
            break
    return errors


def main() -> int:
    all_files = iter_files()
    failed: list[tuple[Path, list[str]]] = []
    for path in all_files:
        issues = check_file(path)
        if issues:
            failed.append((path, issues))
    if failed:
        print('Encoding check FAILED:')
        for path, issues in failed:
            rel = path.relative_to(ROOT)
            for issue in issues:
                print(f'  {rel}: {issue}')
        print(f'\n{len(failed)} file(s) with issues.')
        return 1
    print(f'Encoding check OK ({len(all_files)} files).')
    return 0


if __name__ == '__main__':
    sys.exit(main())
