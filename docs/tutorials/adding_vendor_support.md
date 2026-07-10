# Adding Your First Vendor Support

**Time**: a weekend to a first parse; weeks to a validated vendor | **Difficulty**: Intermediate

This tutorial teaches the end-to-end workflow for adding a new network vendor to
Batfish. The running example is a small hypothetical vendor, **AcmeNOS**, kept
deliberately simple so the *shape* of each stage is visible. At every stage we
cross-link [`vendors/sros.md`](../parsing/vendors/sros.md) — the worked record of
adding Nokia SR-OS, the first real hierarchical vendor added against this guide — so
you can see the same idea on a production-grade config.

## The pipeline

Adding a vendor is not "write a grammar." It is walking a config through seven stages,
each with its own doc:

```
characterize → detect → parse → extract → convert → post-process → validate → (climb complexity)
```

| Stage | What it produces | Canonical doc |
| ----- | ---------------- | ------------- |
| **Characterize** | A doc of the OS's formats, defaults, inheritance, mutation, ordering | (this tutorial, Step 1) |
| **Detect** | `ConfigurationFormat` + a detector that recognizes the config | [parsing README §Adding a new DSL parser](../parsing/README.md#adding-a-new-dsl-parser-to-batfish) |
| **Parse** | A parse tree (ANTLR lexer + grammar) | [parsing README](../parsing/README.md) |
| **Extract** | A **vendor-specific** `VendorConfiguration` | [extraction README](../extraction/README.md) |
| **Convert** | The **vendor-independent** `Configuration` | [conversion README](../conversion/README.md) |
| **Post-process** | A finalized model ready for the data plane | [post-processing README](../post_processing/README.md) |
| **Validate** | Confidence the model matches a real device | the lab-validation framework |

The single most important rule, which the rest of this tutorial keeps returning to:
**extraction targets a vendor-specific representation, conversion targets the
vendor-independent model, and they are separate stages.**

You do not finish all of one stage before the next. The realistic loop is: get the
simplest config through *all* stages, validate it, then climb a complexity ladder
(interfaces → static → OSPF → eBGP → iBGP → route reflection → policy → misconfig),
re-running the pipeline for each newly-exercised feature. SR-OS was built this way; see
[`vendors/sros.md` §Feature coverage](../parsing/vendors/sros.md#feature-coverage).

---

## Prerequisites

1. **ANTLR4 basics**: lexer/parser concepts. New to it? Read the
   [example code](../example_code/new_vendor/) (the "CoolNOS" scaffold) and
   [ANTLR4 tips](../parsing/antlr4_tips.md) first.
2. **Java JDK 21 or later** (25 preferred), and comfort reading existing vendor code.
3. **Read the canonical docs first.** Read [Parsing](../parsing/README.md), the
   [Implementation Guide](../parsing/implementation_guide.md), and
   [Extraction](../extraction/README.md) before writing code — the steps below assume
   their concepts and link into them rather than repeating them.
4. **Pick your reference vendor by config *shape*** — see the box below.

> ### Which existing vendor should I study?
> **Cisco NX-OS** is the most mature vendor support in the repo, especially for
> line-oriented configs — it was rewritten by hand after years of learnings. Its key
> insight is worth internalizing for *any* vendor: **match the grammar *and* the
> vendor-specific representation to the semantics of the vendor's configuration up
> front.** When the model mirrors how the vendor actually thinks, parsing, extraction,
> and conversion all become straightforward.
>
> - **Hierarchical / brace-delimited configs** (Junos, Palo Alto, SR-OS): a single
>   logical statement spans multiple source lines. Study the Junos `StatementTree` and
>   its preprocessors, and get hands-on with `//tools:flatten`, `//tools:preprocess`,
>   and `//tools:annotate` to see how a real hierarchical config is transformed.
> - **Inheritance** (groups, templates, peer inheritance): study NX-OS or Junos BGP
>   templates/groups/peers.
>
> AcmeNOS below is line-oriented, so NX-OS is its closest reference.

---

## Step 1: Characterize the OS *before* writing a grammar

Characterization prevents more rework than any other step. Booting an emulator and
collecting one config proves your *toolchain* works — it does **not** characterize the
OS. Before any grammar, answer these (and write the answers down, with citations):

- **Config formats.** Is there one form or several? Many vendors have more than one: a
  hierarchical/brace form and a flat absolute-path form (Junos `set`, SR-OS
  `/configure …`), or single-line vs. block (submode) entry forms (IOS-XR). **If more
  than one exists, you must parse them all** — users supply either, and a realistic
  input *mixes* them (a production dump with an appended proposed edit).
- **Edit verbs are grammar.** Batfish intentionally supports incremental configs:
  paste a production config and append a `delete`/`replace`/`insert`. Those mutation
  verbs are part of the grammar, applied by a preprocessor — not "interactive only."
- **Inheritance / templates.** Anything like Junos `apply-groups`, BGP peer-groups,
  profiles? This must be *resolved before extraction reads values*, and it decides
  whether you need a preprocessing pass at all.
- **Defaults & "absent" semantics.** What does a missing leaf mean — unset, or a
  specific default? **"No policy" is not always "permit all":** SR-OS *rejects*
  received eBGP routes with no import policy. Get these from the schema, not a guess.
- **Mutation & identity quirks.** How is config deleted/cleared/renamed? Any
  UUID/rename-on-edit (Fortinet) or numeric IDs aliasing names?
- **List ordering is semantic.** Apply-group precedence and policy-entry evaluation
  order depend on declared order. Determine the ordering rule from the schema (YANG
  `ordered-by`) or by interacting with the vendor CLI — don't guess; getting it wrong
  silently corrupts routing semantics.
- **Structured show/state output.** You'll need device ground truth to validate
  (Step 10), and the output format decides how much work that is. Older vendors (e.g.
  Cisco IOS) may emit only free text, forcing you to write fragile parsers just to
  compare against Batfish; newer vendors (NX-OS, Arista, Junos, SR-OS MD-CLI) expose
  JSON-structured `show`/state output, which is far more accurate and saves substantial
  work. Don't conclude "text only" from the absence of a Junos-style `| display json`
  on `show` — probe the *state tree* and alternate verbs (e.g. `info json` on SR-OS)
  first.

**Source priority: a machine-readable schema beats prose.** If the vendor ships YANG /
OpenConfig with its image (SR-SIM shipped 558 YANG files), that is your authoritative,
greppable source for defaults, keying, and ordering — far better than gated web manuals.
Keep vendor-proprietary schema in an untracked `reference/` dir — never commit it to
this open-source repo.

**Mind the OS version.** Defaults and syntax can drift between releases, so note the
running version (`show version`) when a source might be version-specific. Two patterns
handle drift: identify the version up front and branch on it (NX-OS records the OS
version and uses it during parsing), or build a single *polyglot* grammar that accepts
both old and new syntax (Arista does this — it avoids version detection for little user
cost). Prefer the polyglot grammar unless the versions genuinely conflict.

The deliverable is a characterization doc thorough enough to design the grammar. See
[`vendors/sros.md` §Configuration format](../parsing/vendors/sros.md#configuration-format)
for what "done" looks like.

### Our running example, AcmeNOS

AcmeNOS is deliberately a *simple, line-oriented* format so the pipeline mechanics stay
in focus:

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

Characterization notes (what we'd write down for real): space-separated; `exit` ends a
block; line-oriented (one statement per line); no flat form, no apply-groups, no edit
verbs (a real vendor with any of these needs the hierarchical treatment cross-linked
above). This simplicity is why AcmeNOS skips the preprocessor that SR-OS and Juniper
require.

---

## Step 2: Register and detect the format

Before a parser can run, Batfish must (a) have a `ConfigurationFormat` for the vendor and
(b) be able to recognize the format from file content. (Full reference:
[parsing README §Adding a new DSL parser](../parsing/README.md#adding-a-new-dsl-parser-to-batfish).)

**Add the format** in `ConfigurationFormat.java`:

```java
ACME("acme"),
```

**Add detection** in `VendorConfigurationFormatDetector.java`. Update `checkRancid` if a
RANCID content type applies, then add a `checkXxx` and reference it from
`identifyConfigurationFormat`:

```java
private static final Pattern ACME_PATTERN =
    Pattern.compile("(?m)^! THIS IS AN ACME NOS FILE$");

private @Nullable ConfigurationFormat checkAcme() {
  if (fileTextMatches(ACME_PATTERN)) {
    return ConfigurationFormat.ACME;
  }
  return null;
}
```

> **Ordering matters.** Detection is heuristic. A brace-delimited vendor's config can
> contain tokens that Juniper/Cisco heuristics would claim (e.g. `policy-options {`).
> Key your detector on **vendor-specific tells** (a banner, a version header) and run it
> *before* the heuristics it would collide with. SR-OS keys on the TiMOS banner and runs
> `checkSros` before `checkJuniper`/`checkCisco` — see
> [`vendors/sros.md` §Format detection](../parsing/vendors/sros.md#format-detection).
> `VendorConfigurationFormatDetectorTest` is where you lock this ordering down with a
> test.

**Wire the parse job.** In `ParseVendorConfigurationJob.java`, add a `case ACME:`. Until
your parser exists, route the format through `UNIMPLEMENTED_FORMATS` so it is detected
deterministically (status UNSUPPORTED, no crash) rather than misclassified; remove it from
that set once Step 4 lands.

Test detection now in `VendorConfigurationFormatDetectorTest` — assert it classifies a
real captured config (and the flat form, and any RANCID alias) as your format before
moving on.

---

## Step 3: Create the directory structure

Use the **vendor-scoped pattern** (all code under `org.batfish.vendor.<vendor>`). See
[parsing README §Vendor code organization](../parsing/README.md#vendor-code-organization).

```bash
mkdir -p projects/batfish/src/main/antlr4/org/batfish/vendor/acme/grammar
mkdir -p projects/batfish/src/main/java/org/batfish/vendor/acme/grammar
mkdir -p projects/batfish/src/main/java/org/batfish/vendor/acme/representation
mkdir -p projects/batfish/src/test/java/org/batfish/vendor/acme/grammar
```

`grammar/` holds the lexer, parser, base lexer, combined parser, and extractor;
`representation/` holds the **vendor-specific** data model (Step 5) and conversion (Step 7).

---

## Step 4: Lexer and parser

Create `AcmeLexer.g4`:

```antlr
lexer grammar AcmeLexer;

options { superClass = 'AcmeBaseLexer'; }

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

// Literals (variable text)
WORD: [a-zA-Z][a-zA-Z0-9._-]*;
NUMBER: [0-9]+;
IPV4: [0-9]+ '.' [0-9]+ '.' [0-9]+ '.' [0-9]+;
IPV4_PREFIX: IPV4 '/' [0-9]+;

// Newlines are significant (statements end in NEWLINE); whitespace and comments are not.
NEWLINE: '\n'+;
WS: [ \t\r]+ -> channel(HIDDEN);
COMMENT: '#' ~[\r\n]* -> channel(HIDDEN);
```

Create `AcmeParser.g4`. Keep rules **LL(1)** (one-token lookahead to choose an
alternative), end statement rules in `NEWLINE` (not parent rules), and give ignored-but-
recognized leaf rules the `_null` suffix so they don't generate spurious warnings. These
conventions are explained in the [parsing README](../parsing/README.md#parser-rules) and
[parser rule conventions](../parsing/parser_rule_conventions.md).

```antlr
parser grammar AcmeParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = AcmeLexer;
}

// Top-level configuration: a sequence of statements.
config
:
  NEWLINE? statement+ EOF
;

// A statement is one of the top-level constructs.
statement
:
  hostname_stmt
  | interface_block
  | bgp_block
;

hostname_stmt
:
  HOSTNAME name = WORD NEWLINE
;

// Interface block: ends with `exit`.
interface_block
:
  INTERFACE name = WORD NEWLINE
  interface_line*
  EXIT NEWLINE
;

interface_line
:
  ip_address_stmt
;

ip_address_stmt
:
  IP ADDRESS addr = IPV4_PREFIX NEWLINE
;

// BGP block: ends with `exit`.
bgp_block
:
  ROUTER BGP asn = NUMBER NEWLINE
  bgp_line*
  EXIT NEWLINE
;

bgp_line
:
  neighbor_stmt
;

neighbor_stmt
:
  NEIGHBOR ip = IPV4 REMOTE_AS remote = NUMBER NEWLINE
;
```

Create the base classes — `AcmeBaseLexer` (extends `BatfishLexer`, tracks
`lastTokenType()` for whole-line-comment predicates) and `AcmeCombinedParser` (extends
`BatfishCombinedParser`, defines error recovery). Follow the
[CoolNOS combined parser](../example_code/new_vendor/src/main/java/org/batfish/vendor/cool_nos/grammar/CoolNosCombinedParser.java).

> **For hierarchical vendors, decide your grammar architecture deliberately — and write
> the mixed-form test first.** SR-OS faced the choice of flatten-to-flat-lines (the
> flatjuniper pipeline) versus one hierarchical grammar that accepts brace, flat, and
> mixed input. It chose a single grammar and normalizes every leaf to a canonical
> absolute path, because mixed-form equivalence is then correct by construction and true
> source line numbers are preserved. Make the three inputs (pure-brace, pure-flat, mixed)
> producing the *identical* model an acceptance test, and let it drive the decision. See
> [`vendors/sros.md` §Canonical form](../parsing/vendors/sros.md#canonical-form-one-hierarchical-grammar-not-a-flatten-pass).

---

## Step 5: Define the vendor-specific representation

This is the most important structural decision in the whole tutorial. Extraction does
**not** populate the VI `Configuration` directly. It populates a *vendor-specific* model
that mirrors how *this vendor* thinks — its keywords, its inheritance, its
physical/logical splits. Conversion (Step 7) translates that into the VI model. Keeping
them separate is what lets vendor quirks live in one place and keeps conversion readable.

Create the representation under `representation/`, rooted at a `VendorConfiguration`:

```java
package org.batfish.vendor.acme.representation;

import org.batfish.vendor.VendorConfiguration;
// ... (Configuration, ConfigurationFormat, etc.)

public final class AcmeConfiguration extends VendorConfiguration {

  // Vendor-shaped state, in the VENDOR's vocabulary — no VI types here.
  private String _hostname;
  private final Map<String, AcmeInterface> _interfaces = new HashMap<>();
  private @Nullable AcmeBgpProcess _bgpProcess;

  // ... vendor-shaped getters/setters ...

  @Override
  public List<Configuration> toVendorIndependentConfigurations() {
    return ImmutableList.of(AcmeConversions.toConfiguration(this));  // Step 7
  }
}
```

```java
public final class AcmeInterface {
  private final String _name;
  private @Nullable ConcreteInterfaceAddress _address;
  // ...
}

public final class AcmeBgpNeighbor {
  private final Ip _peerAddress;
  private @Nullable Long _remoteAs;
  // For real vendors: also the group/template this inherits from. Resolve that
  // inheritance ON THIS MODEL before conversion (see the box below), not in conversion.
}
```

> **Use the vendor's own keywords here.** If the vendor calls admin distance
> `preference`, the field is `preference`; you translate to the VI term only at the
> conversion boundary. **Resolve inheritance on this model before conversion** — a
> `doInherit`/`inheritFrom` pass where a child fills unset attributes from its parent
> (child wins), so conversion reads a fully-populated object. Per-property inline
> inheritance inside conversion is the anti-pattern. Models to study: NX-OS
> `BgpVrfNeighborConfiguration`, SR-OS `BgpNeighbor.inheritFrom`. See
> [`vendors/sros.md` §BGP inheritance](../parsing/vendors/sros.md#bgp-group--neighbor-inheritance-and-peer-type).

---

## Step 6: Write the extractor (parse tree → representation)

The extractor is a `BatfishListener` driven by `AcmeControlPlaneExtractor`
(implements `ControlPlaneExtractor`). It walks the parse tree and populates the
`AcmeConfiguration`. Four things here are **non-optional production requirements**; name
them explicitly in your definition of done, because each is easy to skip and silently
absent when skipped:

**1. Parse values safely.** Raw `Integer.parseInt`/`Ip.parse`/`Prefix.parse` throw on bad
input. Use the shared range-checked idioms `toIntegerInSpace`/`toLongInSpace` (search
existing vendors — flatjuniper, cisco_xr — for the pattern; don't reinvent it),
`Ip.tryParse`/`Prefix.tryParse`, and a static map + warn for enumerated leaves. A bad
value becomes a **line-stamped `ParseWarning`**, not a crash or a silent drop.

```java
@Override
public void exitNeighbor_stmt(Neighbor_stmtContext ctx) {
  Optional<Ip> peer = toIp(ctx, ctx.ip);          // tryParse + warn(ctx, ...) on failure
  Optional<Long> asn = toLong(ctx, ctx.remote, BGP_ASN_SPACE);  // range-checked
  if (peer.isEmpty() || asn.isEmpty()) {
    return;  // already warned, line-stamped
  }
  _currentBgp.getNeighbors()
      .computeIfAbsent(peer.get(), AcmeBgpNeighbor::new)
      .setRemoteAs(asn.get());
}
```

**2. Emit line-stamped warnings via `warn(ctx, …)`.** Context-free red-flags are invisible
to the [`annotate`](../../projects/batfish/src/main/java/org/batfish/main/annotate/Annotate.java)
tool, which inlines warnings above their source line and reads `ParseWarning`s only. Also
add your `ConfigurationFormat` to `Annotate.getCommentHeader` with the right comment char
(`#` for AcmeNOS), or annotate output for your vendor won't render.

**3. Track named structures.** Every referenceable object (interface, BGP group,
route-map, prefix-list, ACL) must be recorded with `defineStructure`,
`referenceStructure`, and finalized with `markConcreteStructure`. This powers the
`definedStructures`, `undefinedReferences`, and unused-structure questions — which are
**silently empty** for a vendor that skips structure tracking. See
[Implementation Guide Pattern 4](../parsing/implementation_guide.md#pattern-4-structure-definition-and-reference-tracking).

**4. Carry provenance if you extract from a derived tree.** If your extractor walks a
tree *you built* (not the raw ANTLR parse tree — common for hierarchical vendors that
preprocess), the `ParserRuleContext` is gone by extraction time, breaking **both**
line-stamped warnings **and** structure tracking (they ride the same provenance). The
*stronger* reason is warnings: there is a value warning at *every leaf*, vastly more sites
than the handful of structure references. So store the creating `ParserRuleContext` on
each tree node (as a list — a node can be set by multiple statements), and make your
inheritance-copy carry it so an inherited value points back at the group's definition
line. AcmeNOS walks the raw parse tree so this is free; SR-OS does not — see
[`vendors/sros.md` §Source provenance](../parsing/vendors/sros.md#source-provenance-line-stamped-warnings--structure-references).

> **Fix classes of bugs, not instances.** When you find one silent `default: return null`
> or a missing warning, sweep the whole extractor for the same shape. "Is this a one-off
> patch or a reusable pattern for all places like this?" is the right question to ask of
> every fix.

---

## Step 7: Convert (representation → vendor-independent model)

`AcmeConfiguration.toVendorIndependentConfigurations()` calls into `AcmeConversions`,
which builds the VI `Configuration`. This is where you translate vendor vocabulary to VI
concepts, normalize defaults, and resolve cross-references.

```java
static Configuration toConfiguration(AcmeConfiguration vc) {
  Configuration c = new Configuration(vc.getHostname(), ConfigurationFormat.ACME);
  c.setDeviceModel(DeviceModel.ACME_UNSPECIFIED);
  // VRFs, then interfaces, then BGP, then routing policy ...
  return c;
}
```

Three rules govern conversion:

- **Convert only what the VI model genuinely supports; extract everything.** Populating
  the vendor representation (and writing extraction tests for it) is *always* fine, even
  for features the VI model can't express. But do **not** fabricate VI support that
  doesn't exist (e.g. there is no VPNv4 address family with inter-node leaking). When the
  VI model can't represent a construct, leave it out of conversion and **sickbay** the
  device comparison to a tracked issue (Step 10) — keep the lab, track the gap honestly.
- **Significant-but-unmodeled constructs warn, never silently drop.** If conversion
  ignores something that could change behavior, emit a `redFlag`. SR-OS leaves the
  hardware tree unconverted but warns once, rather than dropping it silently.
- **Encode vendor defaults explicitly.** This is where "no policy ≠ permit all" gets
  realized. SR-OS's eBGP default-reject is implemented with the Junos-style
  generated-peer-policy idiom (a per-peer import/export policy that defaults to
  reject-all for eBGP, accept-all for iBGP, then chains the named policies). See
  [`vendors/sros.md` §eBGP default-reject](../parsing/vendors/sros.md#ebgp-default-reject-the-policy-chain)
  — this is the most intricate part of the SR-OS conversion and a good model for any
  vendor with non-trivial routing-policy defaults.

---

## Step 8: Post-processing awareness

Post-processing ([README](../post_processing/README.md)) is largely vendor-independent —
interface dependency resolution, derived bandwidth/IGP costs, Layer-1 topology
application, protocol-state init. Your job is to make sure your *converted* model flows
through it correctly. The one trap worth calling out:

> **Model the physical/logical interface split if your vendor has one.** SR-OS separates
> the physical **port** (`1/1/c1/1`) from the L3 **router-interface** (`to-r2`) — the
> Junos physical/unit split. The fix is to convert a port-bound interface into **two** VI
> interfaces: an addressless `PHYSICAL` named by the port path, and a `LOGICAL` holding
> the address with a `BIND` dependency on the port. Then a user-provided Layer-1 topology
> (which names the *port*) drives the L3 adjacency. **Tell when you've got this wrong:**
> the adjacency forms only via the same-subnet fallback — check that the L1 edge survived
> into `activeLogicalL1`, not just that the session came up. See
> [`vendors/sros.md` §Interfaces](../parsing/vendors/sros.md#interfaces-the-port--router-interface-split).

---

## Step 9: Test at every stage

Tests live under `src/test/java/org/batfish/vendor/acme/grammar/`. A typical grammar test
parses a focused config and asserts on the result:

```java
@Test
public void testInterfaceExtraction() {
  AcmeConfiguration c = parseVendorConfig("basic-interface");
  assertThat(c.getInterfaces().keySet(), contains("eth0"));
  assertThat(
      c.getInterfaces().get("eth0").getAddress(),
      equalTo(ConcreteInterfaceAddress.parse("10.0.0.1/24")));
}
```

Layer your tests by stage:

1. **Parse**: the captured config parses with **zero unrecognized lines / zero FATAL
   warnings**. For a hierarchical vendor, assert pure-brace, pure-flat, and **mixed** input
   yield the identical model.
2. **Extract**: every characterized feature populates the representation; intentionally
   skipped subtrees are skipped *silently*; no unexpected `todo`/`warn`. Cover the
   line-stamped value warnings and structure definitions/undefined-references explicitly.
3. **Convert**: assert on the VI model — interface types/addresses, route-filter lists,
   resolved inheritance, and **behavioral** defaults (e.g. the generated export policy
   accepts the right prefixes and rejects the rest). Assert the unmodeled-construct warning.
4. **Annotate**: an `AnnotateTest` case proving warnings render inline.

> **Passing alone is not enough.** Green tests with shallow assertions hide gaps. Use
> red/green TDD — write a specific failing assertion first, then make it pass — so each
> test pins down real behavior rather than just exercising code. Give test fixtures the
> identity (e.g. a real `hostname`) the device would produce, and test with real
> device-rendered configs, not idealized hand-written ones.

Build and run:

```bash
bazel test //projects/batfish/src/test/java/org/batfish/vendor/acme/...
./tools/run_checkstyle.sh && bazel run //:buildifier.check
# Run the local service with the repo's wrapper — not raw bazel run:
./tools/bazel_run.sh
```

Then sanity-check end to end against a running service with Pybatfish:

```python
from pybatfish.client.session import Session

bf = Session(host="localhost")
bf.init_snapshot("test_configs/", name="acme-test")
print(bf.q.nodeProperties().answer().frame())   # your node should appear
```

---

## Step 10: Validate against a real device

Unit tests prove the model is internally consistent. **Validation proves it matches the
device.** This is where modeling errors actually surface — three real SR-OS conversion
bugs (a wrong BGP local-IP, a wrong route origin, a spurious host route) were caught here,
not in unit tests. Principles, learned the hard way:

- **Validate incrementally, per stage — don't defer all validation to the end.** As soon
  as extraction works you can validate hostname and config-format; as soon as conversion
  works, interfaces. You do not need the full data plane for those.
- **The cross-vendor peer is an oracle from day one.** In a mixed lab, the *other*
  router's vendor is already fully modeled, so its view of the session (does BGP come up?
  which routes does it learn?) independently checks your new vendor's conversion before
  your vendor's own RIB is trustworthy.
- **Never mask a mismatch in the validator.** A validator-side filter that hides a real
  Batfish/device difference is a bug, not a fix. When Batfish is wrong, fix Batfish; when
  the VI model genuinely can't represent something, **sickbay the check to a GitHub issue**.
  Audit every reconciliation line: legitimate normalization vs. hack.
- **A negative finding about device data is weak until you've checked every state path.**
  "The communities aren't in the show output" was wrong — they were in the BGP rib's
  attr-sets, not the local-rib. Enumerate where each attribute lives before concluding it's
  uncollectable.
- **Separate lab *building* from lab *modeling*, and grow both in a ladder.** Collect
  ground truth and add functionality one rung at a time, in order of increasing
  complexity (interfaces → static → OSPF → eBGP → …), validating each rung before the
  next. Collecting on real hardware (EC2/containerlab) is flaky, costly, and
  license/SSH-bound, while modeling and validation are local and deterministic — so batch
  one gated collection sprint per rung (hard instance auto-terminate, teardown on any
  exit), then model locally. On a collection failure, sickbay that rung and continue;
  never abandon teardown.

See [`vendors/sros.md` §RIB validation](../parsing/vendors/sros.md#rib-validation) for
the SR-OS modeling facts that made the device comparison correct (e.g. learned-eBGP admin
distance is 170 on SR-OS, not 20; the device's local-rib over-lists connected routes).

---

## Before you ship: production-readiness checklist

The walkthrough above teaches these; this is the recap to grade yourself against, ordered
roughly by how much grief skipping each one causes.

1. **Validate against ground truth.** Incrementally, with the cross-vendor oracle, no
   masking, gaps sickbayed to issues. This is what catches real modeling errors. (Step 10.)
2. **Characterize first.** Formats, defaults, inheritance, mutation, ordering — from the
   authoritative schema, not guesses. (Step 1.)
3. **Rep/convert split.** Extraction targets a vendor-specific `VendorConfiguration`;
   conversion targets the VI `Configuration`. They are separate stages. (Steps 5–7.)
4. **Parse values safely.** `tryParse`/`toIntegerInSpace`, never raw `parse`. Bad value →
   line-stamped `ParseWarning`. (Step 6.)
5. **Line-stamped warnings + `annotate`.** Emit via `warn(ctx, …)`; add the format to
   `Annotate.getCommentHeader`. (Step 6.)
6. **Track named structures.** `defineStructure`/`referenceStructure`/
   `markConcreteStructure` — or the structure questions are silently empty. (Step 6.)
7. **Carry provenance on a derived tree.** Or you lose warnings *and* structure tracking.
   (Step 6.)
8. **Resolve inheritance on the model, before conversion.** `doInherit`/`inheritFrom`, not
   inline in conversion. (Step 5.)
9. **Convert conservatively; warn, don't drop.** Extract everything, convert only what VI
   supports, sickbay the rest. Significant-but-unmodeled constructs warn. (Step 7.)

A worked example of every item on a real hierarchical vendor:
[`vendors/sros.md`](../parsing/vendors/sros.md).

---

## Common issues

| Symptom | Likely cause / fix |
| ------- | ------------------ |
| Grammar won't compile | Undefined lexer token; left recursion; non-LL(1) alternative. See [parser conventions](../parsing/parser_rule_conventions.md). |
| Parse tree empty | Start rule doesn't reach `EOF`: `config: statement+ EOF;`. |
| Extractor never runs | You didn't walk the tree, or the format detector returns a different vendor (Step 2 ordering). |
| `definedStructures`/`undefinedReferences` empty | Structure tracking skipped (Step 6 item 3). |
| `annotate` shows nothing | Warnings aren't `ParseWarning`s, or the format is missing from `getCommentHeader` (Step 6 item 2). |
| Tests pass but real configs fail | Fixtures are idealized, not device-rendered (Step 9). Test with a real captured config. |
| Adjacency forms but L1 edge missing | Physical/logical split not modeled; same-subnet fallback masking it. Check `activeLogicalL1` (Step 8). |

For systematic debugging, see [Debugging Parser Issues](debugging_parser_issues.md).

---

## Quick reference

### File structure (vendor-scoped)

```
projects/batfish/src/main/
├── antlr4/org/batfish/vendor/acme/grammar/
│   ├── AcmeLexer.g4
│   └── AcmeParser.g4
└── java/org/batfish/vendor/acme/
    ├── grammar/
    │   ├── AcmeBaseLexer.java
    │   ├── AcmeCombinedParser.java
    │   ├── AcmeControlPlaneExtractor.java
    │   └── AcmeConfigurationBuilder.java   # the BatfishListener
    └── representation/
        ├── AcmeConfiguration.java          # vendor model + toVendorIndependent...()
        ├── AcmeInterface.java, AcmeBgp*.java, ...
        └── AcmeConversions.java            # rep → VI
projects/batfish/src/test/java/org/batfish/vendor/acme/grammar/
    └── AcmeGrammarTest.java
```

Plus, outside the vendor package: `ConfigurationFormat.ACME`, a `checkAcme` in
`VendorConfigurationFormatDetector`, a `case ACME` in `ParseVendorConfigurationJob`, and a
`DeviceModel.ACME_UNSPECIFIED`.

### The stages, one line each

- **Characterize** → a cited doc of formats/defaults/inheritance/ordering.
- **Detect** → `ConfigurationFormat` + a content-keyed detector, ordered before colliders.
- **Parse** → ANTLR lexer + grammar → parse tree (LL(1), `_null` for ignored leaves).
- **Extract** → a *vendor* `VendorConfiguration`, with safe parsing, line-stamped
  warnings, structure tracking, provenance.
- **Convert** → the *vendor-independent* `Configuration`; warn don't drop; defaults explicit.
- **Post-process** → flows through the shared pipeline; model physical/logical splits.
- **Validate** → against a real device, incrementally, no masking, gaps sickbayed.

### Commands

```bash
bazel build //projects/batfish/src/main/java/org/batfish/vendor/acme/...
bazel test  //projects/batfish/src/test/java/org/batfish/vendor/acme/...
./tools/bazel_run.sh            # local service (preferred over raw bazel run)
```

---

## Related documentation

- [Parsing README](../parsing/README.md) · [Implementation Guide](../parsing/implementation_guide.md) · [Parser Conventions](../parsing/parser_rule_conventions.md) · [ANTLR4 Tips](../parsing/antlr4_tips.md)
- [Extraction](../extraction/README.md) · [Conversion](../conversion/README.md) · [Post-processing](../post_processing/README.md)
- **Worked example:** [Nokia SR-OS](../parsing/vendors/sros.md) — the first real
  hierarchical vendor added against this guide.
- Other vendor guides: [Juniper](../parsing/vendors/juniper.md) · [Cisco IOS-XR](../parsing/vendors/ios_xr.md)
- [Tutorial: Debugging Parser Issues](debugging_parser_issues.md)
