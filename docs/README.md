# Batfish Documentation

This documentation is primarily aimed at developers interested in understanding and improving Batfish. If you are interested in trying out Batfish on your network, check out our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html) instead.

## Project Overview

Batfish is a network validation tool that provides correctness guarantees for security, reliability, and compliance by analyzing the configuration of network devices. It builds complete models of network behavior from device configurations and finds violations of network policies (built-in, user-defined, and best-practices).

### Core Mission

Batfish enables network engineers to validate configuration changes _before_ deployment, closing a critical gap in existing network automation workflows. By including Batfish in automation workflows, network engineers can ensure that only correct changes are deployed.

### Key Problems Solved

- **Configuration Complexity**: Modern networks involve numerous devices with complex, interdependent configurations
- **High Cost of Errors**: Misconfigurations can lead to outages, security breaches, and compliance violations
- **Limited Testing**: Traditional network testing methods are manual, time-consuming, and often incomplete
- **Vendor Diversity**: Networks typically include devices from multiple vendors with different configuration languages

---

## Quick Links

**New to Batfish?**
- [Quick Reference](quick_reference.md) - Common commands and patterns
- [User Guide](user_guide/README.md) - Usage instructions and best practices

**Developing Batfish:**
- [Development Guide](development/README.md) - Setup and contribution guidelines
- [PR Preparation Guide](development/pr_preparation.md) - Step-by-step PR workflow
- [Building and Running](building_and_running/README.md) - Build system details

**Understanding Internals:**
- [Architecture](architecture/README.md) - System design and components
- [Parsing](parsing/README.md) - How configurations are parsed
- [Data Plane](data_plane/README.md) - How data plane is computed
- [Symbolic Engine](symbolic_engine/README.md) - BDD-based analysis

---

## Documentation Structure

### Configuration Processing Pipeline

Batfish analyzes network configurations through a multi-stage pipeline:

1. **[Parsing](parsing/README.md)** - Convert vendor configs to parse trees
   - [Implementation Guide](parsing/implementation_guide.md)
   - [Parser Rule Conventions](parsing/parser_rule_conventions.md)
   - [Lexer Mode Patterns](parsing/lexer_mode_patterns.md)
   - [Vendor Guides](parsing/vendors/)

2. **[Extraction](extraction/README.md)** - Extract vendor-independent configs
   - BatfishListener patterns
   - Error handling and warnings
   - Multi-file processing

3. **[Conversion](conversion/README.md)** - Convert to vendor-independent model
   - Conversion patterns
   - Warnings and validation
   - Finalization

4. **[Post-processing](post_processing/README.md)** - Finalize configurations
   - Incremental computation
   - Error correction
   - Validation

### Analysis Components

After processing configurations, Batfish performs analysis:

**[Data Plane](data_plane/README.md)** - Compute routing and forwarding state
- IBDP algorithm
- Route types and protocols
- Oscillation detection

**[Symbolic Engine](symbolic_engine/README.md)** - BDD-based comprehensive analysis
- Packet representation
- Reachability analysis
- NAT encoding

**[Topology](topology/README.md)** - Compute network topology
- Layer 1/2/3 topologies
- Overlay networks
- Protocol topologies

**[Forwarding Analysis](forwarding_analysis/README.md)** - Analyze packet forwarding
- FIB computation
- Flow dispositions
- ARP analysis

**[Flow Dispositions](flow_dispositions/README.md)** - All possible flow outcomes
- Disposition types
- When each occurs
- Troubleshooting

### Development

**[Development](development/README.md)** - Getting started as a developer
- [PR Preparation Guide](development/pr_preparation.md) ⭐ NEW - Complete PR workflow
- [Coding Standards](development/coding_standards.md) - Style guidelines
- [Testing Guide](development/testing_guide.md) - Testing practices
- [Git Workflow](development/git_workflow.md) - Version control
- [BDD Best Practices](development/bdd_best_practices.md) - Working with BDDs

**[Building and Running](building_and_running/README.md)** - Build system
- Bazel commands
- Running tests
- Development workflow

**[IntelliJ Setup](intellij_setup/README.md)** - IDE configuration

### Architecture

**[Architecture](architecture/README.md)** - System design
- [Pipeline Overview](architecture/pipeline_overview.md) - How components fit together

### User-facing

**[User Guide](user_guide/README.md)** - Usage instructions
- [Best Practices](user_guide/best_practices.md)
- [Use Cases](user_guide/use_cases.md)

**[Contributing](contributing/README.md)** - Contribution guidelines

**[Question Development](question_development/README.md)** - Adding new questions

### Proposals

**[Proposals](proposals/)** - Design discussions for new features
- [Extensible L3 Adjacencies](proposals/extensible_l3_adjacencies/README.md)

### Other

**[Active Development](active_development/README.md)** - Current roadmap and known issues
**[Quick Reference](quick_reference.md)** ⭐ NEW - Common commands and patterns
**[AWS Network Modeling](aws_network_modeling.md)** - AWS-specific details
**[Azure](azure/)** - Azure-specific documentation

---

## Common Workflows

### For Users

**Verify network behavior:**
1. Create snapshot: `bf.init_snapshot()`
2. Run reachability analysis: `bf.reachability()`
3. Check for violations
4. See: [User Guide](user_guide/README.md), [Quick Reference](quick_reference.md)

**Validate configuration changes:**
1. Create baseline snapshot
2. Make changes, create test snapshot
3. Run differential analysis: `bf.differentialReachability()`
4. Verify intended effects + no regressions
5. See: [Quick Reference](quick_reference.md)

### For Developers

**Add new parser support:**
1. Read [Parsing Guide](parsing/README.md)
2. Create lexer/parser grammars
3. Implement extractor
4. Add tests
5. See: [Implementation Guide](parsing/implementation_guide.md)

**Work on data plane:**
1. Read [Data Plane Guide](data_plane/README.md)
2. Understand IBDP algorithm
3. Modify routing logic
4. Test with networks
5. See: [Development Guide](development/README.md)

**Add symbolic analysis:**
1. Read [Symbolic Engine Guide](symbolic_engine/README.md)
2. Understand BDD operations
3. Follow [BDD Best Practices](development/bdd_best_practices.md)
4. Test memory management
5. See: [Testing Guide](development/testing_guide.md)

**Contribute PR:**
1. Set up: [Development Guide](development/README.md)
2. Make changes and test
3. Follow: [PR Preparation Guide](development/pr_preparation.md) ⭐
4. Submit PR
5. See: [Contributing](contributing/README.md)

---

## Getting Help

**Documentation:**
- Start with [Quick Reference](quick_reference.md) for common tasks
- Check topic-specific guides (see above)
- Read [Architecture](architecture/README.md) for system overview

**Community:**
- [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTcyYzY3M2Q0NWUyYTRhYjdlM2IzYzRhZGU1NWFlNGU2MzlhNDY3OTJmMDIyMjQzYmRlNjhkMTRjNWIwNTUwNTQ) - Ask questions
- [GitHub Issues](https://github.com/batfish/batfish/issues) - Report bugs
- [Pybatfish Documentation](https://pybatfish.readthedocs.io/) - Python API docs

---

## Documentation Overview

| Area | Documentation | Status |
|------|--------------|--------|
| **Quick Start** | [Quick Reference](quick_reference.md) ⭐ | Comprehensive |
| **Parsing** | [Parsing README](parsing/README.md) | Comprehensive |
| **Extraction** | [Extraction README](extraction/README.md) | Comprehensive |
| **Conversion** | [Conversion README](conversion/README.md) | Comprehensive |
| **Post-processing** | [Post-processing README](post_processing/README.md) | Comprehensive |
| **Data Plane** | [Data Plane README](data_plane/README.md) ⭐ | Comprehensive |
| **Symbolic Engine** | [Symbolic Engine README](symbolic_engine/README.md) ⭐ | Comprehensive |
| **Topology** | [Topology README](topology/README.md) | Comprehensive |
| **Forwarding** | [Forwarding Analysis README](forwarding_analysis/README.md) | Comprehensive |
| **Flow Dispositions** | [Flow Dispositions README](flow_dispositions/README.md) | Basic |
| **PR Workflow** | [PR Preparation](development/pr_preparation.md) ⭐ | Comprehensive |
| **BDD Usage** | [BDD Best Practices](development/bdd_best_practices.md) | Comprehensive |

⭐ = Recently expanded/added
