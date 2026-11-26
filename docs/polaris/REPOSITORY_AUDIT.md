# POLARIS Repository Audit

**Audit date:** 2026-11-17  
**Repository:** `M-Arbaoui/POLARIS`  
**Audited branch:** `master`  
**Development branch:** `polaris/development`  
**Base commit:** `7c4e32ba0617cbeadaf3cc87170ea9276626ad8a`  
**Audit status:** Initial source-level audit; not a complete build or runtime validation.

## 1. Executive summary

POLARIS currently exists as a fork of the Batfish repository. The repository contains the upstream Batfish codebase and project documentation, but no dedicated POLARIS implementation package was found during this audit.

The selected architecture is:

- A standalone Python POLARIS research core.
- An optional PyBatfish adapter for querying real network snapshots.
- Batfish as the source of truth for modeled network behavior and independent verification.
- A POLARIS-specific policy dependency model for remediation reasoning and impact analysis.

The initial implementation should not replace Batfish's reachability engine or make invasive changes to Batfish's Java core.

## 2. Repository structure observed

The recursive Git tree contained 11,423 entries at the time of inspection. Relevant top-level areas include:

- `projects/`: Batfish Java modules, including `batfish`, `bdd`, `client`, `common`, `coordinator`, and `question`.
- `python/`: existing Python tooling and dependency-management files.
- `questions/`: question templates consumed by Batfish.
- `tests/`: existing test infrastructure and network scenarios.
- `networks/`: network-related repository assets.
- `docs/`: upstream documentation, including question-development guides.

No `polaris/` package or `docs/polaris/` documentation tree existed in the audited master snapshot.

The existing `python/requirements.in` contains `xmltodict==0.12.0`; the existing `python/README.md` documents a pip-tools workflow. The existing `python/BUILD.bazel` is minimal. This is not evidence that a new POLARIS package should be inserted into that directory.

## 3. Existing Batfish capabilities relevant to POLARIS

The following capabilities were confirmed from repository source or documentation inspected during this audit.

### 3.1 Symbolic reachability

File inspected:

`projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityAnalysis.java`

The class documents a graph of packet-processing states and transitions labeled with Binary Decision Diagrams (BDDs). The analysis computes packet sets that can reach a query state.

Implication for POLARIS:

- Do not rebuild equivalent network forwarding/reachability semantics using NetworkX.
- Use Batfish questions and results to obtain actual modeled network behavior.
- Restrict POLARIS graph modeling to security-policy dependencies, requirement relationships, candidate repairs, and impact relationships unless a distinct graph abstraction is justified.

### 3.2 Reachability question

File inspected:

`docs/question_development/reachability.md`

The documentation describes reachability queries, packet-header constraints, paths, and trace generation.

Implication:

- Investigate the existing reachability question and PyBatfish interface before designing a custom actual-reachability adapter.
- Preserve the distinction between a reachability result and an intent requirement.

### 3.3 ACL line reachability

File inspected:

`docs/question_development/filterLineReachability.md`

The documentation describes detection of unreachable ACL/filter lines, including lines shadowed by earlier rules, using BDD-based packet-space analysis.

Implication:

- POLARIS should consume this existing capability where appropriate.
- Avoid implementing a duplicate shadowed-rule detector unless POLARIS adds a clearly different analysis or explanation.

### 3.4 Filter testing

File inspected:

`docs/question_development/testFilters.md`

The documentation describes evaluating concrete flows against filters and identifying matching lines and evaluation traces.

Implication:

- This can support evidence gathering and targeted validation for supported filter scenarios.
- It is not a substitute for broad symbolic reachability analysis.

### 3.5 Question extension mechanism

File inspected:

`docs/question_development/README.md`

The documentation explains that PyBatfish questions are JSON templates wrapping Java-level questions. A new Java question generally involves a QuestionPlugin, Question, and Answerer, plus a corresponding template.

Implication:

- The initial POLARIS implementation should avoid adding a Java question.
- Prefer existing Batfish questions through PyBatfish.
- Reconsider a Java extension only if a required capability cannot be accessed adequately through existing questions or APIs.

## 4. Proposed architecture

### 4.1 POLARIS Core

A separate Python package should own:

1. Intent and requirement models.
2. Policy parsing and validation.
3. Normalized result models.
4. Security violation comparison.
5. Policy dependency graph.
6. RAMP candidate generation.
7. Impact and preservation constraints.
8. Remediation ranking/optimization.
9. Explanations and structured reports.
10. Reproducible experiments.

The core must support synthetic or abstract test scenarios without requiring a running Batfish service.

### 4.2 Batfish adapter

The adapter should be optional and responsible for:

- Connecting to a configured Batfish service.
- Selecting and initializing a network snapshot.
- Calling supported Batfish questions.
- Normalizing answers into POLARIS internal models.
- Preserving source evidence and query metadata.
- Returning explicit unsupported/error states rather than fabricating data.

The adapter must not silently reinterpret missing or inconclusive Batfish results as permitted or denied traffic.

### 4.3 Verification and configuration changes

Automated configuration editing is a later integration milestone, not a prerequisite for the first core prototype.

The first integration target should be a narrowly scoped, documented configuration family and a small set of reproducible snapshots. Cisco IOS extended ACLs are a candidate, subject to confirming the selected Batfish parsing and serialization workflow.

Any proposed configuration change must be applied to a disposable copy or a new snapshot, then re-analyzed. POLARIS must distinguish proposed, simulated, independently verified, and deployed changes.

## 5. RAMP research formulation

RAMP should be treated as a constrained policy-repair problem, not merely a weighted score.

Given:

- A set of unauthorized flows to block.
- A set of legitimate flows to preserve.
- Candidate policy modifications.
- The enforcement points at which modifications can be applied.

The optimizer should select a low-cost set of modifications subject to explicit security and preservation constraints.

Candidate evaluation may include:

- Unauthorized flows blocked.
- Legitimate flows disrupted.
- Number of rules modified.
- Breadth of each modification.
- Enforcement-point suitability.
- Potential side effects.

The initial implementation may use a deterministic greedy heuristic. Its limitations must be explicit. Evaluation should compare it against transparent baselines and, if practical, an exact or solver-backed formulation on small instances.

Do not claim global optimality without a proof or a suitable exact optimization result.

## 6. Initial implementation boundaries

### In scope

- Standalone Python package.
- YAML-based intent model.
- Strict validation and normalized data models.
- Abstract test fixtures with known expected outcomes.
- Batfish adapter using existing supported questions.
- Intent-versus-actual comparison.
- Policy dependency graph.
- RAMP candidate generation and impact analysis.
- CLI and structured reports.
- Automated tests and reproducible benchmarks.

### Deferred

- Changes to Batfish Java core.
- New Batfish Java questions.
- Automatic deployment to network devices.
- Multi-vendor configuration rewriting.
- React dashboard.
- Production orchestration.
- Machine-learning components without a justified research need.

## 7. Recommended implementation sequence

1. Define normalized models for zones, flows, requirements, observations, violations, and candidate repairs.
2. Implement YAML parsing and validation.
3. Implement deterministic abstract scenarios and ground-truth tests.
4. Implement the intent-versus-observed-behavior analyzer.
5. Implement policy dependency and impact relationships.
6. Implement RAMP candidate generation and a baseline greedy optimizer.
7. Add the optional PyBatfish adapter and integration fixtures.
8. Add one narrowly scoped configuration-change experiment if the mapping is feasible.
9. Run detection, remediation, and scalability evaluations.
10. Document limitations and reproducible results.

## 8. Open questions requiring runtime verification

This audit did not execute the Batfish build, start the all-in-one service, run PyBatfish queries, or validate configuration rewriting. The following remain to be tested:

- Exact PyBatfish version and compatibility with the selected Batfish service version.
- Available question parameters and answer schemas for the selected Batfish release.
- Runtime behavior of reachability, filter-line reachability, and test-filters on POLARIS fixtures.
- Resource requirements on the developer's laptop.
- A reliable workflow for creating and analyzing modified snapshots.
- Whether the chosen configuration format can be safely rendered and re-parsed for differential verification.
- Existing CI/build status on the current base commit.

No performance measurements or runtime compatibility claims are made in this document.

## 9. Decision record

| Decision | Status |
|---|---|
| Keep upstream Batfish source intact initially | Accepted |
| Build POLARIS as a separate Python package | Accepted |
| Keep Batfish integration optional | Accepted |
| Use Batfish for actual modeled reachability | Accepted |
| Use NetworkX only for POLARIS-specific dependency/impact graphs | Accepted |
| Make RAMP the central research contribution | Accepted |
| Start with CLI and tests; defer UI | Accepted |
| Start configuration rewriting with one platform only | Proposed; validate feasibility first |

## 10. Premortem

The most likely failure modes are:

1. Duplicating Batfish functionality instead of adding a distinct policy-repair contribution.
2. Spending too much time mapping abstract zones to vendor-specific configurations.
3. Treating incomplete analysis results as proof of reachability or isolation.
4. Calling a heuristic globally optimal without evidence.
5. Expanding into dashboards, deployment, or multi-vendor rewriting before the core is validated.

Mitigation: keep the first milestone limited to a tested intent model, a reliable analyzer, a small reproducible scenario set, and a measurable RAMP baseline.
