# filterLineReachability

## Overview

`filterLineReachability` identifies unreachable lines in ACLs and firewall rules — lines that will never match any packet because earlier lines have already matched all packets that would reach them.

**Location**: `projects/question/src/main/java/org/batfish/question/filterlinereachability/`

**Question definition**: `questions/experimental/filterLineReachability.json`

## Why This Question Exists

Unreachable filter lines are a common source of configuration errors:

1. **Shadowing bugs**: A broadly-scoped earlier line may unintentionally block a more specific later line, causing unexpected denials or permits.
2. **Dead code**: Lines that were once reachable may become unreachable after edits, leaving cruft that confuses future maintainers.
3. **Policy drift**: When blocking lines have different actions than the blocked line, traffic is being treated differently than the blocked line's author intended.

The 2023 SIGCOMM paper discusses this as part of "Lesson 5: Deep configuration modeling has many applications" — Batfish's detailed model of ACLs enables analyses beyond just packet forwarding, like finding redundant or contradictory rules.

## How It Works

### High-Level Flow

1. **Resolve specifiers** → identify which ACLs on which nodes to analyze
2. **Build ACL dependency graph** → handle ACLs that reference other ACLs
3. **Sanitize** → remove cycles, undefined references, dereference named IP spaces
4. **Deduplicate** → group identical ACLs across devices to avoid redundant analysis
5. **BDD analysis** → convert each line to a BDD, track reachable packet space
6. **Identify blocking lines** → for unreachable lines, compute a concise set of blockers

### BDD-Based Reachability Analysis

The core analysis (in `FilterLineReachabilityUtils.computeUnreachableFilterLines`) uses Binary Decision Diagrams to precisely represent the set of packets matched by each ACL line.

```
unmatchedPackets = all packets (BDD = 1)

for each line in ACL:
    lineBDD = convert line's match condition to BDD

    if lineBDD is empty:
        → line is INDEPENDENTLY_UNMATCHABLE
    else if lineBDD ∩ unmatchedPackets is empty:
        → line is UNREACHABLE (blocked by earlier lines)
    else:
        → line is reachable

    unmatchedPackets = unmatchedPackets - lineBDD
```

This approach is exact — no false positives or negatives for reachability determination.

### The Blocking Lines Algorithm

When a line is unreachable, users want to know *which* earlier lines blocked it. The naive answer — "all lines that match any packet the blocked line would match" — can be overwhelming. A DDoS-style ACL with 255 deny lines for individual IPs followed by a broad permit would report all 255 lines as blockers.

The algorithm (designed in batfish/batfish#2823, documented in the package's README.md) balances conciseness with correctness:

1. **Weight blocking lines by overlap**: For each earlier line that terminates packets the blocked line would match, compute the size of the overlap (using `BDD.satCount()`).

2. **Select a minimal covering set**: Sort blocking lines by weight descending, include lines until the blocked line is fully covered.

3. **Always include a line with different action**: If any blocking line has a different action than the blocked line, include the largest such line even if not needed for coverage. This ensures users see that the blocked line's traffic is being treated differently.

**Example**: Consider:
```
10 - deny tcp any 1.2.3.4/32 neq 80
20 - permit tcp any any
*30 - permit tcp any 1.2.3.4/32
```

Line 30 is blocked. Line 20 alone covers all of line 30's packets, but line 10 denies most of them with a different action. The algorithm reports `[10, 20]` with `diffAction=true` rather than just `[20]`, alerting the user that traffic to 1.2.3.4 (except port 80) is being denied, not permitted.

### ACL Dependency Handling

ACLs can reference other ACLs (e.g., Cisco's `evaluate` or object-groups). The answerer:

1. Builds a dependency graph via `ReferencedAclsCollector`
2. Detects cycles via DFS — lines in cycles are marked `CYCLICAL_REFERENCE`
3. Handles undefined references — lines referencing non-existent ACLs are marked `UNDEFINED_REFERENCE`
4. Flattens dependencies for BDD conversion

### Canonicalization for Deduplication

Large networks often have identical ACLs across many devices. To avoid redundant analysis:

1. **Erase device-specific metadata**: `AclEraser` removes `TraceElement` and `VendorStructureId` (which contain filenames)
2. **Build `CanonicalAcl`**: Includes the sanitized ACL plus its dependencies and referenced interfaces
3. **Group by equality**: ACLs that are structurally identical are analyzed once, results attributed to all sources

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `FilterLineReachabilityAnswerer` | Orchestrates the analysis: resolves specifiers, builds dependency graph, sanitizes, calls BDD analysis |
| `FilterLineReachabilityUtils` | Core BDD logic: converts lines to BDDs, identifies unreachable lines, computes blocking lines |
| `AclNode` | Graph node representing an ACL with its dependencies, cycles, and sanitized form |
| `HeaderSpaceSanitizer` | Dereferences named IP spaces, throws on cycles/undefined refs |
| `AclEraser` | Strips device-specific metadata for canonical comparison |
| `UnreachableFilterLine` | Abstract base for result types (visitor pattern) |
| `BlockedFilterLine` | Line blocked by earlier lines, with blocker list and diffAction flag |
| `IndependentlyUnmatchableFilterLine` | Line with empty match condition |
| `FilterLineWithUndefinedReference` | Line referencing undefined ACL or IP space |

## BDD Infrastructure

The question depends on the BDD infrastructure in `org.batfish.common.bdd`:

- **`BDDPacket`**: Represents packet header fields as BDD variables
- **`IpAccessListToBdd`**: Converts ACL lines to `PermitAndDenyBdds`
- **`PermitAndDenyBdds`**: Stores permit and deny BDDs separately (needed for stateful firewalls and lines that can both permit and deny)
- **`BDDSourceManager`**: Handles `MatchSrcInterface` expressions

## Output Schema

| Column | Description |
|--------|-------------|
| `Sources` | List of hostname:aclName pairs where this unreachable line appears |
| `Unreachable_Line` | The line number and content |
| `Unreachable_Line_Action` | PERMIT or DENY |
| `Blocking_Lines` | For BLOCKING_LINES reason: the computed set of blockers |
| `Different_Action` | True if any blocking line has different action than blocked line |
| `Reason` | BLOCKING_LINES, INDEPENDENTLY_UNMATCHABLE, UNDEFINED_REFERENCE, or CYCLICAL_REFERENCE |
| `Additional_Info` | Extra context (e.g., cycle members, undefined reference name) |

## Performance Considerations

- **Parallelization**: `computeUnreachableFilterLines` runs in parallel across ACL specs (batfish/batfish#7955)
- **Deduplication**: Canonical ACL grouping avoids redundant BDD computation
- **BDD efficiency**: Packet sets arising from ACL analysis tend to have compact BDD representations (see 2023 paper §4.2)

## Known Limitations

1. **Composite filters**: By default, `ignoreComposites=true` skips auto-generated composite filters (e.g., zone-based firewall combined ACLs) to reduce noise.

2. **Source interface modeling**: If an ACL references specific source interfaces, the analysis includes an "unreferencedInterface" placeholder to represent packets from other interfaces. This can affect blocking line computation for interface-dependent rules.

3. **Stateful behavior not modeled**: The analysis treats each line independently; it does not model stateful firewall session tracking. A line like `permit tcp established` is analyzed based on its match condition (ACK or RST flags), not on actual session state.

## Common Sources of Confusion

### "Why didn't it report the blocking line I expected?"

When a line is blocked by multiple earlier lines, users often have intuition about which line is the "real" blocker. The algorithm may return a different line.

**Example**:
```
10 - deny ospf any any
20 - deny ip any 10.0.0.1/32
*30 - permit ospf any 10.0.0.1/32
```

Line 30 is blocked. Both lines 10 and 20 contribute:
- Line 10 blocks all OSPF traffic (including to 10.0.0.1)
- Line 20 blocks all traffic to 10.0.0.1 (including OSPF)

Each line alone fully covers line 30's packet space. The algorithm picks based on overlap weight (see "The Blocking Lines Algorithm" above). Since both have identical overlap with line 30, it returns whichever comes first (line 10).

A user thinking "the problem is the destination deny" might expect line 20. A user thinking "the problem is the protocol deny" expects line 10. The algorithm has no way to know user intent.

This is a fundamentally hard problem — there's no objectively "correct" blocking line when multiple lines each fully cover the blocked line's packet space. We made pragmatic choices: optimize for conciseness while ensuring different-action lines are visible. See the [algorithm design doc](../../projects/question/src/main/java/org/batfish/question/filterlinereachability/README.md) for the detailed rationale and alternative approaches we considered.

**Takeaway**: The reported blocking lines are *a* correct answer, not necessarily *the* answer the user had in mind. When debugging, users may need to examine other earlier lines that also match the blocked line's packet space, or iterate by fixing the reported blockers and re-running the analysis.

### "I fixed the blocking lines but the line is still blocked"

Because the algorithm reports a minimal covering set, there may be additional earlier lines that also shadow the blocked line but weren't reported.

Using the example above: if the user removes line 10, line 30 is still blocked by line 20. The next run will report line 20 as the blocker.

This is expected behavior — the algorithm optimizes for concise output rather than exhaustive listing of all blocking lines. Users should expect to iterate when multiple independent lines shadow the same blocked line.

## Related Questions

- **`searchFilters`**: Find specific packets that match/don't match a filter
- **`testFilters`**: Test how a specific packet is treated by a filter
- **`compareFilters`**: Compare behavior of two filters
- **`findMatchingFilterLines`**: Find lines that match a specific packet space

## References

- [Package README.md](../../../projects/question/src/main/java/org/batfish/question/filterlinereachability/README.md) — detailed design of blocking lines algorithm
- batfish/batfish#2823 — PR implementing the blocking lines algorithm
- [Analyzing ACLs notebook](https://github.com/batfish/pybatfish/blob/master/jupyter_notebooks/Analyzing%20ACLs%20and%20Firewall%20Rules.ipynb) — user-facing examples
