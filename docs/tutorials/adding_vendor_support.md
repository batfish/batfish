# Adding Your First Vendor Support

**Time**: a weekend to a first parse; weeks to a validated vendor | **Difficulty**: Intermediate

This tutorial teaches the end-to-end workflow for adding a new network vendor to
Batfish. The running example is a small hypothetical vendor, **AcmeNOS**, kept
deliberately simple so the *shape* of each stage is visible. At every stage we
cross-link [`vendors/sros.md`](../parsing/vendors/sros.md) — the worked record of
adding Nokia SR-OS, the first real hierarchical vendor added against this guide —
so you can see the same idea on a production-grade config.

> **Read this first.** Earlier versions of this tutorial stopped at "parse a clean
> config into the VI model" and skipped the steps that actually consume most of the
> effort: characterizing the OS, extracting into a *vendor* model before converting,
> tracking provenance and references, and validating against a real device. Those
> omissions were exactly the corrections made while adding SR-OS. This rewrite folds
> them into the walkthrough. If you only take one thing away: **extraction targets a
> vendor-specific representation, conversion targets the vendor-independent model, and
> they are separate stages.**

## The pipeline

Adding a vendor is not "write a grammar." It is walking a config through eight
stages, each with its own doc:

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

You do not finish all of one stage before the next. The realistic loop is: get the
simplest config through *all* stages, validate it, then climb a complexity ladder
(interfaces → static → OSPF → eBGP → iBGP → route reflection → policy → misconfig),
re-running the pipeline for each newly-exercised feature. SR-OS was built this way; see
[`vendors/sros.md` §Complexity ladder](../parsing/vendors/sros.md#complexity-ladder-p8).

---

## Prerequisites

1. **ANTLR4 basics**: lexer/parser concepts. New to it? Read the
   [example code](../example_code/new_vendor/) (the "CoolNOS" scaffold) and
   [ANTLR4 tips](../parsing/antlr4_tips.md) first.
2. **Java 17**, and comfort reading existing vendor code.
3. **Read the canonical docs, don't work from memory.** Read [Parsing](../parsing/README.md),
   the [Implementation Guide](../parsing/implementation_guide.md), and
   [Extraction](../extraction/README.md) before writing code. (Writing the plan from a
   half-remembered version of these docs was an early, repeated mistake on SR-OS.)
4. **Pick your reference vendor by config *shape*** — see the box below.

> ### Which existing vendor should I copy?
> "NX-OS is the reference-quality implementation" is true for *code quality*, but
> NX-OS is **line-oriented**: one structure is one `ParserRuleContext` on one line.
> If your vendor's config is **hierarchical / brace-delimited** (Junos, Palo Alto,
> SR-OS), a single logical statement spans *multiple* source lines, and the NX-OS
> one-line-per-structure machinery does not fit. In that case study **Juniper**
> for the structural model and **NX-OS** for the inheritance-resolution mechanism.
> Match the reference to your shape; AcmeNOS below is line-oriented, so it follows NX-OS.

---

## Step 1: Characterize the OS *before* writing a grammar

This is the step the old tutorial skipped, and the one that prevents the most rework.
Booting an emulator and collecting one config proves your *toolchain* works — it does
**not** characterize the OS. Before any grammar, answer (and write down, with citations):

- **Config formats.** Is there one form or several? Many vendors have both a
  hierarchical/brace form and a flat absolute-path form (Junos `set`, SR-OS
  `/configure …`). **If both exist, you must parse both** — users supply either, and a
  realistic input *mixes* them (a production dump with an appended proposed edit).
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
  order depend on declared order. Read the schema's ordering rule (YANG `ordered-by`),
  don't guess — getting it wrong silently corrupts routing semantics.
- **Structured show/state output.** You'll need device ground truth later (Step 8). Do
  **not** conclude "text only" from the absence of a Junos-style `| display json` on
  `show`. Probe the *state tree* and alternate verbs first.

**Source priority: a machine-readable schema beats prose.** If the vendor ships YANG /
OpenConfig with its image (SR-SIM shipped 558 YANG files), that is your authoritative,
greppable source for defaults, keying, and ordering — far better than gated web manuals.
**Pin every source to the exact running OS version** (`show version`); CLI grammar drifts
between releases. Keep vendor-proprietary schema in an untracked `reference/` dir — never
commit it to this open-source repo.

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
block; line-oriented (one statement per line — so NX-OS is the right reference); no flat
form, no apply-groups, no edit verbs (a real vendor with any of these needs the
hierarchical treatment cross-linked above). This simplicity is why AcmeNOS skips the
preprocessor that SR-OS and Juniper require.

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

**Wire the parse job.** In `ParseVendorConfigurationJob.java`, add a `case ACME:`. Until
your parser exists, route the format through `UNIMPLEMENTED_FORMATS` so it is detected
deterministically (status UNSUPPORTED, no crash) rather than misclassified; remove it from
that set once Step 4 lands.

Test detection now — classify a real captured config (and the flat form, and any RANCID
alias) before moving on.

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
`representation/` holds the **vendor-specific** data model (Step 5) and conversion (Step 6).

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

// Literals
WORD: [a-zA-Z][a-zA-Z0-9._-]*;
NUMBER: [0-9]+;
IPV4: [0-9]+ '.' [0-9]+ '.' [0-9]+ '.' [0-9]+;
IPV4_PREFIX: IPV4 '/' [0-9]+;

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

config: NEWLINE? statement+ EOF;

statement: hostname_stmt | interface_block | bgp_block;

hostname_stmt: HOSTNAME name = WORD NEWLINE;

interface_block:
  INTERFACE name = WORD NEWLINE
  interface_line*
  EXIT NEWLINE
;
interface_line: ip_address_stmt;
ip_address_stmt: IP ADDRESS addr = IPV4_PREFIX NEWLINE;

bgp_block:
  ROUTER BGP asn = NUMBER NEWLINE
  bgp_line*
  EXIT NEWLINE
;
bgp_line: neighbor_stmt;
neighbor_stmt: NEIGHBOR ip = IPV4 REMOTE_AS remote = NUMBER NEWLINE;
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
> source line numbers are preserved. Don't adopt the reference vendor's IR shape
> reflexively; make the three inputs (pure-brace, pure-flat, mixed) produce the *identical*
> model an acceptance test, and let it drive the decision. See
> [`vendors/sros.md` §Canonical form](../parsing/vendors/sros.md#canonical-form-one-hierarchical-grammar-not-a-flatten-pass).

---

## Step 5: Define the vendor-specific representation

**This is the stage the old tutorial skipped, and the most important correction.**
Extraction does **not** populate the VI `Configuration` directly. It populates a
*vendor-specific* model that mirrors how *this vendor* thinks — its keywords, its
inheritance, its physical/logical splits. Conversion (Step 6) translates that into the
VI model. Keeping them separate is what lets vendor quirks live in one place and keeps
conversion readable.

Create the representation under `representation/`, rooted at a `VendorConfiguration`:

```java
package org.batfish.vendor.acme.representation;

import org.batfish.vendor.VendorConfiguration;
// ... (Configuration, ConfigurationFormat, etc.)

public final class AcmeConfiguration extends VendorConfiguration {
  private String _hostname;
  private final Map<String, AcmeInterface> _interfaces = new HashMap<>();
  private @Nullable AcmeBgpProcess _bgpProcess;

  // vendor-shaped getters/setters; NO VI types here.

  @Override
  public List<Configuration> toVendorIndependentConfigurations() {
    return ImmutableList.of(AcmeConversions.toConfiguration(this));  // Step 6
  }
}
```

```java
// Vendor-shaped, using the VENDOR's vocabulary — not VI types.
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
`AcmeConfiguration`. Four things here are **non-optional production requirements** — they
were silently missing on the first SR-OS pass *because nothing named them*, so name them
in your own Done-when:

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
**silently empty** for a vendor that skips it. (This was a real gap caught in SR-OS review:
"I don't see how we're doing line tracking and undefined references." It fell through
because the checklist never named it.) See
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
  device comparison to a tracked issue (Step 8) — keep the lab, track the gap honestly.
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

> **"Passing" is not "excellent."** Green tests with shallow assertions hide gaps. Judge
> coverage by re-reading the tests against the feature, and give test fixtures the
> identity (e.g. a real `hostname`/`system name`) the device would produce. **Fixtures
> must mirror device-rendered output** — a real SR-OS bug (`{ }` inline empty block) was
> masked by a hand-written multi-line `{ \n }` fixture and only a device-collected config
> exposed it.

Build and run:

```bash
bazel test //projects/batfish/src/test/java/org/batfish/vendor/acme/...
./tools/run_checkstyle.sh && bazel run //:buildifier.check
# Run the local service with the repo's wrapper — not raw bazel run:
./tools/bazel_run.sh
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
- **Separate lab *building* from lab *modeling*.** Collecting ground truth on real
  hardware (EC2/containerlab) is flaky, costly, and license/SSH-bound; modeling and
  validation are local and deterministic. Batch one gated collection sprint per ladder
  rung, with a hard instance auto-terminate and teardown-on-any-exit, then model locally.
  On a collection failure, sickbay that rung and continue — never abandon teardown.

See [`vendors/sros.md` §RIB validation](../parsing/vendors/sros.md#rib-validation-p7) for
the SR-OS modeling facts that made the device comparison correct (e.g. learned-eBGP admin
distance is 170 on SR-OS, not 20; the device's local-rib over-lists connected routes).

---

## Before you ship: production-readiness checklist

The walkthrough above teaches these; this is the recap to grade yourself against. Each was
a real correction while adding SR-OS.

1. **Rep/convert split.** Extraction targets a vendor-specific `VendorConfiguration`;
   conversion targets the VI `Configuration`. They are separate stages. (Steps 5–7.)
2. **Characterize first.** Formats, defaults, inheritance, mutation, ordering — from the
   authoritative schema, not guesses; pinned to the running OS version. (Step 1.)
3. **Parse values safely.** `tryParse`/`toIntegerInSpace`, never raw `parse`. Bad value →
   line-stamped `ParseWarning`. (Step 6.)
4. **Line-stamped warnings + `annotate`.** Emit via `warn(ctx, …)`; add the format to
   `Annotate.getCommentHeader`. (Step 6.)
5. **Track named structures.** `defineStructure`/`referenceStructure`/
   `markConcreteStructure` — or the structure questions are silently empty. (Step 6.)
6. **Carry provenance on a derived tree.** Or you lose warnings *and* structure tracking.
   (Step 6.)
7. **Resolve inheritance on the model, before conversion.** `doInherit`/`inheritFrom`, not
   inline in conversion. (Step 5.)
8. **Convert conservatively; warn, don't drop.** Extract everything, convert only what VI
   supports, sickbay the rest. Significant-but-unmodeled constructs warn. (Step 7.)
9. **Validate against ground truth.** Incrementally, with the cross-vendor oracle, no
   masking, gaps sickbayed to issues. (Step 10.)

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
