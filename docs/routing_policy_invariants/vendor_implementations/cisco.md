# Cisco BGP Attribute Handling Implementation

## Overview

Cisco's routing policy implementation follows a "match against what you received" semantic model, where route-map statements primarily match against the original attributes of incoming routes rather than attributes being modified during policy evaluation.

This document details how Batfish models Cisco's BGP attribute handling behavior using the three core properties in the routing policy invariants framework.

## Cisco Route-Map Architecture

### Core Principles

1. **Sequential Processing**: Route-maps are processed sequentially with explicit sequence numbers
2. **Original Attribute Matching**: Match statements typically evaluate against original route attributes
3. **Immediate Modification**: Set statements apply changes immediately to the route being processed
4. **Continue Behavior**: Explicit control over whether processing continues after a match

### Attribute Handling Semantics

Cisco's approach to BGP attribute handling can be summarized as:

- **Match Phase**: Evaluate conditions against original/input attributes
- **Set Phase**: Apply modifications to output attributes
- **Decision Phase**: Use modified attributes for route selection

## Property Configuration for Cisco

### Standard Cisco Configuration

For typical Cisco route-map processing, the BGP attribute handling properties are configured as:

```java
Environment ciscoEnvironment = Environment.builder()
    .setUseOutputAttributes(false)           // Match against original attributes
    .setReadFromIntermediateBgpAttributes(false)  // No intermediate storage
    .setWriteToIntermediateBgpAttributes(false)   // Direct output modification
    .build();
```

### Rationale

- **`useOutputAttributes = false`**: Cisco route-maps match against the original attributes received with the route
- **`readFromIntermediateBgpAttributes = false`**: No intermediate attribute storage in standard Cisco processing
- **`writeToIntermediateBgpAttributes = false`**: Modifications are applied directly to the output route

## Configuration Examples

### Basic Route-Map Example

```cisco
route-map EXAMPLE permit 10
 match as-path 100
 set local-preference 200
 continue 20
!
route-map EXAMPLE permit 20
 match community 65001:100
 set med 50
```

**Batfish Modeling**:

- Sequence 10: Matches AS-path against original route attributes, sets local-preference on output
- Sequence 20: Matches community against original route attributes (not modified by sequence 10), sets MED on output
- Both match operations use original attributes despite local-preference modification in sequence 10

### Complex Multi-Stage Processing

```cisco
route-map COMPLEX permit 10
 match as-path 100
 set as-path prepend 65001
 continue 20
!
route-map COMPLEX permit 20
 match as-path 200
 set local-preference 300
```

**Attribute Handling Flow**:

1. **Sequence 10**:
   - Match: Evaluates AS-path 100 against original route AS-path
   - Set: Prepends 65001 to output route AS-path
   - Continue: Proceeds to sequence 20
2. **Sequence 20**:
   - Match: Evaluates AS-path 200 against **original** route AS-path (not the prepended version)
   - Set: Sets local-preference to 300 if match succeeds

## Vendor-Specific Behaviors

### AS-Path Handling

Cisco's AS-path processing demonstrates the "match original" principle:

```cisco
route-map AS_PATH_EXAMPLE permit 10
 match as-path 1
 set as-path prepend 65001 65002
 continue 20
!
route-map AS_PATH_EXAMPLE permit 20
 match as-path 2
 set local-preference 200
```

**Behavior**: The match in sequence 20 evaluates against the original AS-path, not the prepended version from sequence 10.

### Community Handling

Community attribute processing follows similar semantics:

```cisco
route-map COMMUNITY_EXAMPLE permit 10
 match community 100:1
 set community 100:2 additive
 continue 20
!
route-map COMMUNITY_EXAMPLE permit 20
 match community 100:2
 set local-preference 150
```

**Behavior**: The match in sequence 20 evaluates against original communities, so it will **not** match the community added in sequence 10.

### Local Preference and MED

Numeric attributes like local-preference and MED follow the same pattern:

```cisco
route-map NUMERIC_EXAMPLE permit 10
 match local-preference 100
 set local-preference 200
 continue 20
!
route-map NUMERIC_EXAMPLE permit 20
 match local-preference 200
 set med 50
```

**Behavior**: The match in sequence 20 evaluates against the original local-preference (100), not the modified value (200).

## Edge Cases and Special Scenarios

### Continue Statement Behavior

The `continue` statement in Cisco route-maps creates complex evaluation patterns:

```cisco
route-map CONTINUE_EXAMPLE permit 10
 match as-path 100
 set local-preference 200
 continue 30
!
route-map CONTINUE_EXAMPLE permit 20
 match community 65001:100
 set med 100
!
route-map CONTINUE_EXAMPLE permit 30
 match local-preference 150
 set community 65001:200
```

**Evaluation Flow**:

1. Sequence 10 matches and modifies, then jumps to sequence 30
2. Sequence 30 matches against original local-preference (not the value set in sequence 10)
3. Sequence 20 is skipped due to the continue statement

### Deny Statements

Deny statements in route-maps affect processing flow:

```cisco
route-map DENY_EXAMPLE deny 10
 match as-path 100
!
route-map DENY_EXAMPLE permit 20
 match community 65001:100
 set local-preference 200
```

**Behavior**: If sequence 10 matches, the route is denied and processing stops. Sequence 20 is never evaluated.

## Comparison with Other Vendors

### Cisco vs. Juniper

| Aspect            | Cisco                        | Juniper                            |
| ----------------- | ---------------------------- | ---------------------------------- |
| Match Semantics   | Original attributes          | Modified attributes (configurable) |
| Processing Model  | Sequential route-maps        | Hierarchical policy-statements     |
| Attribute Scope   | Global modifications         | Term-scoped modifications          |
| Continue Behavior | Explicit continue statements | Implicit term progression          |

### Cisco vs. Arista

Arista EOS largely follows Cisco semantics with some extensions:

| Feature            | Cisco          | Arista EOS                                 |
| ------------------ | -------------- | ------------------------------------------ |
| Basic Route-Maps   | Identical      | Identical                                  |
| Extended Features  | Limited        | Enhanced with additional match/set options |
| Attribute Handling | Original-based | Original-based (same as Cisco)             |

## Implementation Details in Batfish

### Parser Integration

Cisco route-map parsing in Batfish:

1. **Grammar Processing**: ANTLR grammar extracts route-map structure
2. **Semantic Analysis**: Route-map sequences are converted to policy statements
3. **Property Assignment**: Environment properties are set based on Cisco semantics
4. **Optimization**: Sequential processing is optimized while preserving semantics

### Conversion Process

The conversion from Cisco-specific to vendor-independent model:

```java
// Cisco route-map conversion pseudocode
public void convertRouteMap(RouteMap routeMap) {
    Environment.Builder envBuilder = Environment.builder()
        .setUseOutputAttributes(false)  // Cisco matches original
        .setReadFromIntermediateBgpAttributes(false)
        .setWriteToIntermediateBgpAttributes(false);

    // Convert each route-map entry
    for (RouteMapEntry entry : routeMap.getEntries()) {
        convertRouteMapEntry(entry, envBuilder.build());
    }
}
```

### Testing and Validation

Batfish validates Cisco behavior through:

1. **Unit Tests**: Individual route-map statement behavior
2. **Integration Tests**: Complex multi-sequence scenarios
3. **Regression Tests**: Comparison with real Cisco device behavior
4. **Performance Tests**: Large-scale route-map processing

## Configuration Best Practices

### Recommended Patterns

1. **Clear Sequencing**: Use explicit sequence numbers with gaps for future insertions
2. **Consistent Matching**: Group related match conditions in single sequences
3. **Minimal Continue Usage**: Use continue sparingly to maintain readability
4. **Documentation**: Comment complex route-maps for maintainability

### Anti-Patterns to Avoid

1. **Dense Sequencing**: Avoid consecutive sequence numbers without gaps
2. **Complex Continue Chains**: Minimize complex continue statement patterns
3. **Overlapping Matches**: Avoid ambiguous match conditions across sequences
4. **Implicit Dependencies**: Don't rely on implicit sequence ordering for correctness

## Troubleshooting Guide

### Common Issues

1. **Unexpected Match Behavior**: Remember that matches evaluate against original attributes
2. **Continue Statement Confusion**: Trace execution flow carefully with continue statements
3. **Sequence Ordering**: Ensure sequences are processed in intended order
4. **Attribute Modification Timing**: Understand when modifications take effect

### Debugging Techniques

1. **Sequence-by-Sequence Analysis**: Trace route processing through each sequence
2. **Attribute State Tracking**: Monitor original vs. modified attribute states
3. **Match Condition Testing**: Verify match conditions against expected attribute values
4. **Output Verification**: Confirm final route attributes match expectations

## Related Documentation

- [BGP Attribute Handling Overview](../bgp_attribute_handling.md): Core concepts and property framework
- [Juniper Implementation](juniper.md): Contrasting vendor approach
- [Arista Implementation](arista.md): Similar vendor implementation
- [Troubleshooting Guide](../troubleshooting_guide.md): Cross-vendor debugging techniques

## References

- Cisco IOS Configuration Guide: Route Maps
- Cisco BGP Configuration Guide: Attribute Manipulation
- RFC 4271: Border Gateway Protocol 4 (BGP-4)
- Batfish Cisco Grammar: Route-map parsing implementation
