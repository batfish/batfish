# Routing Policy Invariants Documentation

## Overview

This directory contains comprehensive documentation for BGP attribute handling properties in routing policy invariants - a critical component of Batfish's routing policy analysis framework. These invariants provide precise control over how BGP attributes are accessed and modified during routing policy evaluation, enabling accurate modeling of diverse vendor behaviors.

## Documentation Structure

### Core Documentation

- **[BGP Attribute Handling](bgp_attribute_handling.md)**: Master documentation covering the complete BGP attribute handling system, including technical specifications, architectural rationale, and usage guidelines.

### Vendor-Specific Implementations

The `vendor_implementations/` directory contains detailed documentation for major network vendors:

- **[Cisco Implementation](vendor_implementations/cisco.md)**: Cisco's "match against what you received" semantics with route-map processing
- **[Juniper Implementation](vendor_implementations/juniper.md)**: Juniper's "match against what you're building" semantics with policy-statement processing
- **[Arista Implementation](vendor_implementations/arista.md)**: Arista EOS implementation with Cisco compatibility plus enhanced features

### Operational Documentation

- **[Troubleshooting Guide](troubleshooting_guide.md)**: Comprehensive troubleshooting techniques, common issues, debugging methodologies, and resolution strategies
- **[API Reference](api_reference.md)**: Detailed API specifications for the three core properties, including code examples and integration patterns

## Quick Start

### Understanding the Core Properties

The BGP attribute handling system is built around three boolean properties in the [`Environment`](../../projects/batfish-common-protocol/src/main/java/org/batfish/datamodel/routing_policy/Environment.java) class:

1. **`useOutputAttributes`**: Controls whether to use output (modified) attributes for matching
2. **`readFromIntermediateBgpAttributes`**: Controls reading from intermediate attribute state
3. **`writeToIntermediateBgpAttributes`**: Controls writing to intermediate attribute state

### Common Configuration Patterns

**Cisco/Arista (Match Original)**:

```java
Environment ciscoEnv = Environment.builder()
    .setUseOutputAttributes(false)
    .setReadFromIntermediateBgpAttributes(false)
    .setWriteToIntermediateBgpAttributes(false)
    .build();
```

**Juniper (Match Modified)**:

```java
Environment juniperEnv = Environment.builder()
    .setUseOutputAttributes(true)
    .setReadFromIntermediateBgpAttributes(false)
    .setWriteToIntermediateBgpAttributes(false)
    .build();
```

## Key Concepts

### Routing Policy Invariants

Routing policy invariants are properties that remain consistent throughout policy evaluation, providing:

- **Semantic Consistency**: Unified behavior modeling across vendor implementations
- **Analysis Reliability**: Predictable evaluation patterns for analysis algorithms
- **Cross-Vendor Compatibility**: Common framework for diverse routing policy semantics

### Decision Tree Logic

The properties work together through a well-defined precedence hierarchy:

1. **Primary**: `useOutputAttributes` (highest precedence)
2. **Secondary**: `readFromIntermediateBgpAttributes` (medium precedence)
3. **Fallback**: Original route attributes (lowest precedence)

### Vendor Semantic Differences

- **Cisco/Arista**: Route-maps match against original attributes ("what you received")
- **Juniper**: Policy-statements can match against modified attributes ("what you're building")
- **Unified Framework**: Properties enable accurate modeling of both approaches

## Architecture Integration

The BGP attribute handling system integrates with Batfish's core architecture at multiple stages:

- **Parsing Stage**: Vendor-specific configurations are analyzed to determine property settings
- **Conversion Stage**: Vendor semantics are mapped to appropriate property configurations
- **Analysis Stage**: Properties guide attribute access during policy evaluation
- **Symbolic Analysis**: Properties are preserved in BDD representations for analysis

## Use Cases

### Network Analysis

- **Policy Validation**: Ensure routing policies behave as intended across vendors
- **Configuration Migration**: Validate behavior consistency when migrating between vendors
- **Compliance Checking**: Verify policies meet organizational requirements

### Development

- **Vendor Support**: Add support for new vendors by configuring appropriate properties
- **Testing**: Validate routing policy implementations against real device behavior
- **Debugging**: Troubleshoot complex routing policy interactions

## Getting Started

### For Users

1. **Read the Overview**: Start with [BGP Attribute Handling](bgp_attribute_handling.md) for conceptual understanding
2. **Vendor-Specific Behavior**: Review your vendor's implementation documentation
3. **Troubleshooting**: Consult the [Troubleshooting Guide](troubleshooting_guide.md) for common issues

### For Developers

1. **API Reference**: Review [API Reference](api_reference.md) for implementation details
2. **Code Examples**: Examine test cases and integration patterns
3. **Architecture**: Understand integration with Batfish's pipeline architecture

### For Network Engineers

1. **Vendor Comparison**: Compare behavior across [vendor implementations](vendor_implementations/)
2. **Migration Planning**: Use vendor documentation for migration strategies
3. **Best Practices**: Follow configuration recommendations in vendor-specific guides

## Research Background

This documentation is based on extensive research across three phases:

### Phase 1: Implementation Research

- Identified three core properties in Environment.java
- Analyzed 38+ usage locations across routing policy expressions
- Validated comprehensive test coverage with 25+ test files

### Phase 2: Interaction Analysis

- Documented decision tree precedence and edge case handling
- Analyzed vendor-specific rationale and architectural benefits
- Identified performance implications and optimization opportunities

### Phase 3: Vendor Research

- Compared route-map vs policy-statement architectures
- Documented industry standards and vendor-specific deviations
- Created cross-vendor compatibility matrix and implementation patterns

## Contributing

### Documentation Updates

- Follow existing documentation style and structure
- Include practical examples and use cases
- Cross-reference related documentation
- Validate technical accuracy against code implementation

### Code Integration

- Ensure property usage follows documented patterns
- Add appropriate test coverage for new scenarios
- Update documentation when adding new features
- Follow established architectural principles

## Support

### Internal Resources

- **Development Team**: For implementation questions and feature requests
- **Test Suites**: Comprehensive examples and validation patterns
- **Code Reviews**: Ensure consistency with established patterns

### External Resources

- **Vendor Documentation**: Device-specific behavior and configuration syntax
- **RFC Standards**: BGP protocol specifications and best practices
- **Community Forums**: General networking and routing policy questions

## Related Documentation

- **[Architecture Overview](../architecture/README.md)**: Batfish system design and pipeline
- **[Conversion Documentation](../conversion/README.md)**: Vendor-specific to vendor-independent conversion
- **[Data Plane Documentation](../data_plane/README.md)**: Route processing and RIB generation
- **[Symbolic Engine Documentation](../symbolic_engine/README.md)**: BDD-based analysis framework

## Version History

- **Initial Release**: Comprehensive documentation based on three-phase research
- **Future Enhancements**: Additional vendor support, performance optimizations, enhanced debugging tools

---

_This documentation serves as the definitive reference for BGP attribute handling in routing policy invariants. For questions or contributions, please consult the support resources above._
