"""Command-line interface for the POLARIS core."""

import argparse
import sys
from pathlib import Path

from polaris.parser import IntentParseError, load_intent


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="polaris", description="POLARIS intent tools")
    subparsers = parser.add_subparsers(dest="command", required=True)

    validate = subparsers.add_parser("validate", help="validate a YAML intent file")
    validate.add_argument("file", type=Path, help="path to the intent YAML file")
    return parser


def main() -> int:
    args = build_parser().parse_args()

    if args.command == "validate":
        try:
            intent = load_intent(args.file)
        except IntentParseError as exc:
            print(f"Invalid intent: {exc}", file=sys.stderr)
            return 2

        print(
            f"Valid intent: {intent.network.name} "
            f"({len(intent.zones)} zones, {len(intent.requirements)} requirements)"
        )
        return 0

    return 2


if __name__ == "__main__":
    raise SystemExit(main())
