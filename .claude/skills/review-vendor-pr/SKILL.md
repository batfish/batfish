---
name: review-vendor-pr
description: Review a Batfish PR that touches vendor config handling - ANTLR grammar (.g4), extraction (*ControlPlaneExtractor), vendor representation classes, conversion to the VI model, or structure/reference tracking. Use when asked to review such a PR or branch, when a diff touches projects/batfish/src/main/antlr4/** or representation/**, or when checking a parsing/extraction change against a vendor CLI manual. Produces findings ranked by whether they reach the vendor-independent model, separating real defects from device-faithful behavior and from newly-surfaced parse warnings.
---

# Reviewing vendor parsing/extraction PRs

Vendor PRs fail in ways that reading the diff cannot reveal. A grammar rule that
looks correct can silently swallow the next command; a removed rule can regress
configs no test covers; extracted state can be written and never read. The only
reliable method is **differential probing**: parse hand-built configs on the PR
branch and on the merge base, and diff the results.

Two failure modes dominate reviews of this kind, and both produce confident
findings that are wrong:

- **Calling device-faithful behavior a bug.** You notice a submode rule
  consuming a command you think is global, and report data corruption — but the
  real device, in that submode, does exactly the same thing.
- **Scoring parse-warning changes as blockers.** A line that used to be silently
  ignored now warns. Batfish handles parse warnings well; the snapshot still
  converts. This is low severity at most.

The severity rubric below exists to catch both before you report.

## 1. Establish scope and read the docs first

Get the diff and the merge base:

```bash
gh pr view <N> --json title,body,files,commits
gh pr diff <N> > working/pr<N>.diff
gh pr checkout <N>
git merge-base HEAD origin/master   # the baseline for all differential probes
```

Read the docs for what the diff touches — these encode conventions reviewers are
expected to enforce, and citing them makes a finding actionable:

| Diff touches | Read |
|---|---|
| any `.g4` | `docs/parsing/parser_rule_conventions.md` (LL(1), single-token advancement, `<parent_prefix><ext>_next_token` naming, prefix collisions, inlining) |
| a new/ignored command | `docs/parsing/implementation_guide.md` (the extract vs `_null` decision tree; §"When to Use the `_null` Suffix") |
| lexer tokens, modes | `docs/parsing/lexer_mode_patterns.md`, `docs/parsing/antlr4_tips.md` |
| extractor | `docs/extraction/README.md` — esp. range validation via `toIntegerInSpace`/`toLongInSpace`, and `warn(ctx, …)` vs `redFlagf` (only `ParseWarning`s are line-stamped, so only they work with `annotate`) |
| `defineStructure`/`referenceStructure`/`StructureUsage` | `docs/parsing/implementation_guide.md` §"Pattern 4: Structure Definition and Reference Tracking" |
| conversion to VI | `docs/conversion/README.md`; inheritance belongs in a `doInherit` pass on the vendor model, not inline in conversion |
| tests | `docs/development/testing_guide.md`; ref tests under `tests/` |
| a vendor-specific grammar | `docs/parsing/vendors/<vendor>.md` if present |

## 2. Get the vendor CLI manual

Never rule on syntax from memory. Fetch the manual for the platform and version
the PR targets, and quote it in findings.

If the user supplied manual URLs, use those. Otherwise search for the vendor's
config guide and command reference for the relevant release. Cisco ASA 9.24, for
example, splits into three books: general (hostname/DNS/AAA/interfaces), firewall
(ACL/NAT/objects), and VPN (tunnel-group/group-policy/webvpn).

`WebFetch` works for public vendor docs. Ask it narrow questions — exact syntax
line, valid value range, **and which CLI mode the command belongs to**. The mode
matters more than the syntax; see step 4.

Confirm from the manual, for every command in the diff:

- exact syntax, argument order, which arguments are optional
- documented value ranges (check these against the `IntegerSpace` constants in
  the diff — a wrong range silently drops valid config)
- **which mode**: global config, or a submode, or *both* (the same keyword valid
  in two modes is the single richest source of real bugs — step 4)
- whether a `no` form is documented
- for removals: whether the command truly does not exist on this platform. A PR
  claiming "this syntax never applied to vendor X" is making a load-bearing
  claim; ask for support if the diff regresses previously-parsing input.

## 3. Build the probe harness

The reusable machinery, plus the compile errors you will hit, is in
[`references/probe-harness.md`](references/probe-harness.md). Read it before
writing probes.

In short: drop a temporary `zzProbe` test into the vendor's existing grammar test
class (it already has the parser, settings, and resource-path wiring), point it
at a scratch testconfig, print markers, and run it with `--test_output=all`.
Probe at two levels — `parseVendorConfig`-style for the vendor model, and
`parseConfig` for the VI model, since a bug can be invisible at one and obvious
at the other.

Delete every probe artifact when done and confirm `git status` is clean.

## 4. Probe checklist

Write one probe per hypothesis. For each, **run it on the PR branch and on the
merge base** and diff — a difference is the finding; identical behavior means
you have found a pre-existing trait, not a regression.

**Submode boundaries (highest yield).** For any new submode rule with an
unbounded `(...)*` loop, list the submode's sub-keywords and ask which also exist
as global commands. For each collision, put the global command *after* the
submode block and check where the value lands.

Then apply the fidelity check from step 5 before reporting.

Also test: does a comment line (`!`) end the submode? (Usually no — comments are
lexed on the hidden channel.) Does an unrelated global command terminate it
cleanly? Note that a keyword collision is often *shape*-protected — e.g. a
`timeout <dec>` submode rule won't match global `timeout xlate 3:00:00`, so the
loop exits correctly. Verify rather than assume, and say so if the protection is
incidental rather than designed.

**Submode escapes (the inverse).** When the diff adds a rule to the top-level
`stanza` alternation, check whether that keyword is *also* a sub-command of some
submode the grammar does **not** model — one ignored via `null_rest_of_line`,
which consumes only its own line and lets indented children fall through to
`stanza`. The child then matches the new global rule and writes to global state
that the device scopes per-submode. This is the mirror image of over-consumption
and is a genuine divergence from the device, not fidelity.

**Removed rules.** For every deleted rule, feed a config line that used to match
it. Confirm on the merge base that it parsed, then on the branch that it does
not. Grep `tests/**` and the vendor's `testconfigs/` for real instances.

**`no` forms.** If the change moves a keyword out of an ignored alternation that
allowed a leading `NO?` (e.g. `null_single`), the `no` form probably no longer
parses. Check the manual for whether it is documented.

**Ranges.** Probe each tuning value at, just inside, and just outside its
documented bounds. Confirm out-of-range values warn and are dropped rather than
stored.

**Reference tracking.** Confirm interface/object references produce the right
referrer counts and no undefined references, per Pattern 4. For vendors where a
logical name (`nameif`) registers as a structure, check the raw-vs-canonical name
choice is consistent with how the structure was defined.

**Then run the real suites:**

```bash
bazel test //projects/batfish/src/test/java/org/batfish/grammar/<vendor>:tests
bazel test //projects/batfish/src/test/java/org/batfish/representation/<vendor>:tests
bazel test //tests/parsing-tests:ref_tests     # required for grammar changes
./tools/run_checkstyle.sh
bazel run //:buildifier.check                  # if BUILD files changed
```

## 5. Severity rubric — apply before reporting anything

Run every candidate finding through these three gates in order. Most die here.

**Gate 1 — Device fidelity. What would the real device do given this exact
input?**

CLI submodes are **not** indentation-sensitive. You leave a submode with `exit`,
or implicitly by entering a command that is not valid in that mode. Whitespace
and comments do not end it. So if a submode rule consumes a trailing global
command, the device very likely scopes it to the submode too — Batfish is being
faithful, and there is no finding.

Then ask **reachability**: Batfish parses `show running-config` output, which is
emitted in canonical order (globals in a fixed early position, submode blocks
after). If your probe's ordering cannot appear in generated output, it is not a
realistic input even where behavior does diverge. Say so explicitly rather than
reporting it.

**Gate 2 — Does the wrong value reach anything?** Grep every getter the diff adds
for callers outside the extractor that writes it:

```bash
grep -rn "getMyNewField\|getMyOtherField" projects/batfish/src/main/java/org/batfish/
```

One caller (the extractor) or zero means the field is **write-only**: no
conversion code, no question, no VI model field reads it. Corrupt data in a
write-only field is not a user-visible defect. Report it as scope, not
correctness. Only state that reaches the VI `Configuration`, a question, or the
data plane can carry a correctness finding.

**Gate 3 — Is it a parse warning or a wrong answer?** Batfish handles parse
warnings well, and vendor grammars use newline-based error recovery: an
unparseable line becomes a warning and the rest of the snapshot still converts.
So "a line that used to be silently ignored now warns" is low severity — mention
it, do not block on it. Rank by:

1. **Blocking** — wrong data in the VI model, a question, or the data plane;
   silently (no warning) is worse than loudly.
2. **Notable** — a whole block dropped rather than one line (recovery only
   rescues the line it failed on); or misleading claims in the PR body that
   justify a behavior change.
3. **Low** — newly-surfaced parse warnings on input previously ignored.
4. **Convention** — `docs/` violations: rule naming, file organization, catch-all
   `_null` rules where one rule per keyword is wanted, non-LL(1) shape,
   multi-token advancement. Cite the doc.
5. **Scope** — extracted state nothing consumes; test configs whose command
   *ordering* dodges the interesting case. Note that unused new classes are also
   usually where a codecov complaint is really coming from — the fix is a
   consumer or a narrower diff, not more tests on `equals`.

## 6. Report

Use `ReportFindings`, most severe first, with `verdict: CONFIRMED` only for
findings you reproduced by probe. For each: the probe config, observed output on
the branch, observed output on the merge base, and the manual quote or `docs/`
citation that makes it wrong.

State plainly when you have no blocking finding. A PR can be correct and still
have convention and scope items worth raising; do not inflate those to fill a
review. If a probe you built turns out to describe device-faithful or
unreachable behavior, drop it rather than reporting it hedged.

Check whether prior review comments were addressed, and verify against the
current head rather than trusting the PR body:

```bash
gh pr view <N> --json comments,reviews \
  --jq '{comments: [.comments[] | {author: .author.login, body: .body}],
         reviews: [.reviews[] | {author: .author.login, state: .state, body: .body}]}'
```
