# Juniper-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Juniper configurations in Batfish.

## Juniper Configuration Structure

Juniper configurations have several unique characteristics:

1. **Hierarchical Structure**: Configurations are organized in a hierarchical tree structure
2. **Set-Style Commands**: Configurations use `set` commands to define the configuration
3. **Inheritance**: Configurations can inherit from groups using `apply-groups`
4. **Deactivation**: Parts of the configuration can be deactivated using `deactivate`

## Preprocessing

Before parsing, Juniper configurations go through a preprocessing stage that:

1. Builds a hierarchical representation of the configuration
2. Applies group inheritance
3. Processes deactivation statements
4. Flattens the configuration into a series of active configuration lines

The preprocessing is handled by the [JuniperPreprocessor](../../../projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/PreprocessJuniperExtractor.java).

### Preprocessing Stages

1. **Build Hierarchy**: Parse the configuration into a hierarchical structure
2. **Apply Groups**: Apply group inheritance
3. **Process Deactivation**: Remove deactivated parts of the configuration
4. **Flatten**: Convert the hierarchical structure into a flat list of configuration lines

## Juniper Grammar Structure

The Juniper grammar is split into several files:

- `FlatJuniperLexer.g4`: Defines tokens for the lexer
- `FlatJuniperParser.g4`: Main parser file
- `FlatJuniper_interfaces.g4`: Interface-specific grammar
- `FlatJuniper_protocols.g4`: Protocol-specific grammar
- And many other feature-specific grammar files

## Common Juniper Parsing Patterns

### null_filler Pattern

For Juniper, the `null_filler` parse rule is used to ignore the rest of a line when using the `_null` suffix:

```
ife_recovery_timeout_null
:
  RECOVERY_TIMEOUT null_filler
;
```

This pattern is commonly used for commands that should be parsed but not extracted.

### Interface Hierarchy

Juniper interfaces have a hierarchical structure:

1. **Physical Interfaces**: e.g., `ge-0/0/0`
2. **Logical Units**: e.g., `ge-0/0/0.0`

In the code, this is represented by the `Interface` class, which has a `parent` property and a `units` map.

### Family Configuration

Interface properties are often organized under "families" like `ethernet-switching`, `inet`, etc.:

```
set interfaces ge-0/0/0 family ethernet-switching recovery-timeout 180
```

In the grammar, these are represented by rules like `if_ethernet_switching`, `if_inet`, etc.

## Implementation Decision Guide for Juniper

When implementing a new Juniper command, consider:

1. **Which configuration section does it belong to?** (interfaces, protocols, etc.)
2. **Does it affect the data model?** If not, use `_null` suffix
3. **Is it a common pattern?** Look for similar commands as examples

### Example: Adding a New Interface Command

To add support for a new interface command like `recovery-timeout`:

1. **Add the token to the lexer**:

   ```java
   RECOVERY_TIMEOUT: 'recovery-timeout';
   ```

2. **Decide on implementation level**:

   - If it doesn't affect the data model: Add with `_null` suffix
   - If it does affect the data model: Add extraction and conversion

3. **Add the grammar rule**:

   - With `_null` suffix:
     ```java
     ife_recovery_timeout_null
     :
       RECOVERY_TIMEOUT null_filler
     ;
     ```
   - Without `_null` suffix (for extraction):
     ```java
     ife_recovery_timeout
     :
       RECOVERY_TIMEOUT seconds = uint16
     ;
     ```

4. **Add it to the parent rule**:

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
         | ife_recovery_timeout
         | ife_vlan
      )
   ;
   ```

5. **Add extraction code** (if not using `_null` suffix):

   ```java
   @Override
   public void exitIfe_recovery_timeout(Ife_recovery_timeoutContext ctx) {
     _currentInterfaceOrRange.getEthernetSwitching().setRecoveryTimeout(toInt(ctx.seconds));
   }
   ```

6. **Add conversion code** (if needed)

7. **Write tests**

## Testing Juniper Configurations

When testing Juniper configurations:

1. **Create minimal test configurations** that focus on the feature being tested
2. **Use `parseJuniperConfig`** to parse the configuration and access the vendor-specific objects
3. **Verify extraction** by checking that the appropriate properties are set
4. **Test edge cases** like invalid values or interactions with other features

### Example Test

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
