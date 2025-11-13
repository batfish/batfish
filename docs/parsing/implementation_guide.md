# Implementation Guide for New Commands

This guide provides a structured approach to implementing new commands in Batfish, with a focus on making the right decisions about how deeply to implement each command.

## Decision Tree for Implementation Level

When adding support for a new command, follow this decision tree to determine the appropriate implementation level:

```
Is the command syntax recognized by the parser?
├── No → State 1: Not parsed at all (unrecognized)
│   └── Action: Add to grammar
└── Yes → Does this command affect the data model or network behavior?
    ├── No → State 2: In grammar but never needs extraction
    │   └── Action: Add with _null suffix
    └── Yes → Is the command fully understood and ready to implement?
        ├── No → State 3: In grammar but not implemented yet
        │   └── Action: Add to grammar and use todo() or warn() in extraction
        └── Yes → Does the command have complex or conditional behavior?
            ├── Yes → State 4: Extracted but conditionally supported
            │   └── Action: Implement extraction and add conditional warnings
            └── No → State 5: Fully implemented
                └── Action: Implement parsing, extraction, and conversion
```

## Implementation States Explained

### State 1: Not parsed at all (unrecognized)

The command is not recognized by the parser. This results in parse errors and warnings about unrecognized lines.

**Example:** A brand new command that hasn't been added to the grammar yet.

### State 2: In grammar, but never needs extraction (silently ignored)

The command is recognized by the parser but doesn't affect the data model or network behavior that Batfish models. These commands should be added to the grammar with a `_null` suffix to indicate they are intentionally not extracted.

**Example:** Commands related to logging, display preferences, or other operational aspects that don't affect forwarding behavior.

```java
s_log_null
:
  LOG SYSLOG NEWLINE
;
```

### State 3: In grammar, not implemented yet, but known to be wrong if used

The command is recognized by the parser and does affect network behavior, but it's not yet implemented. Use `todo()` or `warn()` in the extraction phase to indicate this.

**Example:** A command that affects routing behavior but hasn't been fully implemented yet.

```java
@Override
public void exitS_routing_options(S_routing_optionsContext ctx) {
  todo(ctx);
}
```

### State 4: Extracted but conditionally supported

The command is implemented but may not be fully supported in all contexts or with all parameter combinations. Add conditional warnings during conversion.

**Example:** A command that works in some contexts but not others.

```java
if (!isSupported) {
  _w.redFlag("This particular use of the command is not supported");
}
```

### State 5: Fully implemented

The command is fully supported with no warnings.

## When to Use the `_null` Suffix

The `_null` suffix should be used when:

1. The command is recognized by the parser but doesn't affect the data model
2. You want to avoid parse warnings for valid syntax
3. There's no current plan to extract or convert the command

Adding the `_null` suffix allows the SilentSyntaxListener to handle these commands without generating warnings.

### Example: Adding a Command with `_null` Suffix

For a command like `set interfaces ge-0/0/0 family ethernet-switching recovery-timeout 180` that you want to parse but not extract:

1. Add the token to the lexer:

   ```java
   RECOVERY_TIMEOUT: 'recovery-timeout';
   ```

2. Add the grammar rule with `_null` suffix:

   ```java
   ife_recovery_timeout_null
   :
     RECOVERY_TIMEOUT uint16
   ;
   ```

3. Add it to the parent rule:
   ```java
   if_ethernet_switching
   :
      ETHERNET_SWITCHING
      (
         apply
         | if_storm_control
         | ife_filter
         | ife_interface_mode
         | ife_native_vlan_id
         | ife_port_mode
         | ife_recovery_timeout_null
         | ife_vlan
      )
   ;
   ```

## Testing Extraction

When implementing a new command, it's important to write tests to verify that the extraction works correctly. Here's how to structure extraction tests:

1. Create a test configuration file in the `test_configs` directory
2. Parse the configuration using `parseJuniperConfig`
3. Access the vendor-specific configuration objects
4. Verify that specific properties have been correctly extracted using assertions

### Example Extraction Test

```java
@Test
public void testRecoveryTimeoutExtraction() {
  JuniperConfiguration config = parseJuniperConfig("recovery-timeout-test");
  Interface iface = config.getMasterLogicalSystem().getInterfaces().get("ge-0/0/0");
  EthernetSwitching es = iface.getEthernetSwitching();

  // If implementing full extraction:
  assertEquals(Integer.valueOf(180), es.getRecoveryTimeout());

  // If using _null suffix, verify the command doesn't cause parse errors:
  assertThat(config.getWarnings().getParseWarnings(), empty());
}
```

## Vendor-Specific Considerations

### Juniper-Specific Notes

1. **Preprocessing**: Juniper configurations go through a preprocessing stage that handles hierarchy, inheritance, and deactivation before parsing.

2. **null_filler**: For Juniper, the `null_filler` parse rule is used to ignore the rest of a line when using the `_null` suffix.

3. **Interface Hierarchy**: Juniper interfaces have a hierarchical structure with physical interfaces and logical units.

4. **Family Configuration**: Interface properties are often organized under "families" like `ethernet-switching`, `inet`, etc.

## Common Implementation Patterns

### Pattern 1: Simple Property Extraction

For simple properties that need to be extracted:

1. Add a field to the appropriate class
2. Add getter/setter methods
3. Update the extractor to set the property

### Pattern 2: Ignored Command with `_null` Suffix

For commands that should be parsed but ignored:

1. Add the grammar rule with `_null` suffix
2. No extraction or conversion code needed

### Pattern 3: Extraction with Warnings

For commands that are partially supported:

1. Extract the property
2. Add conditional warnings during conversion

### Pattern 4: Structure Definition and Reference Tracking

For commands that define named objects referenced elsewhere:

1. Add structure type to vendor's `StructureType` enum
2. Add usage type(s) to vendor's `StructureUsage` enum (name should match command path)
3. In extractor enter method: Call `defineStructure(type, name, ctx)`
4. In extractor exit methods: Call `referenceStructure(type, name, usage, line)` at reference sites
5. In conversion: Call `markConcreteStructure(type)`
6. Test with `hasDefinedStructure()`, `hasNumReferrers()`, `hasUndefinedReference()` matchers

**See**: CiscoControlPlaneExtractor methods `enterDtr_policy`, `exitIfdt_attach_policy`, `exitVlanc_device_tracking` and CiscoConfiguration method `toVendorIndependentConfiguration` for examples.

## Extraction Best Practices

### Using Assert Statements in Alternatives

When extracting grammar alternatives, use assert in else branches to catch when new alternatives are added without updating extraction logic:

```java
// GOOD: Future-proof with assert
if (ctx.OPTION_A() != null) {
  handleOptionA();
} else {
  assert ctx.OPTION_B() != null;  // Fails if OPTION_C added to grammar
  handleOptionB();
}

// BAD: else-if chain silently skips new alternatives
if (ctx.OPTION_A() != null) {
  handleOptionA();
} else if (ctx.OPTION_B() != null) {  // OPTION_C would be silently ignored
  handleOptionB();
}
```

### Using @Nullable to Distinguish Unconfigured vs Explicit Values

Use `@Nullable Boolean`/`Integer` instead of primitives with constructor defaults. This allows conversion to distinguish "not configured" from "explicitly set to default":

```java
// GOOD: null means unconfigured, false means explicit
private @Nullable Boolean _trackingEnabled;

// BAD: Cannot distinguish unconfigured from explicit false
private boolean _trackingEnabled = false;
```

### Storing Current Context Objects vs Names

For block-mode configurations, store the current object rather than its name to avoid repeated map lookups and null checks:

```java
// GOOD: Store object, access directly in exit methods
private @Nullable PolicyObject _currentPolicy;
_currentPolicy = _configuration.getPolicies().computeIfAbsent(name, PolicyObject::new);
// Later: _currentPolicy.setSomeProperty(value);

// BAD: Store name, lookup required in every exit method
private @Nullable String _currentPolicyName;
PolicyObject policy = _configuration.getPolicies().get(_currentPolicyName);
if (policy != null) { policy.setSomeProperty(value); }
```

**See**: Extractor `_current*` fields for examples of this pattern.

