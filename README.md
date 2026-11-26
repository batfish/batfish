# POLARIS

### Policy-Oriented Logical Analysis and Risk Inspection System

**Understand. Analyze. Secure.**

POLARIS is an experimental network-security research project built on top of [Batfish](https://github.com/batfish/batfish). It explores additional ways to inspect network security policies, reason about network reachability, explain security exposure, and evaluate low-impact policy corrections.

> **Project status:** Early-stage research and development. The capabilities described as research objectives below are planned work unless explicitly documented as implemented and tested.

## Why POLARIS?

Network configurations combine routing, access-control lists, firewall rules, and segmentation policies. Their interactions can create unintended communication paths or overly broad access, even when individual rules appear reasonable.

POLARIS aims to help network engineers reason about these interactions before deployment by combining Batfish's configuration-analysis foundation with additional security-oriented analysis.

## Research objectives

- **Graph-based reachability analysis** — represent relevant network entities and permitted communication relationships to inspect possible paths.
- **Security policy analysis** — investigate unintended reachability, overly permissive rules, shadowed rules, and policy conflicts.
- **Explainable risk assessment** — provide traceable explanations of why a flow is considered risky, including the relevant path and policy conditions.
- **Minimal-impact policy remediation** — research candidate policy changes that block unauthorized communication while preserving required legitimate flows.
- **Independent verification** — use Batfish analysis to validate network behavior and assess proposed changes where supported.

## Research direction: risk-aware policy repair

A central research direction is a risk-aware minimal policy repair method.

Given:
1. a network configuration,
2. a set of intended communication requirements, and
3. one or more detected policy violations,

the method will explore candidate changes and evaluate them against criteria such as:

- whether the unauthorized flow is blocked;
- how many policy rules must change;
- whether legitimate communication is disrupted;
- how broad the proposed change is; and
- what potential side effects it introduces.

The goal is to identify and explain a low-impact correction, then re-check the resulting policy model. The algorithm, evaluation methodology, and measured results will be documented as implementation progresses.

## Relationship with Batfish

POLARIS is built upon the open-source [Batfish](https://github.com/batfish/batfish) project. Batfish analyzes network-device configurations and models network behavior to support security, reliability, compliance, and change-validation workflows.

POLARIS does not claim to replace Batfish. The project intends to extend and complement its existing capabilities with additional research components focused on security-policy reasoning and remediation.

- Original project: [batfish/batfish](https://github.com/batfish/batfish)
- Python client: [batfish/pybatfish](https://github.com/batfish/pybatfish)
- Original documentation: [Batfish documentation](https://batfish.readthedocs.io/)

## Development principles

- Preserve the upstream project's licensing, copyright notices, and required attribution.
- Keep POLARIS-specific code and changes clearly documented.
- Prefer independently testable components over invasive changes to the Batfish core.
- Validate findings against reproducible test scenarios.
- Distinguish implemented functionality from planned research objectives.
- Report limitations and false positives rather than overstating guarantees.

## Current status

This repository is currently an early-stage fork and research workspace. The original Batfish codebase remains the technical foundation. POLARIS-specific algorithms, interfaces, experiments, and documentation will be added incrementally.

No research objective should be considered implemented until its code, tests, and evaluation results are available in this repository.

## Intended technology areas

- Java and the Batfish codebase
- Python and PyBatfish
- Graph theory and network reachability
- Network security policy analysis
- Automated testing and reproducible experiments

The final implementation choices may evolve as the architecture and feasibility are assessed.

## Contributing and feedback

This is currently an individual research and development project. Issues and technical feedback are welcome, especially around reproducible network-policy scenarios, verification methodology, and algorithm evaluation.

## License and attribution

POLARIS is a fork of [Batfish](https://github.com/batfish/batfish), which is distributed under the Apache License 2.0. The upstream license and copyright notices must be retained. See the repository's [LICENSE](LICENSE) and upstream project for the applicable terms.
