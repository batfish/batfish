# Extraction

In Batfish, "extraction" refers to the process of populating vendor-specific network configuration data structures from one or more [parse trees](../parsing/README.md) of DSL network configuration files.

The configuration of some network devices may also include files in well-known structured formats (e.g. JSON, YAML). The process of converting such files into vendor-specific data structures is outside of the scope of this document, covered instead [here](../parsing/README.md#adding-support-for-structured-file-formats).

The vendor-specific data structures produced by the extraction process are grouped into logical units, each an instance of `VendorConfiguration`.

## Table of Contents

- [Single vs. Multiple Files](#single-vs-multiple-files)
- [ControlPlaneExtractor Interface](#controlplaneextractor-interface)
- [Pre-processing](#pre-processing)
- [BatfishListener Patterns](#batfishlistener-patterns)
- [ParseTreeWalker Usage](#parsetreewalker-usage)
- [Common Extraction Utilities](#common-extraction-utilities)
- [Error Handling and Warnings](#error-handling-and-warnings)
- [Testing Extraction](#testing-extraction)

---

## Single vs. Multiple Files

### Single DSL File to Single VendorConfiguration

In the simple case, a `VendorConfiguration` is produced from a single (DSL or well-known structured) input file.

**Dataflow:**
```
file --parse--> parse tree --extract--> VendorConfiguration
```

Extraction is performed by a vendor-specific implementation of `ControlPlaneExtractor`.

In the single file case, a `ControlPlaneExtractor` creates a `VendorConfiguration` and passes it and the parse tree to a sequence of one or more `BatfishListener`s, which extend the ANTLR4 `ParseTreeListener` interface. In the single-file case, all except the final listener are pre-processors that may alter the parse tree and produce state needed by later stages. The final listener extracts data from the parse tree into a `VendorConfiguration`.

### Multiple DSL Files to Single VendorConfiguration

Many network devices use multiple configuration files that must be combined. The most prominent example is **Juniper**, which uses:

- Main configuration files (e.g., `juniper.conf`, `junos.conf`)
- Group configuration files (for apply-groups)
- Inherited configurations from other files
- Potential for insert/delete operations across files

#### Juniper Multi-File Pattern

Juniper configurations can span multiple files with complex relationships:

```java
// FlatJuniperControlPlaneExtractor.java
public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {
  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    Hierarchy hierarchy = new Hierarchy();

    // Pre-process the parse tree to handle:
    // - Insert/delete operations
    // - Deactivation commands
    // - Apply-groups inheritance
    PreprocessJuniperExtractor.preprocess(
        (Flat_juniper_configurationContext) tree, hierarchy, _parser, _w);

    // Build configuration from pre-processed tree
    ConfigurationBuilder cb = new ConfigurationBuilder(
        _parser, _text, _w, hierarchy.getTokenInputs(), _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
```

#### Multi-File Processing Steps

1. **Parse all files** - Each file is parsed into its own parse tree
2. **Merge trees** - Combine trees according to vendor-specific rules
3. **Preprocess merged tree** - Apply preprocessing operations
4. **Extract configuration** - Build vendor-specific configuration from processed tree

#### Other Multi-File Vendors

**Palo Alto** - Can manage multiple firewalls from Panorama:
- Primary configuration
- Managed device configurations
- Shared objects and templates

**CheckPoint** - Multiple management files:
- Main configuration
- Policy packages
- Object definitions

**Cisco** - Generally single-file, but can include:
- Main configuration
- Additional config snippets (in some cases)

---

## ControlPlaneExtractor Interface

The `ControlPlaneExtractor` interface is the primary abstraction for extraction.

**Location:** `projects/common/src/main/java/org/batfish/grammar/controlplane_extractor/ControlPlaneExtractor.java`

```java
public interface ControlPlaneExtractor {
  void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree);

  VendorConfiguration getVendorConfiguration();

  default Warnings getWarnings() {
    return null;
  }
}
```

### Implementing ControlPlaneExtractor

#### Simple Implementation (Single File)

```java
public class CoolNosControlPlaneExtractor implements ControlPlaneExtractor {
  private final CoolNosConfiguration _configuration;
  private final Warnings _warnings;

  public CoolNosControlPlaneExtractor(
      String filename,
      String text,
      CoolNosCombinedParser parser) {
    _warnings = new Warnings(true);
    _configuration = new CoolNosConfiguration(filename);
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    // No preprocessing needed
    ConfigurationBuilder cb = new ConfigurationBuilder(
        _parser, _text, _w, ImmutableMap.of(), false);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }

  @Override
  public CoolNosConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public Warnings getWarnings() {
    return _warnings;
  }
}
```

#### Complex Implementation (With Preprocessing)

```java
public class JuniperControlPlaneExtractor implements ControlPlaneExtractor {
  private final JuniperConfiguration _configuration;

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    Hierarchy hierarchy = new Hierarchy();

    // Preprocess to handle:
    // - Insert/delete operations
    // - Deactivate commands
    // - Apply-groups (with convergence)
    PreprocessJuniperExtractor.preprocess(tree, hierarchy, _parser, _w);

    // Extract from preprocessed tree
    ConfigurationBuilder cb = new ConfigurationBuilder(
        _parser, _text, _w, hierarchy.getTokenInputs(), _silentSyntax);
    new BatfishParseTreeWalker(_parser).walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
```

---

## Pre-processing

Some vendors require complex pre-processing of the parse tree before extraction can begin.

### When Pre-processing is Necessary

Pre-processing steps are necessary when:

- The parse tree contains commands that remove or disable portions of the tree
  - **Juniper `delete`**: Removes configuration statements
  - **Juniper `deactivate`**: Disables configuration statements
- The parse tree contains commands that generate syntax
  - **Juniper `apply-groups`**: Inherits configuration from other parts
- Configuration uses insert/replace operations
  - **Juniper `insert`**: Inserts configuration at specific paths

### Juniper Pre-processing Pipeline

**Location:** `projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/PreprocessJuniperExtractor.java`

```java
static void preprocess(
    Flat_juniper_configurationContext tree,
    Hierarchy hierarchy,
    FlatJuniperCombinedParser parser,
    Warnings w) {

  ParseTreeWalker walker = new BatfishParseTreeWalker(parser);

  // Step 1: Apply insert/delete operations (respecting order)
  InsertDeleteApplicator d = new InsertDeleteApplicator(parser, w);
  walker.walk(d, tree);

  // Step 2: Handle deactivation
  DeactivateTreeBuilder dtb = new DeactivateTreeBuilder(hierarchy);
  walker.walk(dtb, tree);

  ActivationLinePruner dp = new ActivationLinePruner();
  walker.walk(dp, tree);

  DeactivatedLinePruner dlp = new DeactivatedLinePruner(hierarchy);
  walker.walk(dlp, tree);

  // Step 3: Handle apply-groups inheritance (with convergence)
  ApplyGroupsMarker agm = new ApplyGroupsMarker(hierarchy, w);
  boolean changed;
  do {
    walker.walk(agm, tree);
    changed = GroupInheritor.inheritGroups(hierarchy, tree);
  } while (changed);

  GroupPruner.prune(tree);
}
```

### Pre-processing Components

#### InsertDeleteApplicator

Processes insert and delete operations in configuration:

```java
// Handles:
// set delete interfaces ge-0/0/0
// set interfaces ge-0/0/0 unit 0
// insert <before-stmt>:
//     new-statement
```

**Key operations:**
- Maintains `StatementTree` of configuration structure
- Processes insert/delete in correct order
- Validates target paths exist before operations

#### DeactivateTreeBuilder

Marks configuration paths as deactivated:

```java
// Handles:
// deactivate
// interfaces ge-0/0/0
// unit 0
```

**Key operations:**
- Tracks deactivated paths in `Hierarchy`
- Marks subtrees as inactive
- Preserves original line number information

#### ApplyGroupsMarker

Resolves apply-groups and inheritance:

```java
// Handles:
// apply-groups [group1, group2];
// set apply-groups group3;
```

**Key operations:**
- Resolves group definitions
- Inherits configuration from group members
- Converges in loops (groups can reference other groups)
- Marks group boundaries for later pruning

### Convergence Loops

Some pre-processing operations require convergence:

```java
boolean changed;
do {
  walker.walk(listener, tree);
  changed = checkForChanges();
} while (changed);
```

**Used for:**
- Apply-groups inheritance (groups can reference other groups)
- Template expansion (if templates reference other templates)

---

## BatfishListener Patterns

### BatfishListener Interface

All extraction listeners should implement `BatfishListener`:

```java
public interface BatfishListener {
  /** Get the input text (for error reporting) */
  String getInputText();

  /** Get the parser for error context */
  CombinedParser getParser();

  /** Get warnings object */
  Warnings getWarnings();
}
```

### Listener Best Practices

#### 1. Prefer exit() Methods

Override only `exit` methods unless you explicitly need `enter`:

```java
// GOOD: Only exit
@Override
public void exitSs_enable(Ss_enableContext ctx) {
  _currentStaticRoute.setEnable(true);
}

// GOOD: Both enter and exit when needed
@Override
public void enterSs_modify(Ss_modifyContext ctx) {
  _currentStaticRoute = getRoute(ctx.prefix);
}

@Override
public void exitSs_modify(Ss_modifyContext ctx) {
  if (_currentNextHop != null) {
    _currentStaticRoute.setNextHop(_currentNextHop);
  }
  _currentStaticRoute = null;
}
```

#### 2. Use Dummy Values to Prevent NPE

Set dummy values when a configuration references something that doesn't exist:

```java
@Override
public void enterSs_modify(Ss_modifyContext ctx) {
  Prefix prefix = toPrefix(ctx.prefix);
  if (!_c.getStaticRoutes().containsKey(prefix)) {
    warn(ctx, String.format("Attempt to modify non-existent static route for prefix %s", prefix));
    // Set dummy to prevent NPE in child nodes
    _currentStaticRoute = new StaticRoute();
    return;
  }
  _currentStaticRoute = _c.getStaticRoutes().get(prefix);
}
```

#### 3. Clean Up State in exit() Methods

Always reset state to prevent cross-contamination:

```java
@Override
public void exitSs_modify(Ss_modifyContext ctx) {
  // ... use _currentStaticRoute ...
  _currentStaticRoute = null;  // Clean up
}
```

#### 4. Avoid getText() Calls

Prefer typed helper functions over `getText()`:

```java
// BAD: Direct getText() usage
String vlanText = ctx.vlan().getText();
int vlanNum = Integer.parseInt(vlanText);

// GOOD: Typed helper
Optional<Integer> maybeVlan = toInteger(ctx, ctx.vlan());
if (maybeVlan.isPresent()) {
  int vlanNum = maybeVlan.get();
}
```

**Benefits:**
- Single place to update if parsing changes
- Compile-time errors if grammar changes
- Type safety and validation

### Writing a Listener

In ANTLR4, a listener interposes actions during a parse tree walk by a `ParseTreeWalker`. The walk starts at the root and does a depth-first traversal.

For every parser rule `some_rule` in a grammar, the listener defines:

- `void enterSome_rule(Some_ruleContext ctx)`: Executed before visiting children
- `void exitSome_rule(Some_ruleContext ctx)`: Executed after visiting children

Both have default empty implementations that can be overridden.

#### Listener Example

```java
public class CoolNosConfigurationBuilder extends CoolNosParserBaseListener
    implements BatfishListener {

  private final CoolNosConfiguration _c;
  private final Warnings _w;
  private StaticRoute _currentStaticRoute;

  @Override
  public void exitSs_enable(Ss_enableContext ctx) {
    _currentStaticRoute.setEnable(true);
  }

  @Override
  public void enterSs_modify(Ss_modifyContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    if (!_c.getStaticRoutes().containsKey(prefix)) {
      warn(ctx, "Attempt to modify non-existent static route");
      _currentStaticRoute = new StaticRoute(); // Dummy
      return;
    }
    _currentStaticRoute = _c.getStaticRoutes().get(prefix);
  }

  @Override
  public void exitSs_modify(Ss_modifyContext ctx) {
    if (_currentNextHop != null) {
      _currentStaticRoute.setNextHop(_currentNextHop);
    }
    _currentNextHop = null;
    _currentStaticRoute = null;
  }
}
```

### Validating and Converting Parse Tree Nodes

Many configurations include values that must be validated at extraction time.

#### Example: VLAN Interface Validation

VLAN interfaces require numbers 1-4094, but the lexer accepts 0-65535:

```java
@Override
public void exitSsa_interface(Ssa_interfaceContext ctx) {
  toString(ctx, ctx.interface_name())
      .ifPresent(name -> _currentNextHop = new NextHopInterface(name));
}

private Optional<String> toString(
    ParserRuleContext messageCtx, Interface_nameContext ctx) {

  if (ctx.ETHERNET() != null) {
    return Optional.of(String.format("ethernet %d", toInteger(ctx.ethernet_num)));
  } else {
    assert ctx.VLAN() != null;
    Optional<Integer> maybeVlan = toInteger(messageCtx, ctx.vlan);
    if (!maybeVlan.isPresent()) {
      return Optional.empty(); // Already warned
    }
    return Optional.of(String.format("vlan %d", maybeVlan.get()));
  }
}

private Optional<Integer> toInteger(
    ParserRuleContext messageCtx, Vlan_numberContext ctx) {
  return toIntegerInSpace(messageCtx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
}

private Optional<Integer> toIntegerInSpace(
    ParserRuleContext messageCtx, ParserRuleContext ctx,
    IntegerSpace space, String name) {

  int num = Integer.parseInt(ctx.getText());
  if (!space.contains(num)) {
    warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
    return Optional.empty();
  }
  return Optional.of(num);
}

private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4094));
```

#### Error Node Handling

Handle parse errors via `visitErrorNode`:

```java
@Override
public void visitErrorNode(ErrorNode errorNode) {
  Token token = errorNode.getSymbol();
  int line = token.getLine();
  String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
  _c.setUnrecognized(true);

  if (token instanceof UnrecognizedLineToken) {
    UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
    _w.getParseWarnings()
        .add(new ParseWarning(
            line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
  } else {
    _w.redFlagf("Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
        line, lineText);
  }
}
```

---

## ParseTreeWalker Usage

### Standard Pattern

```java
// Create walker
ParseTreeWalker walker = new BatfishParseTreeWalker(parser);

// Create and configure listener
MyListener listener = new MyListener(_configuration, _warnings, _parser);

// Walk the tree
walker.walk(listener, tree);
```

### BatfishParseTreeWalker Benefits

- Enhanced error reporting with context
- Better stack traces for debugging
- Automatic exception handling
- Line number preservation

### Multi-Pass Processing

Some extractions require multiple passes over the parse tree:

```java
ParseTreeWalker walker = new BatfishParseTreeWalker(parser);

// Pass 1: Mark group boundaries
ApplyGroupsMarker marker = new ApplyGroupsMarker(hierarchy, w);
walker.walk(marker, tree);

// Pass 2: Inherit groups
boolean changed;
do {
  changed = GroupInheritor.inheritGroups(hierarchy, tree);
} while (changed);

// Pass 3: Prune unused groups
GroupPruner.prune(tree);

// Pass 4: Extract configuration
ConfigurationBuilder cb = new ConfigurationBuilder(...);
walker.walk(cb, tree);
```

---

## Common Extraction Utilities

### StatementTree

Manages hierarchical configuration structures for insert/delete operations:

**Location:** `projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/StatementTree.java`

**Purpose:**
- Represents configuration as a tree structure
- Enables insert/delete at specific paths
- Maintains parent-child relationships

### Hierarchy

Tracks configuration structure and deactivated paths:

**Location:** `projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/Hierarchy.java`

**Purpose:**
- Tracks configuration paths
- Manages apply-groups inheritance
- Handles deactivated configuration
- Supports wildcard matching

**Key features:**
- Path recording for error reporting
- Wildcard node matching
- Deactivation tracking
- Token inputs mapping for position tracking

### ConfigurationBuilder

Base class for building vendor-specific configurations:

**Location:** `projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/ConfigurationBuilder.java`

**Purpose:**
- Provides common extraction functionality
- Manages configuration object creation
- Handles structure definitions and references
- Collects warnings

### InsertDeleteApplicator

Processes insert and delete operations:

**Location:** `projects/batfish/src/main/java/org/batfish/grammar/flatjuniper/InsertDeleteApplicator.java`

**Operations:**
- Insert: Add configuration at specific path
- Delete: Remove configuration at specific path
- Replace: Replace configuration at specific path
- Maintains statement order

---

## Error Handling and Warnings

### Warnings Class

**Location:** `projects/common/src/main/java/org/batfish/datamodel/Warnings.java`

**Severity levels:**

| Level | Usage |
|-------|--------|
| `PEDANTIC` | Minor issues, informational messages |
| `REDFLAG` | Serious issues that may affect analysis |
| `UNIMPLEMENTED` | Features not yet implemented |

**Categories:**
- `FATAL`: Critical errors
- `RISKY`: Potentially problematic configurations
- `MISCELLANEOUS`: Other warnings

### Warning Patterns

#### Standard Warning

```java
private void warn(ParserRuleContext ctx, String message) {
  _w.redFlagWarning(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      message);
}
```

#### Unimplemented Feature

```java
@Override
public void exitS_some_line(S_some_lineContext ctx) {
  todo(ctx); // Adds UNIMPLEMENTED warning
}

// Or custom:
@Override
public void exitS_newFeature(S_newFeatureContext ctx) {
  warn(ctx, "This line is unimplemented for reasons");
}
```

#### ParseWarning

For parsing-specific warnings with context:

```java
_w.getParseWarnings().add(
    new ParseWarning(
        line,                    // Line number
        lineText,                // Line text
        parserContext,           // Parser context
        "This syntax is unrecognized"));
```

### Error Handling Patterns

#### Graceful Degradation

Continue processing even when errors occur:

```java
@Override
public void exitS_someComplexRule(S_someComplexRuleContext ctx) {
  try {
    extractComplexFeature(ctx);
  } catch (Exception e) {
    warn(ctx, "Failed to extract complex feature: " + e.getMessage());
    // Set safe default
    _currentFeature.setDefaultValue();
  }
}
```

#### Validation Errors

```java
private Optional<Integer> toIntegerInSpace_helper(
    ParserRuleContext messageCtx, ParserRuleContext ctx,
    IntegerSpace space, String name) {

  int num = Integer.parseInt(ctx.getText());
  if (!space.contains(num)) {
    warn(messageCtx,
        String.format("Expected %s in range %s, but got '%d'", name, space, num));
    return Optional.empty();
  }
  return Optional.of(num);
}
```

---

## Testing Extraction

### Basic Extraction Test Structure

Extraction tests typically follow this pattern:

1. Parse a configuration file using a vendor-specific parser
2. Access the vendor-specific configuration objects
3. Verify that specific properties have been correctly extracted

```java
@Test
public void testFeatureExtraction() {
  // Parse the configuration
  JuniperConfiguration config = parseJuniperConfig("feature-test");

  // Access the relevant configuration objects
  Interface iface = config.getMasterLogicalSystem().getInterfaces().get("ge-0/0/0");

  // Verify properties were correctly extracted
  assertThat(iface.getSomeProperty(), equalTo(180));
}
```

### Testing Different Implementation States

#### Testing State 2: `_null` Suffix Rules

```java
@Test
public void testNullRuleParsing() {
  // Should parse without warnings
  parseConfig("null-rule-test");
}
```

#### Testing State 3: Unimplemented with Warnings

```java
@Test
public void testUnimplementedFeature() {
  Warnings warnings = parseConfig("unimplemented-feature-test").getWarnings();

  // Verify warnings were generated
  assertThat(
      warnings.getUnimplementedWarnings(),
      hasItem(hasText(containsString("unimplemented feature"))));
}
```

### Test Configuration Files

Test configurations should be:

1. **Minimal** - Include only what's needed to test the feature
2. **Focused** - Test one feature or aspect at a time
3. **Clear** - Use descriptive names and comments

Example:
```
set system host-name test-switch
set interfaces ge-0/0/0 family ethernet-switching recovery-timeout 180
```

### Common Testing Patterns

1. **Positive testing**: Verify valid configurations extract correctly
2. **Negative testing**: Verify invalid configurations generate warnings
3. **Edge cases**: Test boundary values and special cases
4. **Interaction testing**: Test feature interactions

---

## Related Documentation

- [Parsing](../parsing/README.md): Converting configs to parse trees
- [Conversion](../conversion/README.md): Vendor-specific to vendor-independent
- [Implementation Guide](../parsing/implementation_guide.md): Decision guide for commands
- [Post-processing](../post_processing/README.md): Finalizing configurations
