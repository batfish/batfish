# Adding Your First Vendor Support

**Time**: 2-4 hours | **Difficulty**: Intermediate

This tutorial teaches you how to add support for a new network vendor or device type to Batfish. You'll create a minimal parser for a hypothetical vendor called "AcmeNOS".

## What You'll Build

By the end of this tutorial, you'll have:
- Created ANTLR4 lexer and parser grammars
- Implemented a basic extractor
- Converted to vendor-independent format
- Written tests
- Integrated your parser with Batfish

**Why AcmeNOS?**
- It's hypothetical, so we can focus on concepts
- Simpler than real vendors (no edge cases)
- Demonstrates the full pipeline

---

## Prerequisites

Before starting, ensure you have:

1. **ANTLR4 basics**: Understand lexer/parser concepts
2. **Java experience**: Comfortable writing Java code
3. **Read parsing docs**: [Parsing README](../parsing/README.md)
4. **Read implementation guide**: [Implementation Guide](../parsing/implementation_guide.md)

**New to ANTLR4?** Review the [example code](../example_code/new_vendor/) first.

---

## Overview: The Pipeline

Adding vendor support involves 4 stages:

```
1. Parsing:     Config text → Parse tree (ANTLR)
2. Extraction:  Parse tree → Vendor-specific structures
3. Conversion:  Vendor-specific → Vendor-independent
4. Testing:     Verify everything works
```

Let's go through each stage.

---

## Step 1: Understand AcmeNOS Configuration Format

Our hypothetical vendor has this config format:

```bash
# Sample AcmeNOS configuration
hostname router1
interface eth0
  ip address 10.0.0.1/24
  exit
router bgp 65001
  neighbor 10.0.0.2 remote-as 65002
  exit
```

**Key observations**:
- Space-separated (not line-based like Juniper)
- Uses `exit` to end blocks
- Simple interface syntax
- Basic BGP configuration

---

## Step 2: Create Directory Structure

```bash
# Create vendor directory using vendor-scoped pattern
mkdir -p projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar
mkdir -p projects/batfish/src/main/java/org/batfish/vendor/acme/grammar
mkdir -p projects/batfish/src/main/java/org/batfish/vendor/acme/representation
mkdir -p projects/batfish/src/test/java/org/batfish/vendor/acme/grammar
```

**Note**: This uses the vendor-scoped pattern recommended for new vendors: all code lives under
`org.batfish.vendor.acme`, with the grammar, parser, and extractor in the `grammar` subpackage and
the vendor-specific data model in the `representation` subpackage. See
[Vendor Code Organization](../parsing/README.md#vendor-code-organization) in the parsing
documentation.

---

## Step 3: Create the Lexer

Create: `projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar/AcmeLexer.g4`

```antlr
lexer grammar AcmeLexer;

// Keywords
HOSTNAME: 'hostname';
INTERFACE: 'interface';
IP: 'ip';
ADDRESS: 'address';
ROUTER: 'router';
BGP: 'bgp';
NEIGHBOR: 'neighbor';
REMOTE_AS: 'remote-as';
EXIT: 'exit';

// Literals
WORD: [a-zA-Z][a-zA-Z0-9._-]*;
NUMBER: [0-9]+;
IPV4: [0-9]+ '.' [0-9]+ '.' [0-9]+ '.' [0-9]+;
IPV4_PREFIX: IPV4 '/' [0-9]+;

// Whitespace and comments
WS: [ \t\r\n]+ -> skip;
COMMENT: '#' ~[\r\n]* -> skip;
```

**Key points**:
- Keywords are uppercase (convention)
- Skip whitespace and comments
- Define token patterns for values

---

## Step 4: Create the Parser

Create: `projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar/AcmeParser.g4`

```antlr
parser grammar AcmeParser;

options {
    tokenVocab = AcmeLexer;
}

// Top-level configuration
config: line* EOF;

// A line can be:
// - hostname statement
// - interface block
// - router bgp block
line: hostname_stmt
     | interface_block
     | bgp_block
     | unrecognized_stmt_null
     ;

// Hostname statement
hostname_stmt: HOSTNAME WORD;

// Interface block
interface_block:
    INTERFACE WORD NEWLINE
    interface_line*
    EXIT
;

interface_line:
    ip_address_stmt
    | unrecognized_stmt_null
;

ip_address_stmt: IP ADDRESS IPV4_PREFIX;

// BGP block
bgp_block:
    ROUTER BGP NUMBER NEWLINE
    bgp_line*
    EXIT
;

bgp_line:
    neighbor_stmt
    | unrecognized_stmt_null
;

neighbor_stmt: NEIGHBOR IPV4 REMOTE_AS NUMBER;

// Lines we don't care about
unrecognized_stmt_null:
    (~EXIT | ~EXIT | ~EXIT)+ NEWLINE
;
```

**Key points**:
- `_null` suffix for ignored statements
- Block structure matches config format
- Use `NEWLINE` to terminate statements

---

## Step 5: Create Base Classes

Create: `projects/batfish/src/main/java/org/batfish/vendor/acme/grammar/AcmeCombinedParser.java`

```java
package org.batfish.vendor.acme.grammar;

import org.batfish.grammar.BatfishCombinedParser;

public class AcmeCombinedParser
    extends BatfishCombinedParser<AcmeParser, AcmeLexer> {

  public AcmeCombinedParser(String input) {
    super(AcmeParser.class, AcmeLexer.class, input);
  }
}
```

Create: `projects/batfish/src/main/java/org/batfish/vendor/acme/grammar/AcmeBatfishLexer.java`

```java
package org.batfish.vendor.acme.grammar;

import org.antlr.v4.runtime.CharStream;
import org.batfish.grammar.BatfishLexer;

public class AcmeBatfishLexer extends BatfishLexer {

  public AcmeBatfishLexer(CharStream input) {
    super(new AcmeLexer(input));
  }
}
```

---

## Step 6: Create the Extractor

Create: `projects/batfish/src/main/java/org/batfish/vendor/acme/grammar/AcmeExtractor.java`

```java
package org.batfish.vendor.acme.grammar;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.config.Range;
import org.batfish.config.StaticRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishListener;
import org.batfish.vendor.acme.grammar.AcmeParser.ConfigContext;
import org.batfish.vendor.acme.grammar.AcmeParser.Interface_blockContext;
import org.batfish.vendor.acme.grammar.AcmeParser.Ip_address_stmtContext;

public class AcmeExtractor extends BatfishListener {

  private final Warnings _w;
  private final String _text;
  private final Configuration _configuration;

  private String _currentInterface;

  public AcmeExtractor(AcmeCombinedParser parser, Warnings w) {
    _w = w;
    _text = parser.getInput();
    _configuration = new Configuration();
    _configuration.setConfigurationFormat(ConfigurationFormat.ACME);
  }

  @Override
  public void exitConfig(ConfigContext ctx) {
    // Finalize configuration
    _configuration.setDefaultCrossZoneAction(false);
  }

  @Override
  public void enterInterface_block(Interface_blockContext ctx) {
    String name = ctx.WORD().getText();
    _currentInterface = name;

    Interface iface = new Interface(name, _configuration);
    iface.setActive(true);
    iface.setAdminUp(true);
    _configuration.getInterfaces().put(name, iface);
  }

  @Override
  public void exitInterface_block(Interface_blockContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitIp_address_stmt(Ip_address_stmtContext ctx) {
    if (_currentInterface == null) {
      return; // IP outside interface block
    }

    String prefixStr = ctx.IPV4_PREFIX().getText();
    Prefix prefix = Prefix.parse(prefixStr);

    Interface iface = _configuration.getInterfaces().get(_currentInterface);
    iface.setAddress(prefix.getAddress());
    iface.setPrefixAndMask(prefix);
  }

  public Configuration getConfiguration() {
    return _configuration;
  }
}
```

---

## Step 7: Register Your Vendor

Edit: `projects/batfish/src/main/java/org/batfish/common/VendorConfiguration.java`

```java
// Add to vendor enum
ACME("acme", "AcmeNOS", ConfigurationFormat.ACME),

// Add to format enum
public enum ConfigurationFormat {
    // ... existing formats ...
    ACME("acme", "AcmeNOS Configuration", 5),
}
```

---

## Step 8: Create BUILD Files

Create: `projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar/BUILD.bazel`

```python
load("@rules_java//java:defs.bzl", "java_library")
load("@io_bazel_rules_antlr//antlr:defs.bzl", "antlr4_lexergen", "antlr4_parsergen")

package(default_visibility = ["//visibility:public"])

antlr4_lexergen(
    name = "AcmeLexer",
    srcs = ["AcmeLexer.g4"],
    visitor = True,
)

antlr4_parsergen(
    name = "AcmeParser",
    srcs = ["AcmeParser.g4"],
    visitor = True,
    package = "org.batfish.vendor.acme.grammar",
)

java_library(
    name = "acme_grammar",
    srcs = [":AcmeLexer", ":AcmeParser"],
    deps = [
        "//third_party/antlr:antlr-runtime",
    ],
)
```

Create: `projects/batfish/src/main/java/org/batfish/vendor/acme/grammar/BUILD.bazel`

```python
package(default_visibility = ["//visibility:public"])

java_library(
    name = "grammar",
    srcs = glob(["*.java"]),
    deps = [
        "//projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar:acme_grammar",
        "//projects/batfish/src/main/java/org/batfish/vendor/acme/representation",
        "//projects/common/src/main/java/org/batfish/common",
        "//projects/batfish/src/main/java/org/batfish/datamodel",
        "//third_party/antlr:antlr-runtime",
        "//third_party/guava",
    ],
)
```

Place the vendor-specific data model classes (referenced by the extractor) under
`projects/batfish/src/main/java/org/batfish/vendor/acme/representation/` with their own
`BUILD.bazel` defining a `representation` library.

---

## Step 9: Write Tests

Create: `projects/batfish/src/test/java/org/batfish/vendor/acme/grammar/AcmeGrammarTest.java`

```java
package org.batfish.vendor.acme.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.batfish.common.Warnings;
import org.batfish.config.StaticRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.junit.Test;

public class AcmeGrammarTest {

    @Test
    public void testInterfaceExtraction() {
        String config = "hostname router1\n"
                      + "interface eth0\n"
                      + "  ip address 10.0.0.1/24\n"
                      + "  exit\n";

        AcmeCombinedParser parser = new AcmeCombinedParser(config);
        Warnings warnings = new Warnings();

        AcmeExtractor extractor = new AcmeExtractor(parser, warnings);
        parser.parse().walk(extractor);

        Configuration c = extractor.getConfiguration();

        assertEquals("router1", c.getHostname());
        assertNotNull(c.getInterfaces().get("eth0"));

        Interface eth0 = c.getInterfaces().get("eth0");
        assertEquals("10.0.0.1", eth0.getAddress().getHostAddress());
        assertEquals(24, eth0.getPrefixLen());
    }
}
```

Run tests:

```bash
bazel test //projects/batfish/src/test/java/org/batfish/vendor/acme/grammar:tests
```

---

## Step 10: Test with Real Configs

Create test configs directory:

```bash
mkdir -p projects/batfish/src/test/resources/test_configs/vendor/acme/simple
```

Create: `projects/batfish/src/test/resources/test_configs/vendor/acme/simple/router.cfg`

```bash
hostname core-router-01
interface eth0
  ip address 10.0.0.1/24
  exit
interface eth1
  ip address 10.0.1.1/24
  exit
router bgp 65001
  neighbor 10.0.0.2 remote-as 65002
  exit
```

Add integration test:

```java
@Test
public void testFullConfig() {
    String configText = Files.readString(
        Paths.get("projects/batfish/src/test/resources/test_configs/vendor/acme/simple/router.cfg")
    );

    AcmeCombinedParser parser = new AcmeCombinedParser(configText);
    Warnings warnings = new Warnings();

    AcmeExtractor extractor = new AcmeExtractor(parser, warnings);
    parser.parse().walk(extractor);

    Configuration c = extractor.getConfiguration();

    assertEquals("core-router-01", c.getHostname());
    assertEquals(2, c.getInterfaces().size());
    assertNotNull(c.getInterfaces().get("eth0"));
    assertNotNull(c.getInterfaces().get("eth1"));
}
```

---

## Step 11: Build and Verify

```bash
# Build Acme parser
bazel build //projects/batfish/src/main/java/org/batfish/vendor/acme/...

# Run tests
bazel test //projects/batfish/src/test/java/org/batfish/vendor/acme/...

# Build all of Batfish
bazel build //projects/allinone:allinone_main

# Test with Batfish service
bazel run //projects/allinone:allinone_main
```

Test via Pybatfish:

```python
from pybatfish.client.session import Session

bf = Session(host="localhost")
bf.init_snapshot('test_configs/', name='acme-test', platform='acme')

nodes = bf.nodes()
print(f"Found {len(nodes)} nodes")
```

---

## Common Issues and Solutions

### Issue: Grammar won't compile

**Check**:
1. Are all lexer rules defined?
2. Are parser rules referencing correct lexer tokens?
3. Any left recursion?

**Fix**: Review [Parser Conventions](../parsing/parser_rule_conventions.md)

---

### Issue: Parse tree is empty

**Symptoms**: `ctx.getChildCount()` returns 0

**Fix**: Make sure your top-level rule matches everything:

```antlr
config: line* EOF;  // EOF ensures we consume everything
```

---

### Issue: Extractor doesn't get called

**Symptoms**: No print statements in extractor execute

**Fix**: Make sure you're walking the parse tree:

```java
parser.parse().walk(extractor);
```

---

### Issue: Tests pass but real configs fail

**Debug**:
1. Test with minimal config
2. Add complexity gradually
3. Check for vendor-specific quirks

**Common quirks**:
- Hidden characters
- Vendor-specific defaults
- Configuration variations

---

## Next Steps

Now that you have basic parsing working:

1. **Add more features**: BGP, OSPF, ACLs, etc.
2. **Handle edge cases**: Malformed configs, vendor extensions
3. **Improve coverage**: Add more test cases
4. **Add documentation**: Document vendor-specific quirks

### Adding BGP Support

The skeleton below shows the *shape* of BGP extraction. **It is deliberately
naive** — `Long.parseLong`/`Ip.parse` throw on bad input, it extracts straight
into the VI model, and it has no peer-group inheritance or reference tracking.
Before using it for real, apply the
[production-readiness checklist](#before-you-ship-production-readiness-checklist)
below: parse values safely, resolve group→neighbor inheritance on the vendor
model, and track structures.

```java
// In extractor — SKELETON ONLY; see the checklist before shipping
@Override
public void exitBgp_block(AcmeParser.Bgp_blockContext ctx) {
    String asn = ctx.NUMBER().getText();
    BgpProcess bgp = new BgpProcess();
    bgp.setAs(Long.parseLong(asn));
    _configuration.setBgpProcess(bgp);
}

@Override
public void exitNeighbor_stmt(AcmeParser.Neighbor_stmtContext ctx) {
    String ip = ctx.IPV4().getText();
    String remoteAs = ctx.NUMBER().getText();

    BgpNeighbor neighbor = new BgpNeighbor();
    neighbor.setIp(Ip.parse(ip));
    neighbor.setAs(Long.parseLong(remoteAs));

    _configuration.getBgpProcess().getNeighbors().put(ip, neighbor);
}
```

---

## Before you ship: production-readiness checklist

This tutorial gets you a parser that produces a model for clean input. A real
vendor needs more, and the gaps are easy to miss because the happy path looks
done. Each item below is a capability the rest of Batfish (questions, the
`annotate` tool, operators reading warnings) depends on; each was a real
correction made while adding the first hierarchical vendor (Nokia SR-OS).

1. **Separate the vendor representation from the VI model.** This tutorial
   extracts straight into a VI `Configuration` to stay short. Real vendors extract
   into a **vendor-specific representation** first, then convert to the VI model in
   a distinct step. The split is where vendor quirks live — inheritance, default
   policies, physical/logical interface splits — and keeps extraction free of
   conversion logic. See [Extraction](../extraction/README.md) and
   [Conversion](../conversion/README.md).

2. **Characterize the OS before writing the grammar.** Understand the config
   format (flat vs hierarchical, whether forms can be mixed), defaults (what
   "absent" means), mutation/edit semantics, and inheritance (groups, templates,
   `apply-groups`) up front — from the vendor's authoritative schema (YANG/XSD)
   and docs, not guesses. This decides whether you need a preprocessing/flatten
   pass at all.

3. **Parse values safely — never `parse`, always `tryParse`/in-space.** Raw
   `Integer.parseInt`/`Ip.parse`/`Prefix.parse` throw on malformed input. Use
   `toIntegerInSpace`/`toLongInSpace` (range-checked against the schema's bounds),
   `Ip.tryParse`/`Prefix.tryParse`, and a static map + warn for enumerated leaves.
   A bad value should become a **line-stamped `ParseWarning`**, not a crash or a
   silent drop.

4. **Emit line-stamped `ParseWarning`s and wire `annotate`.** The
   [`annotate`](../../projects/batfish/src/main/java/org/batfish/main/annotate/Annotate.java)
   tool inlines warnings above their source line, reading `ParseWarning`s only —
   context-free red-flags are invisible to it. Emit value/extraction warnings via
   the `warn(ctx, …)` helper, and add your `ConfigurationFormat` to
   `Annotate.getCommentHeader` with the right comment character.

5. **Track named structures.** Every referenceable object (route-map, prefix-list,
   peer group, ACL, …) must be recorded with `defineStructure` /
   `referenceStructure` / `markConcreteStructure`. This powers the
   `definedStructures`, `undefinedReferences`, and unused-structure questions —
   which are silently empty for a vendor that skips it. See
   [Implementation Guide Pattern 4](../parsing/implementation_guide.md#pattern-4-structure-definition-and-reference-tracking).

6. **Carry source provenance if you extract from a derived tree.** If your
   extractor walks a tree you built (not the ANTLR parse tree), the
   `ParserRuleContext` is gone by extraction time — breaking **both** line-stamped
   warnings and structure tracking — unless you carry the context onto the tree.
   See [`vendors/sros.md`](../parsing/vendors/sros.md) §"Source provenance".

7. **Resolve inheritance on the model, before conversion.** Resolve peer
   group/template/`apply-groups` inheritance with a `doInherit`/`inheritFrom` pass
   on the vendor model (child fills unset attributes from parent, child wins), so
   conversion reads a fully-populated object. Per-property inline inheritance in
   conversion is the anti-pattern. Models: NX-OS `BgpVrfNeighborConfiguration`,
   SR-OS `BgpNeighbor.inheritFrom`.

8. **Close the loop with validation.** Validate the converted model against real
   device state (the lab-validation framework), and sickbay known gaps to issues
   rather than leaving them silent.

A worked example of all of the above on a real hierarchical vendor:
[`docs/parsing/vendors/sros.md`](../parsing/vendors/sros.md).

---

## Quick Reference

### File Structure

```
projects/batfish/
├── src/main/antlr4/org/batfish/vendor/acme/grammar/
│   ├── AcmeLexer.g4
│   └── AcmeParser.g4
├── src/main/java/org/batfish/vendor/acme/
│   ├── grammar/
│   │   ├── AcmeExtractor.java
│   │   ├── AcmeBatfishLexer.java
│   │   └── AcmeCombinedParser.java
│   └── representation/                # Vendor-specific data model
└── src/test/java/org/batfish/vendor/acme/grammar/
    └── AcmeGrammarTest.java
```

### Key Classes

- **Lexer**: Tokenizes config text
- **Parser**: Creates parse tree
- **Extractor**: Converts tree to Configuration
- **Configuration**: Batfish's vendor-neutral format

### Commands

```bash
# Build
bazel build //projects/batfish/src/main/java/org/batfish/vendor/acme/...

# Test
bazel test //projects/batfish/src/test/java/org/batfish/vendor/acme/...

# Run service
bazel run //projects/allinone:allinone_main
```

---

## Related Documentation

- [Parsing README](../parsing/README.md)
- [Implementation Guide](../parsing/implementation_guide.md)
- [Parser Conventions](../parsing/parser_rule_conventions.md)
- [Extractor Guide](../extraction/README.md)
- [Conversion Guide](../conversion/README.md)
- [Tutorial: Debugging Parser Issues](debugging_parser_issues.md)
