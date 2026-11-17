"""YAML loading and validation for POLARIS intent documents."""

from pathlib import Path
from typing import Any

import yaml
from pydantic import ValidationError

from polaris.models import NetworkIntent


class IntentParseError(ValueError):
    """Raised when an intent document cannot be parsed or validated."""


def parse_intent(text: str) -> NetworkIntent:
    """Parse and validate an intent document from YAML text."""
    try:
        raw: Any = yaml.safe_load(text)
    except yaml.YAMLError as exc:
        raise IntentParseError(f"Invalid YAML: {exc}") from exc

    if not isinstance(raw, dict):
        raise IntentParseError("The YAML document must contain a mapping at its root")

    try:
        return NetworkIntent.model_validate(raw)
    except ValidationError as exc:
        raise IntentParseError(f"Invalid network intent: {exc}") from exc


def load_intent(path: str | Path) -> NetworkIntent:
    """Read and validate an intent document from a UTF-8 file."""
    intent_path = Path(path)
    try:
        content = intent_path.read_text(encoding="utf-8")
    except OSError as exc:
        raise IntentParseError(f"Cannot read intent file {intent_path}: {exc}") from exc

    return parse_intent(content)
