# POLARIS Core

Standalone Python research core for network security intent modeling and policy analysis.

## Current status

The initial implementation focuses on validated intent data and YAML parsing. It does not yet perform network reachability analysis, remediation, configuration editing, or Batfish verification.

## Development setup

Requires Python 3.11 or newer.

```bash
cd polaris
python -m venv .venv
# Windows:
.venv\Scripts\activate
# Linux/macOS:
# source .venv/bin/activate

python -m pip install -e ".[dev]"
pytest
```

## Intent format

```yaml
network:
  name: enterprise-lab

zones:
  USERS: 192.168.10.0/24
  APP: 192.168.30.0/24
  DATABASE: 192.168.40.0/24

requirements:
  - id: REQ-001
    source: USERS
    destination: APP
    protocol: TCP
    ports: [443]
    expectation: MUST_ALLOW
  - id: REQ-002
    source: USERS
    destination: DATABASE
    protocol: TCP
    ports: [5432]
    expectation: MUST_DENY
```

An omitted `ports` field means all ports for the selected protocol. For `ANY`, ports must be omitted. No implicit default-allow or default-deny behavior is inferred: requirements describe only the flows explicitly declared.

## Design boundaries

- The core can load and validate intent without a running Batfish service.
- Actual network behavior will be supplied by a separate adapter or deterministic test fixtures.
- Batfish remains responsible for its existing network modeling and symbolic reachability capabilities.
- The core does not deploy or modify device configurations.
