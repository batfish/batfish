# Huawei VRP Parser - Quick Reference

## For Developers Working on Huawei Parser

## File Locations

### Grammar Files (`.g4`)
```
projects/batfish/src/main/antlr4/org/batfish/grammar/huawei/
```

### Parser Java Files
```
projects/batfish/src/main/java/org/batfish/grammar/huawei/
├── HuaweiCombinedParser.java          # Parser wrapper
├── HuaweiControlPlaneExtractor.java   # Parse tree walker
└── parsing/
    ├── HuaweiBaseLexer.java           # Base lexer
    └── HuaweiBaseParser.java          # Base parser
```

### Representation Files
```
projects/batfish/src/main/java/org/batfish/representation/huawei/
├── HuaweiConfiguration.java           # Main config class
├── HuaweiConversions.java             # Convert to Batfish model
├── HuaweiInterface.java               # Interface representation
├── HuaweiBgpProcess.java              # BGP representation
├── HuaweiOspfProcess.java             # OSPF representation
└── ... (other feature representations)
```

### Test Files
```
projects/batfish/src/test/java/org/batfish/grammar/huawei/
└── HuaweiGrammarTest.java

projects/batfish/src/test/resources/org/batfish/grammar/huawei/testconfigs/
└── basic-interface/
    └── test-config
```

## Common Tasks

### Adding a New Command to the Grammar

1. **Add token to `HuaweiLexer.g4`:**
   ```antlr
   MY_COMMAND: 'my-command';
   ```

2. **Add parser rule to appropriate `Huawei_<feature>.g4`:**
   ```antlr
   s_my_command
   :
     MY_COMMAND ...
   ;
   ```

3. **Add to `statement` rule in `HuaweiParser.g4`:**
   ```antlr
   statement
   :
     s_my_command
     | ...
   ;
   ```

### Implementing Extraction for a Command

1. **Add field to representation class:**
   ```java
   private @Nullable String _myField;
   public @Nullable String getMyField() { return _myField; }
   public void setMyField(String myField) { _myField = myField; }
   ```

2. **Add listener method to `HuaweiControlPlaneExtractor.java`:**
   ```java
   @Override
   public void exitS_my_command(HuaweiParser.S_my_commandContext ctx) {
     // Extract value from ctx
     String value = ctx.STRING().getText();
     // Set on appropriate configuration object
     _configuration.setMyField(value);
   }
   ```

3. **Implement conversion in `HuaweiConversions.java`:**
   ```java
   private static void convertMyField(
       HuaweiConfiguration huaweiConfig,
       Configuration vendorIndependentConfig) {
     String myValue = huaweiConfig.getMyField();
     if (myValue != null) {
       // Convert to vendor-independent format
       vendorIndependentConfig.setSomeProperty(myValue);
     }
   }
   ```

4. **Call conversion method in `toVendorIndependentConfiguration()`:**
   ```java
   convertMyField(huaweiConfig, c);
   ```

### Adding Tests

1. **Create test configuration:**
   ```
   projects/batfish/src/test/resources/org/batfish/grammar/huawei/testconfigs/my-test/
   └── test-config
   ```

2. **Add test method to `HuaweiGrammarTest.java`:**
   ```java
   @Test
   public void testMyCommand() {
     HuaweiConfiguration config = parseHuaweiConfig("my-test");
     // Assert extraction worked
     assertThat(config.getMyField(), equalTo("expected-value"));
   }
   ```

## Building and Testing

### Build Parser (after grammar changes)
```bash
bazel build //projects/batfish/src/main/antlr4/org/batfish/grammar/huawei:huawei
```

### Run Huawei Tests
```bash
# All Huawei tests
bazel test //projects/batfish/src/test/java/org/batfish/grammar/huawei:...

# Specific test
bazel test --test_filter=HuaweiGrammarTest#testMyCommand \
  //projects/batfish/src/test/java/org/batfish/grammar/huawei:tests
```

### Run All Tests (after integration changes)
```bash
bazel test //...
```

## Huawei VRP Syntax Reference

### Interface Naming
- `GigabitEthernet0/0/0` - Physical interface (slot/subcard/port)
- `GigabitEthernet0/0/0.100` - Subinterface
- `Vlanif100` - VLAN interface
- `LoopBack0` - Loopback interface
- `Eth-Trunk1` - Port channel

### Key Commands
- `sysname <name>` - Set hostname
- `interface <name>` - Enter interface configuration
- `ip address <ip> <mask>` - Set IP address
- `description <text>` - Set description
- `undo <command>` - Negate a command
- `return` - Exit to user view

### Configuration Structure
```
sysname Router1
#
interface GigabitEthernet0/0/0
 description To-Core
 ip address 192.168.1.1 255.255.255.0
#
return
```

## Debugging Tips

### Check Parse Tree
```java
// In HuaweiControlPlaneExtractor
@Override
public void exitS_my_command(HuaweiParser.S_my_commandContext ctx) {
  // Debug: print parse tree
  System.err.println(ctx.toStringTree(_parser));
}
```

### Check Tokenization
```bash
# Test lexer alone
echo "sysname Router1" | \
  bazel-bin/projects/batfish/src/main/antlr4/org/batfish/grammar/huawei/huawei_generated-src.jar
```

### Enable ANTLR Debug Output
```bash
bazel test --test_output=all //projects/batfish/src/test/java/org/batfish/grammar/huawei:...
```

## Integration Points

### Adding Huawei to a Switch Statement
```java
switch (format) {
  case HUAWEI:
    {
      HuaweiCombinedParser huaweiParser = new HuaweiCombinedParser(fileText, _settings);
      ControlPlaneExtractor extractor =
          new HuaweiControlPlaneExtractor(
              fileText,
              huaweiParser,
              _fileResults.get(filename).getWarnings(),
              _fileResults.get(filename).getSilentSyntax());
      // ... process
      break;
    }
}
```

### Detecting Huawei Format
```java
private static final Pattern HUAWEI_PATTERN = Pattern.compile("(?m)^sysname\\s+");

private @Nullable ConfigurationFormat checkHuawei() {
  if (fileTextMatches(HUAWEI_PATTERN)) {
    return ConfigurationFormat.HUAWEI;
  }
  return null;
}
```

## Phase Implementation Status

- ✅ **Phase 1**: Foundation (COMPLETE)
- 🔄 **Phase 2**: System and Interfaces (NEXT)
- ⏳ **Phase 3**: VLANs and Subinterfaces
- ⏳ **Phase 4**: Static Routes
- ⏳ **Phase 5**: BGP
- ⏳ **Phase 6**: OSPF
- ⏳ **Phase 7**: ACLs
- ⏳ **Phase 8**: NAT
- ⏳ **Phase 9**: VRF
- ⏳ **Phase 10**: Polish and Advanced Features

## Documentation Links

- **Phase Summary:** `docs/parsing/vendors/huawei_phase1_summary.md`
- **Grammar Overview:** `docs/parsing/vendors/huawei.md`
- **Implementation Guide:** `docs/parsing/implementation_guide.md`
- **Parser Conventions:** `docs/parsing/parser_rule_conventions.md`
- **General Parsing:** `docs/parsing/README.md`

## Getting Help

1. Check Cisco parser as reference (similar structure)
2. Read parser documentation in `docs/parsing/`
3. Look at existing Huawei grammar files
4. Check implementation plan for phased approach
