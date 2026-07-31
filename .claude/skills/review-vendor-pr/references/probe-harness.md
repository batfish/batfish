# Differential probe harness

How to parse a hand-built config on demand and compare the PR branch against its
merge base.

Examples below are Cisco ASA (`grammar/cisco_asa`, `AsaCombinedParser`,
`AsaControlPlaneExtractor`, `CiscoAsaGrammarTest`). Substitute your vendor's
equivalents — resolve them with the `find`/`bazel query` commands in SKILL.md
step 0, since directory layout and class roles both vary:

| ASA example | Varies by vendor |
|---|---|
| `grammar/cisco_asa/…` | newer vendors: `vendor/<v>/grammar/…` |
| `AsaCombinedParser` | `<Vendor>CombinedParser` |
| `AsaControlPlaneExtractor` | may delegate to a `<Vendor>ConfigurationBuilder`; Junos's is `flatjuniper/ConfigurationBuilder` |
| `AsaConfiguration` | `<Vendor>Configuration` under `representation/<v>/` or `vendor/<v>/representation/` |
| `CiscoAsaGrammarTest` | usually `<Vendor>GrammarTest`; some differ (`XrGrammarTest`, `IpTablesGrammarTest`) |

**Do not hand-write the parser/extractor construction.** Copy it from the target
test class's own `parseVendorConfig`/`parseConfig` helper — constructor arity and
argument order differ across vendors, and flattened vendors (Junos, Palo Alto)
run a preprocessing/flattening step first that you must not skip.

## Why not a standalone test file

Writing a new test class means recreating the parser wiring, the settings, the
`TESTCONFIGS_PREFIX` resource path, and the BUILD deps — and BUILD files glob
`*.java`, so a new file must compile against a dep set you did not choose. Adding
a temporary method to the vendor's **existing** grammar test class inherits all of
it and needs no BUILD change.

## Recipe

**1. Scratch testconfig** in the vendor's testconfigs dir — either
`…/resources/org/batfish/grammar/<vendor>/testconfigs/` or
`…/resources/org/batfish/vendor/<vendor>/grammar/testconfigs/`.

Include whatever header the vendor's format detection needs, or the file will be
parsed as the wrong vendor (or skipped). Copy the first lines of an existing
testconfig in that same directory rather than guessing — e.g. ASA needs the
`! This is an ASA device.` comment plus an `ASA Version` line; Junos configs need
their brace/`set`-line shape; PAN-OS needs its XML-ish nesting. Then add a
hostname and any interfaces or objects the probed commands reference.

Name it `zz*` so it sorts last and is obvious as a leftover.

**2. Probe methods** in the vendor's grammar test class. Two levels — a bug can be
invisible at one and obvious at the other:

The vendor-model probe below is the ASA shape. **Start from the target class's own
vendor-level helper** and add only the `println`s. That helper's name varies —
`parseVendorConfig` (ASA, NX-OS, SR-OS), `parsePaloAltoConfig` /
`parseNestedConfig` (Palo Alto), `parseJuniperConfig` (FlatJuniper) — so grep for
the vendor `Configuration` type as the return value to find it:

```bash
grep -nE "private .*<Vendor>Configuration [a-zA-Z]+\(" <path-to-test>
```

```java
  @Test
  public void zzProbe() {                    // vendor model
    String src = readResource(TESTCONFIGS_PREFIX + "zzprobe", UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    AsaCombinedParser parser = new AsaCombinedParser(src, settings);
    Warnings w = new Warnings(true, true, true);   // pedantic, redFlag, unimplemented
    AsaControlPlaneExtractor extractor =
        new AsaControlPlaneExtractor(src, parser, w, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    AsaConfiguration c = (AsaConfiguration) extractor.getVendorConfiguration();
    System.out.println("ZZPROBE parseWarnings=" + w.getParseWarnings());
    System.out.println("ZZPROBE redFlags=" + w.getRedFlagWarnings());
    System.out.println("ZZPROBE field=" + c.getSomeNewField());
  }
```

The VI-level probe uses `parseConfig`, present in most (not all) grammar tests —
absent from the Cumulus-family, FRR, Sonic, SR-OS, and iptables ones, which parse
via a different path. Where it exists the body is portable, since `Configuration`
getters are vendor-independent:

```java
  @Test
  public void zzProbeVi() throws IOException {   // vendor-independent model
    Configuration c = parseConfig("zzprobe");
    System.out.println("ZZPROBEVI domainName=" + c.getDomainName());
    System.out.println("ZZPROBEVI iface=" + c.getAllInterfaces().keySet());
  }
```

**3. Run**, filtering to the marker:

```bash
bazel test --test_output=all \
  --test_filter='CiscoAsaGrammarTest#zzProbe$' \
  -- //projects/batfish/src/test/java/org/batfish/grammar/cisco_asa:tests 2>&1 \
  | grep "ZZPROBE \|error:"
```

Resolve the target with `bazel query "tests(//projects/batfish/...)" | grep <vendor>`
— new-layout vendors are under `…/org/batfish/vendor/<vendor>/grammar:tests`.

`#zzProbe$` anchors the filter so it does not also match `zzProbeVi`.

Trust Bazel's caching. Editing the test method or the scratch testconfig changes a
declared input, so the test re-runs; and a cached result still replays its log
under `--test_output=all`, so the markers print either way. If you see stale-looking
output, the cause is almost always a compile failure (below), not the cache.

## Reading the output

**Grep both the marker and `error:`.** If the method fails to compile, Bazel
prints the javac error and *no* marker lines. Grepping only the marker makes a
compile failure look like empty output — a trap that costs several cycles. The
symptom is `grep` matching only the literal `System.out.println` line echoed in
the javac error, so a match containing `System.out.println` means it did not run.

**A parse error throws.** `Batfish.parse` raises
`ParserBatfishException: Parser error` before any marker prints. The useful detail
is in the stack trace, so re-run without the marker filter:

```bash
... 2>&1 | grep -E "no viable|mismatched|extraneous|Offending|>>>"
```

`>>>` marks the offending source line in the error context block. This is the
expected result when probing a removed rule — record the message as the finding.

For the VI-level probe, a parse error surfaces as
`BatfishException: Exiting due to parser errors`, since `parseConfig` builds a
snapshot. That wrapper makes VI probes a poor way to *characterize* a parse
error; use the vendor-level probe for that, and the VI probe to show what a
successfully-parsed config yields.

## Baseline comparison

The finding is the *difference*. Preserve the probe files across the branch
switch — a `git checkout` of a tracked test file will revert your edit:

```bash
cp .../CiscoAsaGrammarTest.java working/probe-test.java
cp .../testconfigs/zzprobe working/zzprobe            # untracked; survives checkout
git stash push -- .../CiscoAsaGrammarTest.java
git checkout $(git merge-base HEAD origin/master)     # or: origin/master
# re-apply the probe method (the class differs between branches, so re-insert
# rather than copying the whole file), re-run, record output
git checkout -- .../CiscoAsaGrammarTest.java
git checkout <pr-branch> && git stash pop
```

Insert the probe method with a small Python replace against a stable anchor
(an existing `@Test` method signature) rather than by line number — line numbers
differ between branches:

```python
s = s.replace('''
  @Test
  public void testHumanName() throws IOException {''', probe + '''
  @Test
  public void testHumanName() throws IOException {''', 1)
```

Use a different marker on the baseline run (`ZZBASE` vs `ZZPROBE`) so the two
outputs cannot be confused in scrollback.

Note that the baseline may not have the getters the PR adds — drop those prints
for the baseline probe and compare only fields that exist on both, or compare at
the VI level where the field names are stable.

## Cleanup

```bash
git checkout -- projects/batfish/src/test/java/org/batfish/grammar/<vendor>/<Vendor>GrammarTest.java
rm -f projects/batfish/src/test/resources/org/batfish/grammar/<vendor>/testconfigs/zzprobe
rm -f working/probe-test.java working/zzprobe
git status --short    # expect only pre-existing untracked files
```

Scratch files go in `working/` (gitignored) or the testconfigs dir. Never leave a
`zzprobe` testconfig behind — the vendor's ref tests may pick it up.
