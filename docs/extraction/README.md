# Extraction

In Batfish, "extraction" refers to the process of populating vendor-specific network configuration
data structures from one or more [parse trees](../parsing/README.md) of DSL network configuration
files.

The configuration of some network devices may also include files in well-known structured formats
(e.g. JSON, YAML). The process of converting such files into vendor-specific data structures is
outside of the scope of this document, covered instead
[here](../parsing/README.md#adding-support-for-structured-file-formats).

The vendor-specific data structures produced by the extraction process are grouped into logical
units, each an instance of of `VendorConfiguration`.

## Single DSL file to single VendorConfiguration

In the simple case, a `VendorConfiguration` is produced from a single (DSL or well-known structured)
input file. In general, however, information
from [multiple files](#multiple-dsl-files-to-single-vendorconfiguration) may be combined into a
single `VendorConfiguration`.

The dataflow for producing a single VendorConfiguration from a single file is as follows:

- file --parse-> parse tree --extract--> VendorConfiguration

Extraction is perfomed by a vendor-specific implementation of `ControlPlaneExtractor`.

In the single file case, a `ControlPlaneExtractor` creates a `VendorConfiguration` and passes it and
the parse tree to a sequence of one or more `BatfishListener`s, which extend the
ANTLR4 `ParseTreeListener` interface. In the single-file case, all except the final listener are
pre-processors that may alter the parse tree and produce state needed by later stages. Then the last
listener extracts data from the parse tree into a `VendorConfiguration`.

Pre-processing steps are necessary when:

- the parse tree contains commands that are best modeled as removing or disabling portions of the
  parse tree, e.g. the Juniper `delete` and `deactivate` commands.
- the parse tree contains commands that effectively generate syntax, e.g. the Juniper `apply-groups`
  command.

## Multiple DSL files to single VendorConfiguration

This section of the documentation is still in progress. Check back later!

## Writing a listener

In ANTLR4 parsing terminology, a listener is an agent that interposes actions during a walk of a
parse tree by a `ParseTreeWalker` (`BatfishParseTreeWalker` in our case). The walk starts at the
root of the parse tree and does
a [depth-first traversal](https://en.wikipedia.org/wiki/Tree_traversal#Depth-first_search).

For every parser rule `some_rule` in a parser grammar, the listener defines two functions:

- `void enterSome_rule(Some_ruleContext ctx)`: executed at the beginning of the traversal of
  a `some_rule` node before its children are visited
- `void exitSome_rule(Some_ruleContext ctx)`: executed at the end of the traversal of a `some_rule`
  node after all its children have been visited.

Each such function has a default empty implementation that may be overridden to perform useful
work.

In the Batfish project, we have adopted the convention that only an `exit` function be overridden
unless there is a strict need to do work prior to visiting children.

For example, the
[listener](../example_code/new_vendor/src/main/java/org/batfish/grammar/cool_nos/CoolNosConfigurationBuilder.java)
for [Cool NOS](../parsing/README.md) defines the following overrides for the `ss_enable`
and `ss_modify` parser rules respectively:

```
  @Override
  public void exitSs_enable(Ss_enableContext ctx) {
    _currentStaticRoute.setEnable(true);
  }
```

```
  @Override
  public void enterSs_modify(Ss_modifyContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    if (!_c.getStaticRoutes().containsKey(prefix)) {
      warn(ctx, String.format("Attempt to modify non-existent static route for prefix %s", prefix));
      // set to a dummy so modification further down the parse tree does not NPE
      _currentStaticRoute = new StaticRoute();
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

```

Note that:

- Only the exit function is overriden for `ss_enable`.
- Both the enter and exit functions are overridden for `ss_modify`. The `enter` function is needed
  because it prepares state used when visiting the `ss_enable` node that is a descendent of an
  `ss_modify` node. Specifically, `_currentStaticRoute` is set and used by `exitSs_enable`.
- Even when the modify line is invalid because it refers to a non-existent static route,
  `_currentStaticRoute` is set to a dummy value so that the code in `exitSs_enable` does not throw
  an NPE. The dummy static route is garbage collected after `_currentStaticRoute` is set to `null`
  in `exitSs_modify`. You will see this pattern used extensively in Batfish listeners.

### Validating and converting parse tree nodes with variable text

It is often that case that a parse tree will contain nodes for commands that include rules/tokens
whose values may not be validated at parsing-time for practical reasons. For such cases, it is the
responsibility of the extractor to identify and react to the invalid values.

For example, in the Cool NOS grammar, the name of a VLAN interface is specifed as `vlan <number>`,
where `<number>` is a number between 1-4094. It is not practical for the lexer/parser to enforce
that vlan numbers in the input text are in this range, so the Cool NOS extractor performs the
validation shown in this excerpt:

```
  @Override
  public void exitSsa_interface(Ssa_interfaceContext ctx) {
    toString(ctx, ctx.interface_name())
        .ifPresent(name -> _currentNextHop = new NextHopInterface(name));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_nameContext ctx) {
    if (ctx.ETHERNET() != null) {
      return Optional.of(String.format("ethernet %d", toInteger(ctx.ethernet_num)));
    } else {
      assert ctx.VLAN() != null;
      Optional<Integer> maybeVlan = toInteger(messageCtx, ctx.vlan);
      if (!maybeVlan.isPresent()) {
        // already warned
        return Optional.empty();
      }
      return Optional.of(String.format("vlan %d", maybeVlan.get()));
    }
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vlan_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
  }

  /**
   * Convert a {@link Uint16Context} to an {@link Integer} if it is contained in the provided {@code
   * space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace_helper(messageCtx, ctx, space, name);
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 32-bit
   * decimal integer to an {@link Integer} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   *
   * <p>This function should only be called by more strictly typed overloads of {@code
   * toIntegerSpace}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace_helper(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4094));
```

The relevant parser rules are included for clarity:

```
// parser
ssa_interface: INTERFACE name = interface_name;

interface_name
:
  ETHERNET ethernet_num = uint8
  | VLAN vlan = vlan_number
;

vlan_number
:
  // 1-4094, extractor should validate number
  uint16
;

uint16
:
  UINT8
  | UINT16
;
```

In this case, the extractor is trying to convert the `interface_name` child node of
the `ssa_interface` node into a `String`. The parser and lexer guarantee that the text content
of an `interface_name` node is of the form `ethernet 0` through `ethernet 255` or `vlan 0` through
`vlan 65535`. But the number portion of the name of a vlan interface must be further restricted
to between 1-4094.

First let's consider the case that the configuration file contains a valid vlan interface number:

- In `exitSsa_interface`, the `toString(ParserRuleContext messageCtx, Interface_nameContext ctx)`
  function is used to convert
  the `interface_name` node (`Interface_nameContext` Java class) to a Java `String`.
- In `toString(ParserRuleContext messageCtx, Interface_nameContext ctx)`, `ctx.ETHERNET()` will be
  `null`, so the `else` branch is executed. In this branch, we assert `ctx.VLAN()` as a sanity check
  on the grammar. Then `vlan` alias of `interface_name` - which is of node type `vlan_number` is
  converted via `toInteger(ParserRuleContext messageCtx, Vlan_numberContext ctx)`.
- The `toInteger(ParserRuleContext messageCtx, Vlan_numberContext ctx)` converts the `vlan_number`
  to an integer via `toIntegerInSpace(...,Uint16Context ctx,...)` on `vlan_number`'s `uint16` child
  node.
- The `toIntegerInSpace(...,Uint16Context ctx,...)` converts the `uint`
  to an integer via `toIntegerInSpace_helper`.
- `toIntegerInSpace_helper` parses the text of the passed in `Uint16Context`. Note that
  while `Integer.parseInt` may throw in general, the parser and caller together guarantee it will
  not, since the input will be a node of type `uint16`, whose text is within 0-65535. The function
  checks whether the parsed integer is in the provided range, `VLAN_NUMBER_RANGE`, which is an
  `IntegerSpace` containing the closed interval from 1 to 4094. Since we assumed at the outset the
  text represented a valid vlan number, this function will return an `Optional` of that number.
- The value is returned down the stack to `exitSsa_interface`. Since it is present, the `ifPresent`
  action is performed: ` _currentNextHop = new NextHopInterface(name)`.

Now consider the case where the input text contained an invalid vlan interface, say `vlan 50000`.

- The control flow is the same as above until the range check within `VLAN_NUMBER_RANGE`.
- This time, `!space.contains(num)` returns `true`, so a warning is emitted and `Optional.empty()`
  is returned.
- In `exitSsa_interface`, the `ifPresent` action is not executed, and the function concludes. No
  data
  structure is modified, and the function returns.

For completeness, let us finally consider the case where the input text contained a number far out
of range, say `vlan 100000`. Since 100000 won't be parsed as a `uint16`, no `vlan_number` node will
be created, therefore no `interface_name`, and no `ssa_interface`. None of the functions above will
be executed. Instead, an error node will be inserted into the parse tree at parsing time as a child
of `ssa_interface` as a sibling of any `ss` nodes (assuming the parser has been patterned correctly
and line-based recovery is working properly).

The user still must be warned about the invalid syntax in the input file. This is done via the
special listener override `visitErrorNode`, which is executed whenever the `ParseTreeWalker`
encounters an error node:

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
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlagf(
          "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
          line, lineText);
    }
  }
```

Note that Batfish's line-based recovery will produce an error node whose `getSymbol` function will
return a token whose text contains the entire line that was unrecognized. The user in this case
will receive a generic warning about an unrecognized line with its text and location. And finally
note that the user would receive a warning of the same form if any other unparseable value was
entered for the vlan number, e.g. `vlan awholebunchofgarbage`, `vlan` (forgot the number).

--

It is worth emphasizing that in the excerpts above, `getText()` is only ever called once in the deep
generic converter function `toIntegerInSpace_helper`. In general, you should avoid
calling `getText()` on `Token` or `ParserRuleContext` instances except in such helper functions. To
the extent possible, prefer to write and use intermediate converter functions that operate on typed
parse tree nodes (subclasses of `ParserRuleContext`). This has multiple benefits:

- If handling of the raw text changes (e.g. you want to add canonicalization of numbers of strings),
  code only needs to be updated in one place.
- If the type of a child node (or alias of a child node) changes, the converter-calling code will no
  longer compile. You will immediately see where changes need to be made, rather than having to wait
  for a test to fail, or worse - something to fail in production.

### Unimplemented warnings in extraction

Sometimes there are cases where a line has been added to the grammar, but it still needs further implementation.
In these cases, we still want to leave a warning for this line.

A generic `todo` in extraction automatically adds an unimplemented warning:

```java
  @Override
  public void exitS_some_line(S_some_lineContext ctx) {
    todo(ctx);
  }
```

or alternatively, add a custom warning, to give further details:

```java
...
warn(ctx, "This line is unimplemented for reasons");
...
```

## Testing Extraction

Testing extraction is a critical part of the development process. This section provides guidance on how to write effective extraction tests.

### Basic Extraction Test Structure

Extraction tests typically follow this pattern:

1. Parse a configuration file using a vendor-specific parser
2. Access the vendor-specific configuration objects
3. Verify that specific properties have been correctly extracted using assertions

Here's an example of a basic extraction test for a Juniper configuration:

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

#### Testing State 2: `_null` suffix rules

Even though `_null` suffix rules don't require extraction code, they should still be tested to ensure they parse correctly without warnings:

```java
@Test
public void testNullRuleParsing() {
  // Should parse without warnings
  parseConfig("null-rule-test");
}
```

#### Testing State 3: Unimplemented with warnings

For rules that use `todo()` or `warn()`, test that appropriate warnings are generated:

```java
@Test
public void testUnimplementedFeature() {
  // Parse the configuration
  Warnings warnings = parseConfig("unimplemented-feature-test").getWarnings();

  // Verify warnings were generated
  assertThat(
      warnings.getUnimplementedWarnings(),
      hasItem(hasText(containsString("unimplemented feature"))));
}
```

### Test Configuration Files

Test configuration files should be:

1. Minimal - Include only what's needed to test the feature
2. Focused - Test one feature or aspect at a time
3. Clear - Use descriptive names and comments

Example test configuration for a Juniper interface feature:

```
set system host-name ethernet-switching-parsing
set interfaces ge-0/0/0 family ethernet-switching recovery-timeout 180
```

### Common Testing Patterns

1. **Positive testing**: Verify that valid configurations are correctly extracted
2. **Negative testing**: Verify that invalid configurations generate appropriate warnings
3. **Edge cases**: Test boundary values and special cases
4. **Interaction testing**: Test how features interact with each other

### Testing Tools

The `parseConfig` and `parseJuniperConfig` methods in test classes provide convenient ways to parse test configurations. These methods handle the details of setting up the parser, extractor, and warnings collection.
