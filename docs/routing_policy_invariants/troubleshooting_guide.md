# BGP Attribute Handling Troubleshooting Guide

## Overview

This guide provides comprehensive troubleshooting techniques for BGP attribute handling issues in routing policy invariants. It covers common problems, debugging methodologies, and resolution strategies across different vendor implementations.

## Common Issues and Solutions

### Issue 1: Unexpected Match Behavior

**Symptoms**:

- Route-map or policy-statement terms not matching as expected
- Attributes appearing to be ignored during policy evaluation
- Inconsistent behavior between similar configurations

**Root Causes**:

1. **Vendor Semantic Differences**: Cisco matches original attributes, Juniper can match modified attributes
2. **Property Misconfiguration**: Incorrect `useOutputAttributes` setting
3. **Attribute Timing**: Misunderstanding when attributes are available for matching

**Debugging Steps**:

1. **Identify Vendor Semantics**:

   ```bash
   # Check which vendor implementation is being used
   grep -r "useOutputAttributes" batfish-output/
   ```

2. **Verify Property Configuration**:

   ```java
   // Check Environment property settings
   Environment env = getEnvironment();
   boolean useOutput = env.getUseOutputAttributes();
   boolean readIntermediate = env.getReadFromIntermediateBgpAttributes();
   boolean writeIntermediate = env.getWriteToIntermediateBgpAttributes();
   ```

3. **Trace Attribute State**:
   - Log original route attributes before policy evaluation
   - Log intermediate attribute states during evaluation
   - Log final route attributes after policy evaluation

**Solutions**:

- **For Cisco/Arista**: Ensure `useOutputAttributes = false`
- **For Juniper**: Configure `useOutputAttributes = true` when matching modified attributes
- **Cross-Vendor**: Use appropriate property settings for target vendor semantics

### Issue 2: Attribute Modification Not Taking Effect

**Symptoms**:

- Set statements appear to execute but attributes remain unchanged
- Modifications visible in logs but not in final route
- Inconsistent modification behavior across different attributes

**Root Causes**:

1. **Intermediate Storage Issues**: Incorrect `writeToIntermediateBgpAttributes` setting
2. **Attribute Precedence**: Higher precedence attributes overriding modifications
3. **Type Conversion Errors**: Attribute type mismatches during modification

**Debugging Steps**:

1. **Check Intermediate Storage**:

   ```java
   // Verify intermediate attribute handling
   if (env.getWriteToIntermediateBgpAttributes()) {
       // Check intermediate storage state
       BgpAttributes intermediate = getIntermediateBgpAttributes();
       logAttributeState("Intermediate", intermediate);
   }
   ```

2. **Verify Attribute Types**:

   ```java
   // Ensure attribute types match expectations
   if (attribute instanceof LocalPreference) {
       LocalPreference localPref = (LocalPreference) attribute;
       // Verify value and type
   }
   ```

3. **Trace Modification Flow**:
   - Log before each set statement execution
   - Log after each set statement execution
   - Verify modifications persist through policy evaluation

**Solutions**:

- **Direct Modification**: Set `writeToIntermediateBgpAttributes = false` for immediate application
- **Intermediate Processing**: Set `writeToIntermediateBgpAttributes = true` for deferred application
- **Type Validation**: Ensure attribute values match expected types and ranges

### Issue 3: Complex Property Combination Behavior

**Symptoms**:

- Unexpected behavior when multiple properties are enabled
- Edge cases not behaving as documented
- Performance degradation with certain property combinations

**Root Causes**:

1. **Property Interaction**: Complex interactions between the three properties
2. **Edge Case Handling**: Uncommon property combinations not well-tested
3. **Performance Impact**: Certain combinations causing overhead

**Debugging Steps**:

1. **Property Combination Analysis**:

   ```java
   // Analyze current property combination
   boolean useOutput = env.getUseOutputAttributes();
   boolean readIntermediate = env.getReadFromIntermediateBgpAttributes();
   boolean writeIntermediate = env.getWriteToIntermediateBgpAttributes();

   String combination = String.format("useOutput=%b, readInt=%b, writeInt=%b",
       useOutput, readIntermediate, writeIntermediate);
   logger.info("Property combination: {}", combination);
   ```

2. **Decision Tree Tracing**:
   ```java
   // Trace decision tree execution
   if (useOutput) {
       logger.info("Using output attributes for matching");
       return getOutputAttributes();
   } else if (readIntermediate) {
       logger.info("Using intermediate attributes for matching");
       return getIntermediateBgpAttributes();
   } else {
       logger.info("Using original attributes for matching");
       return getOriginalAttributes();
   }
   ```

**Solutions**:

- **Standard Combinations**: Use well-tested property combinations when possible
- **Edge Case Testing**: Thoroughly test uncommon property combinations
- **Performance Monitoring**: Monitor performance impact of complex combinations

## Debugging Methodology

### Step 1: Environment Analysis

**Collect Environment Information**:

```java
// Gather environment details
Environment env = getRoutingPolicyEnvironment();
Map<String, Object> envDetails = new HashMap<>();
envDetails.put("useOutputAttributes", env.getUseOutputAttributes());
envDetails.put("readFromIntermediateBgpAttributes", env.getReadFromIntermediateBgpAttributes());
envDetails.put("writeToIntermediateBgpAttributes", env.getWriteToIntermediateBgpAttributes());
envDetails.put("vendor", detectVendorType());
envDetails.put("policyType", detectPolicyType());
```

**Verify Property Consistency**:

- Ensure properties match vendor expectations
- Check for property conflicts or inconsistencies
- Validate property settings against policy requirements

### Step 2: Attribute State Tracking

**Implement Comprehensive Logging**:

```java
public class AttributeStateTracker {
    public void logAttributeState(String phase, BgpAttributes attributes) {
        logger.info("=== Attribute State: {} ===", phase);
        logger.info("Local Preference: {}", attributes.getLocalPreference());
        logger.info("MED: {}", attributes.getMetric());
        logger.info("AS Path: {}", attributes.getAsPath());
        logger.info("Communities: {}", attributes.getCommunities());
        logger.info("Extended Communities: {}", attributes.getExtendedCommunities());
        logger.info("================================");
    }
}
```

**Track State Transitions**:

1. **Initial State**: Log original route attributes
2. **Intermediate States**: Log attributes after each policy statement
3. **Final State**: Log attributes after complete policy evaluation

### Step 3: Policy Execution Tracing

**Statement-Level Tracing**:

```java
public class PolicyExecutionTracer {
    public void traceStatementExecution(Statement stmt, Environment env) {
        logger.info("Executing statement: {}", stmt.getClass().getSimpleName());
        logger.info("Environment properties: useOutput={}, readInt={}, writeInt={}",
            env.getUseOutputAttributes(),
            env.getReadFromIntermediateBgpAttributes(),
            env.getWriteToIntermediateBgpAttributes());

        // Execute statement and log results
        Result result = stmt.execute(env);
        logger.info("Statement result: {}", result);
    }
}
```

**Expression-Level Tracing**:

```java
public class ExpressionTracer {
    public void traceExpressionEvaluation(BooleanExpr expr, Environment env) {
        logger.info("Evaluating expression: {}", expr.getClass().getSimpleName());

        // Log attribute source being used
        BgpAttributes attrs = getAttributesForMatching(env);
        logger.info("Using attributes from: {}", getAttributeSource(env));

        boolean result = expr.evaluate(env);
        logger.info("Expression result: {}", result);
    }
}
```

### Step 4: Cross-Vendor Validation

**Compare Vendor Behaviors**:

```java
public class CrossVendorValidator {
    public void validateBehavior(RoutingPolicy policy, BgpRoute route) {
        // Test with Cisco semantics
        Environment ciscoEnv = createCiscoEnvironment();
        BgpRoute ciscoResult = evaluatePolicy(policy, route, ciscoEnv);

        // Test with Juniper semantics
        Environment juniperEnv = createJuniperEnvironment();
        BgpRoute juniperResult = evaluatePolicy(policy, route, juniperEnv);

        // Compare results
        compareResults(ciscoResult, juniperResult);
    }
}
```

## Edge Case Resolution Strategies

### Edge Case 1: All Properties Enabled

**Scenario**: `useOutputAttributes=true`, `readFromIntermediateBgpAttributes=true`, `writeToIntermediateBgpAttributes=true`

**Expected Behavior**:

- Match operations use output attributes (highest precedence)
- Intermediate attributes are maintained but not used for matching
- Modifications are written to intermediate storage

**Troubleshooting**:

1. Verify output attributes are being used for matching
2. Check that intermediate storage is being maintained
3. Ensure modifications are applied correctly despite intermediate storage

### Edge Case 2: Intermediate Read Without Write

**Scenario**: `useOutputAttributes=false`, `readFromIntermediateBgpAttributes=true`, `writeToIntermediateBgpAttributes=false`

**Expected Behavior**:

- Match operations use intermediate attributes if available, otherwise original
- Modifications are written directly to output route
- Intermediate storage may be empty initially

**Troubleshooting**:

1. Check if intermediate attributes exist before reading
2. Verify fallback to original attributes when intermediate is empty
3. Ensure direct output modifications work correctly

### Edge Case 3: Intermediate Write Without Read

**Scenario**: `useOutputAttributes=false`, `readFromIntermediateBgpAttributes=false`, `writeToIntermediateBgpAttributes=true`

**Expected Behavior**:

- Match operations use original attributes
- Modifications are written to intermediate storage
- Final application of intermediate attributes to output

**Troubleshooting**:

1. Verify original attributes are used for matching
2. Check that modifications accumulate in intermediate storage
3. Ensure intermediate attributes are applied to final output

## Performance Troubleshooting

### Performance Issue 1: Excessive Attribute Copying

**Symptoms**:

- High memory usage during policy evaluation
- Slow policy evaluation performance
- Garbage collection pressure

**Debugging**:

```java
// Monitor attribute copying
public class AttributeCopyMonitor {
    private int copyCount = 0;

    public BgpAttributes copyAttributes(BgpAttributes original) {
        copyCount++;
        if (copyCount % 100 == 0) {
            logger.warn("Attribute copy count: {}", copyCount);
        }
        return original.toBuilder().build();
    }
}
```

**Solutions**:

- Use reference sharing when attributes are not modified
- Implement copy-on-write semantics for attribute modifications
- Cache frequently accessed attribute combinations

### Performance Issue 2: Intermediate Storage Overhead

**Symptoms**:

- Memory usage increases with intermediate attribute usage
- Performance degradation with complex policies
- Memory leaks in long-running evaluations

**Debugging**:

```java
// Monitor intermediate storage usage
public class IntermediateStorageMonitor {
    public void monitorStorage(Environment env) {
        if (env.getWriteToIntermediateBgpAttributes()) {
            BgpAttributes intermediate = env.getIntermediateBgpAttributes();
            if (intermediate != null) {
                logger.info("Intermediate storage size: {} bytes",
                    estimateAttributeSize(intermediate));
            }
        }
    }
}
```

**Solutions**:

- Clear intermediate storage when no longer needed
- Use lazy initialization for intermediate attributes
- Implement storage pooling for frequently used patterns

## Vendor-Specific Troubleshooting

### Cisco-Specific Issues

**Common Problems**:

1. **Continue Statement Confusion**: Complex continue chains causing unexpected behavior
2. **Sequence Ordering**: Implicit dependencies on sequence number ordering
3. **Attribute Timing**: Misunderstanding when modifications take effect

**Debugging Techniques**:

```bash
# Trace route-map sequence execution
debug ip bgp updates
debug ip policy
```

**Solutions**:

- Simplify continue statement usage
- Make sequence dependencies explicit
- Use clear sequence numbering with gaps

### Juniper-Specific Issues

**Common Problems**:

1. **Policy Chaining**: Complex policy chains causing attribute confusion
2. **Term Scoping**: Misunderstanding term vs. policy scope for modifications
3. **Flow Control**: Incorrect next-term/next-policy/accept/reject usage

**Debugging Techniques**:

```juniper
# Enable policy tracing
set policy-options policy-statement POLICY_NAME then trace
```

**Solutions**:

- Simplify policy chain structures
- Clarify term vs. policy scoping in documentation
- Use explicit flow control statements

### Arista-Specific Issues

**Common Problems**:

1. **Enhanced Feature Conflicts**: Arista-specific features conflicting with Cisco compatibility
2. **Arithmetic Operations**: Boundary conditions in arithmetic operations
3. **Call Statement Complexity**: Complex call hierarchies causing confusion

**Debugging Techniques**:

```arista
# Enable route-map debugging
debug bgp updates
debug ip policy
```

**Solutions**:

- Test enhanced features thoroughly in isolation
- Validate arithmetic operation boundaries
- Limit call statement nesting depth

## Testing and Validation

### Unit Testing Approach

```java
@Test
public void testAttributeHandlingProperties() {
    // Test all property combinations
    for (boolean useOutput : Arrays.asList(true, false)) {
        for (boolean readInt : Arrays.asList(true, false)) {
            for (boolean writeInt : Arrays.asList(true, false)) {
                Environment env = Environment.builder()
                    .setUseOutputAttributes(useOutput)
                    .setReadFromIntermediateBgpAttributes(readInt)
                    .setWriteToIntermediateBgpAttributes(writeInt)
                    .build();

                validatePropertyCombination(env);
            }
        }
    }
}
```

### Integration Testing Approach

```java
@Test
public void testCrossVendorConsistency() {
    // Test same logical policy across vendors
    RoutingPolicy policy = createTestPolicy();
    BgpRoute inputRoute = createTestRoute();

    // Test with different vendor environments
    BgpRoute ciscoResult = evaluateWithCiscoSemantics(policy, inputRoute);
    BgpRoute juniperResult = evaluateWithJuniperSemantics(policy, inputRoute);
    BgpRoute aristaResult = evaluateWithAristaSemantics(policy, inputRoute);

    // Validate results match expectations
    validateVendorResults(ciscoResult, juniperResult, aristaResult);
}
```

### Regression Testing

```java
@Test
public void testRegressionScenarios() {
    // Test known problematic scenarios
    List<TestScenario> scenarios = loadRegressionScenarios();

    for (TestScenario scenario : scenarios) {
        Environment env = scenario.getEnvironment();
        RoutingPolicy policy = scenario.getPolicy();
        BgpRoute input = scenario.getInputRoute();
        BgpRoute expected = scenario.getExpectedOutput();

        BgpRoute actual = evaluatePolicy(policy, input, env);
        assertEquals("Regression test failed: " + scenario.getName(),
                    expected, actual);
    }
}
```

## Best Practices for Prevention

### Configuration Best Practices

1. **Property Documentation**: Document property choices and rationale
2. **Vendor Consistency**: Use consistent property settings within vendor families
3. **Testing**: Thoroughly test property combinations before deployment
4. **Monitoring**: Monitor performance impact of property choices

### Development Best Practices

1. **Property Validation**: Validate property combinations during initialization
2. **Error Handling**: Provide clear error messages for invalid property combinations
3. **Performance Monitoring**: Monitor performance impact of different property settings
4. **Documentation**: Maintain clear documentation of property behavior

### Operational Best Practices

1. **Change Management**: Test property changes in non-production environments
2. **Monitoring**: Monitor for unexpected behavior after property changes
3. **Rollback Plans**: Maintain rollback procedures for property modifications
4. **Documentation**: Keep operational documentation current with property usage

## Related Documentation

- [BGP Attribute Handling Overview](bgp_attribute_handling.md): Core concepts and property framework
- [Cisco Implementation](vendor_implementations/cisco.md): Cisco-specific behavior and troubleshooting
- [Juniper Implementation](vendor_implementations/juniper.md): Juniper-specific behavior and troubleshooting
- [Arista Implementation](vendor_implementations/arista.md): Arista-specific behavior and troubleshooting
- [API Reference](api_reference.md): Detailed API specifications and usage patterns

## Support Resources

### Internal Resources

- Batfish development team for implementation questions
- Vendor documentation for device-specific behavior
- Test suites for validation and regression testing

### External Resources

- Vendor support for device-specific issues
- RFC specifications for BGP behavior standards
- Community forums for general networking questions

### Escalation Procedures

1. **Level 1**: Check this troubleshooting guide and related documentation
2. **Level 2**: Review test cases and implementation code
3. **Level 3**: Engage Batfish development team
4. **Level 4**: Consult vendor support for device-specific issues
