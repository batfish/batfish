from pathlib import Path

import pytest

from polaris.models import Expectation, Protocol
from polaris.parser import IntentParseError, load_intent, parse_intent


EXAMPLE = Path(__file__).parents[1] / "examples" / "enterprise.yaml"


def test_loads_enterprise_example() -> None:
    intent = load_intent(EXAMPLE)

    assert intent.network.name == "enterprise-lab"
    assert len(intent.zones) == 5
    assert len(intent.requirements) == 5
    assert intent.requirements[0].protocol is Protocol.TCP
    assert intent.requirements[0].expectation is Expectation.MUST_ALLOW


def test_rejects_unknown_zone_reference() -> None:
    document = """
network:
  name: test
zones:
  USERS: 192.168.1.0/24
requirements:
  - id: REQ-1
    source: USERS
    destination: MISSING
    protocol: TCP
    expectation: MUST_DENY
"""
    with pytest.raises(IntentParseError, match="unknown destination zone"):
        parse_intent(document)


def test_rejects_duplicate_requirement_ids() -> None:
    document = """
network:
  name: test
zones:
  USERS: 192.168.1.0/24
requirements:
  - id: REQ-1
    source: USERS
    destination: USERS
    protocol: TCP
    expectation: MUST_DENY
  - id: REQ-1
    source: USERS
    destination: USERS
    protocol: UDP
    expectation: MUST_DENY
"""
    with pytest.raises(IntentParseError, match="duplicate requirement id"):
        parse_intent(document)


def test_rejects_ports_for_any_protocol() -> None:
    document = """
network:
  name: test
zones:
  USERS: 192.168.1.0/24
requirements:
  - id: REQ-1
    source: USERS
    destination: USERS
    protocol: ANY
    ports: [443]
    expectation: MUST_DENY
"""
    with pytest.raises(IntentParseError, match="ports are supported only"):
        parse_intent(document)


def test_rejects_unknown_fields() -> None:
    document = """
network:
  name: test
  unexpected: true
zones:
  USERS: 192.168.1.0/24
requirements:
  - id: REQ-1
    source: USERS
    destination: USERS
    protocol: TCP
    expectation: MUST_DENY
"""
    with pytest.raises(IntentParseError, match="unexpected"):
        parse_intent(document)
