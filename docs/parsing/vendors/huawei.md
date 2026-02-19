# Huawei-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Huawei VRP (Versatile Routing Platform) configurations in Batfish.

## Huawei Configuration Structure

Huawei VRP configurations have several unique characteristics:

1. **Hierarchical Block Structure**: Configurations are organized in hierarchical blocks using indentation or explicit block delimiters
2. **Return Statements**: Configuration blocks are terminated with `return` statements
3. **Undo Commands**: The `undo` prefix negates or removes configuration (similar to `no` in Cisco IOS)
4. **Comment Syntax**: Supports both `#` and `!` for comments

## Code Organization

Huawei support uses the **vendor-scoped pattern** (recommended for new vendors):

```
projects/batfish/src/main/
├── antlr4/org/batfish/vendor/huawei/grammar/   # ANTLR .g4 files
│   ├── HuaweiLexer.g4
│   └── HuaweiParser.g4
└── java/org/batfish/vendor/huawei/
    ├── grammar/                                  # Parser, extractor, base lexer
    │   ├── HuaweiBaseLexer.java
    │   ├── HuaweiCombinedParser.java
    │   └── HuaweiControlPlaneExtractor.java
    └── representation/                           # Vendor-specific data model
        ├── HuaweiConfiguration.java
        ├── HuaweiInterface.java
        ├── HuaweiBgpProcess.java
        ├── HuaweiOspfProcess.java
        ├── HuaweiStaticRoute.java
        ├── HuaweiAcl.java
        ├── HuaweiVrf.java
        └── HuaweiVlan.java

projects/batfish/src/test/java/org/batfish/vendor/huawei/  # All tests unified
```

## Huawei Grammar Structure

The Huawei grammar is organized into two files:

- `HuaweiLexer.g4`: Defines tokens for the lexer
- `HuaweiParser.g4`: Main parser file with all grammar rules

### Key Tokens

```
ACL, ADDRESS, AREA, AS_NUMBER, BATCH, BGP, DENY, DESCRIPTION,
INTERFACE, IP, LOOPBACK, NETWORK, OSPF, PEER, PERMIT, RETURN,
ROUTE_STATIC, ROUTER_ID, RULE, SHUTDOWN, SYSNAME, UNDO, VLAN, VPN_INSTANCE
```

## Common Huawei Parsing Patterns

### Block Termination with Return

Huawei configuration blocks use `return` to exit the current configuration context:

```antlr
s_interface: INTERFACE name=word NEWLINE interface_statement* s_return;

s_return: RETURN NEWLINE;
```

This pattern is used consistently across:
- Interface configurations
- BGP configurations
- OSPF configurations
- ACL configurations
- VRF/VPN instance configurations

### Null Statement for Unrecognized Lines

The grammar includes a catch-all `s_null` rule to handle unrecognized configuration lines:

```antlr
statement
  : s_sysname
  | s_interface
  | s_bgp
  | s_ospf
  | s_ip
  | s_vlan
  | s_acl
  | s_return
  | s_null
  ;

s_null: null_rest_of_line;
null_rest_of_line: ~NEWLINE* NEWLINE;
```

This ensures that unrecognized commands don't cause parse errors.

### Undo Command Handling

Huawei uses `undo` to negate commands. The grammar handles this pattern:

```antlr
is_shutdown: SHUTDOWN NEWLINE;
is_undo_shutdown: UNDO SHUTDOWN NEWLINE;
```

## Currently Supported Features

### System Configuration

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| sysname (hostname) | Yes | Yes | Yes |

### Interfaces

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| GigabitEthernet | Yes | Yes | Yes |
| LoopBack | Yes | Yes | Yes |
| Vlanif | Yes | Yes | Yes |
| Eth-Trunk | Yes | Yes | Yes |
| Description | Yes | Yes | Yes |
| IP Address | Yes | Yes | Yes |
| Shutdown | Yes | Yes | Yes |
| Undo Shutdown | Yes | Yes | Yes |

### IP Configuration

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| Static Routes | Yes | Yes | Yes |
| VPN Instance (VRF) | Yes | Yes | Partial |

### Routing Protocols

#### BGP

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| AS Number | Yes | Yes | Yes |
| Router ID | Yes | Yes | Yes |
| Router ID Inference | N/A | N/A | Yes (from Loopback0) |
| Peer | Yes | Yes | Yes |
| Peer AS Number | Yes | Yes | Yes |
| Network | Yes | Yes | Planned |

#### OSPF

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| Process ID | Yes | Yes | Planned |
| Router ID | Yes | Yes | Planned |
| Area | Yes | Yes | Planned |
| Network (area) | Yes | Yes | Planned |

### Layer 2

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| VLAN (single) | Yes | Yes | N/A |
| VLAN (batch) | Yes | Yes | N/A |

### Security

| Feature | Grammar | Extraction | VI Conversion |
|---------|---------|------------|---------------|
| ACL (numbered) | Yes | Yes | Planned |
| ACL (named) | Yes | Yes | Planned |
| ACL Rule (permit/deny) | Yes | Yes | Planned |

## Implementation Decision Guide for Huawei

When implementing a new Huawei command, consider:

1. **Which configuration section does it belong to?** (interface, bgp, ospf, etc.)
2. **Does it affect the data model?** If not, add to the appropriate `_null` rule
3. **Does it require conversion to vendor-independent model?** If yes, update `HuaweiConversions.java`

### Example: Adding a New Interface Command

To add support for a new interface command like `mtu`:

1. **Add the token to the lexer**:

   ```antlr
   MTU: 'mtu';
   ```

2. **Add the grammar rule**:

   For extraction:
   ```antlr
   is_mtu: MTU num=dec NEWLINE;
   ```

   Or for null (no extraction):
   ```antlr
   // Add to is_null which already catches unrecognized commands
   ```

3. **Update the interface_statement rule**:

   ```antlr
   interface_statement
     : is_description
     | is_ip_address
     | is_shutdown
     | is_undo_shutdown
     | is_mtu      // Add here
     | is_null
     ;
   ```

4. **Add extraction code** in `HuaweiControlPlaneExtractor.java`:

   ```java
   @Override
   public void exitIs_mtu(Is_mtuContext ctx) {
     _currentInterface.setMtu(Integer.parseInt(ctx.num.getText()));
   }
   ```

5. **Add to HuaweiInterface.java**:

   ```java
   private Integer _mtu;

   public Integer getMtu() { return _mtu; }
   public void setMtu(Integer mtu) { _mtu = mtu; }
   ```

6. **Add conversion code** in `HuaweiConversions.java` (if needed)

7. **Write tests** in `HuaweiGrammarTest.java`

## Testing Huawei Configurations

When testing Huawei configurations:

1. **Create minimal test configurations** that focus on the feature being tested
2. **Use inline configs** in test methods for simple cases
3. **Verify extraction** by checking that the appropriate properties are set
4. **Verify VI conversion** for features that convert to the vendor-independent model

### Example Test

```java
@Test
public void testNewFeature() {
  String config =
      "sysname TestRouter\n"
          + "\n"
          + "interface GigabitEthernet0/0/0\n"
          + " ip address 192.168.1.1 255.255.255.0\n"
          + " mtu 9000\n"
          + "return\n";

  HuaweiConfiguration huaweiConfig = parseVendorConfig(config);
  HuaweiInterface iface = huaweiConfig.getInterfaces().get("GigabitEthernet0/0/0");

  assertThat(iface.getMtu(), equalTo(9000));
}
```

### Running Tests

```bash
# Run all Huawei tests
bazel test //projects/batfish/src/test/java/org/batfish/vendor/huawei:tests

# Run a specific test method
bazel test --test_filter=HuaweiGrammarTest#testNewFeature \
    //projects/batfish/src/test/java/org/batfish/vendor/huawei:tests
```

## Huawei-Specific Conversion Notes

### BGP Router ID Inference

If BGP router ID is not explicitly configured, the conversion logic infers it from the IP address of `Loopback0`:

```java
// In HuaweiConversions.convertBgpProcess()
if (bgpProcess.getRouterId() == null) {
  HuaweiInterface loopback0 = interfaces.get("LoopBack0");
  if (loopback0 != null && loopback0.getAddress() != null) {
    builder.setRouterId(loopback0.getAddress().getIp());
  }
}
```

### Interface Type Detection

Interface types are automatically detected based on naming conventions:

| Prefix | Interface Type |
|--------|---------------|
| `GigabitEthernet` | PHYSICAL |
| `LoopBack` | LOOPBACK |
| `Vlanif` | VLAN |
| `Eth-Trunk` | AGGREGATED |

### Static Route Conversion

Static routes are converted to the vendor-independent model with proper prefix calculation:

```java
Prefix prefix = Prefix.create(route.getNetwork(), route.getMask());
StaticRoute.builder()
    .setNetwork(prefix)
    .setNextHopIp(route.getNextHop())
    .build();
```

## References

- [Parsing Documentation](../README.md)
- [Implementation Guide](../implementation_guide.md)
- [ANTLR4 Tips and Tricks](../antlr4_tips.md)
