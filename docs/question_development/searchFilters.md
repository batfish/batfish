# searchFilters

## Overview

`searchFilters` finds example flows for which a filter (ACL, firewall rule set) exhibits a specified behavior. Unlike `testFilters`, which evaluates a specific packet, `searchFilters` uses symbolic analysis to search across all possible packets matching optional constraints.

**Location**: `projects/question/src/main/java/org/batfish/question/searchfilters/`

**Question definition**: `questions/experimental/searchFilters.json`

## Why This Question Exists

Traditional testing with concrete packets has a fundamental limitation: you can only test packets you think to try. If a bug exists for packets you did not test, you will not find it.

`searchFilters` enables verification-style analysis:

1. **Prove absence of unwanted behavior**: "Show me any SSH traffic this firewall permits" — if empty, the firewall blocks all SSH.
2. **Find witness flows**: "Find a packet this ACL denies" — produces a concrete example for debugging.
3. **Verify line reachability**: "Find a packet that matches line 5" — confirms whether a specific rule ever fires.
4. **Differential analysis**: "What flows are newly permitted/denied after this change?" — validates ACL modifications.

The 2023 SIGCOMM paper (Section 4.2) discusses how BDD-based ACL analysis enables "complete" answers: instead of testing whether specific packets are permitted, users can verify properties about all packets.

## How It Works

### High-Level Flow

1. **Resolve specifiers** — identify which ACLs on which nodes to analyze
2. **Build BDD context** — set up packet variables and source interface tracking
3. **Convert header constraints** — user-specified packet constraints become a BDD
4. **Execute query** — compute the BDD representing flows matching the requested behavior
5. **Extract flow** — if a satisfying assignment exists, convert it to a concrete `Flow`
6. **Run testFilters** — trace the discovered flow through the filter for detailed output

### Query Types

The `action` parameter determines what flows to search for:

| Action | Query Class | BDD Computation |
|--------|-------------|-----------------|
| `permit` | `PermitQuery` | `toBdd(acl)` — flows the ACL permits |
| `deny` | `DenyQuery` | `toBdd(acl).not()` — flows the ACL denies |
| `matchLine N` | `MatchLineQuery` | `toBdd(line[N]) - toBdd(lines[0..N-1])` — flows reaching line N |

### BDD-Based Search

The core search logic in `NonDiffConfigContext.getReachBdd()`:

```
headerSpaceBdd = convert user constraints to BDD
validSourceBdd = valid source interface assignments
prerequisiteBdd = headerSpaceBdd AND validSourceBdd

queryBdd = query.getMatchingBdd(acl, ...)  // permit, deny, or matchLine
resultBdd = queryBdd AND prerequisiteBdd

if resultBdd is satisfiable:
    return concrete flow from resultBdd
else:
    return no result
```

This is exact: if a flow exists matching the constraints and query, the analysis finds one. If no flow exists, the result is empty — a proof that no such flow can exist.

### Differential Mode

When comparing two snapshots, `searchFilters` finds flows where the filter's behavior changed:

```java
// In getDiffResult()
bdd = getReachBdd(acl, query)         // flows matching query in current
refBdd = getReachBdd(refAcl, query)   // flows matching query in reference

increasedBDD = bdd.diff(refBdd)       // newly matches query
decreasedBDD = refBdd.diff(bdd)       // no longer matches query
```

For example, with `action=permit`:
- `increasedFlow`: a packet now permitted that was previously denied
- `decreasedFlow`: a packet now denied that was previously permitted

### Source Interface Handling

ACLs can reference the packet's ingress interface via `MatchSrcInterface`. The analysis:

1. Collects all source interfaces referenced by the ACL (`referencedSources`)
2. Intersects with active interfaces on the device (`activeAclSources`)
3. Uses `BDDSourceManager` to encode valid source assignments
4. Reports the source interface in the output flow

The `startLocation` parameter restricts which sources to consider, enabling queries like "find a flow entering via eth0 that this ACL permits."

### The invertSearch Parameter

The `invertSearch` parameter inverts the *header space*, not the query action. This is subtle but important:

| invertSearch | action=permit | Finds flows that... |
|--------------|---------------|---------------------|
| false (default) | permit | Match constraints AND are permitted |
| true | permit | Do NOT match constraints AND are permitted |
| false | deny | Match constraints AND are denied |
| true | deny | Do NOT match constraints AND are denied |

**Use case**: Verify that *only* certain traffic is permitted. With `invertSearch=true` and `action=permit`, you find permitted flows *outside* your expected traffic profile — ideally empty.

```python
# Verify only HTTP/HTTPS is permitted to the web server
result = bf.q.searchFilters(
    filters="web-acl",
    headers=HeaderConstraints(dstIps="10.0.0.1", dstPorts="80,443"),
    action="permit",
    invertSearch=True  # Find permitted traffic that is NOT HTTP/HTTPS
).answer()
assert result.frame().empty, "Unexpected traffic permitted to web server"
```

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `SearchFiltersQuestion` | Question parameters: nodes, filters, headers, action, invertSearch |
| `SearchFiltersAnswerer` | Orchestrates analysis: resolves ACLs, builds BDD context, executes queries |
| `SearchFiltersQuery` | Interface for query types (permit, deny, matchLine) |
| `PermitQuery` | Returns BDD of flows the ACL permits |
| `DenyQuery` | Returns BDD of flows the ACL denies (complement of permit) |
| `MatchLineQuery` | Returns BDD of flows matching a specific line, minus earlier lines |
| `NonDiffConfigContext` | Holds BDD state for single-snapshot analysis |
| `DiffConfigContext` | Holds BDD state for differential analysis |
| `DifferentialSearchFiltersResult` | Container for increased/decreased flow results |
| `SearchFiltersParameters` | Resolved parameters including header space and location specifiers |
| `FilterQuestionUtils` | Shared utilities for filter questions (source resolution, flow extraction) |

## BDD Infrastructure

The question depends on the BDD infrastructure in `org.batfish.common.bdd`:

- **`BDDPacket`**: Represents packet header fields as BDD variables
- **`IpAccessListToBdd`**: Converts ACL to BDD representing permitted packets
- **`BDDSourceManager`**: Tracks and encodes source interface assignments
- **`BDDFlowConstraintGenerator`**: Extracts concrete flows from satisfying assignments

## Output Schema

The output reuses the `testFilters` schema (flows are traced through the filter for detailed results):

| Column | Description |
|--------|-------------|
| `Node` | Hostname where the filter exists |
| `Filter_Name` | Name of the ACL/filter |
| `Flow` | The discovered flow matching the query |
| `Action` | PERMIT or DENY — the filter's decision |
| `Line_Content` | Name or description of the matching line |
| `Trace` | Detailed trace through the filter |

In differential mode, the output includes results from both snapshots for comparison.

## Performance Considerations

- **BDD efficiency**: ACL matching conditions typically yield compact BDD representations. The 2023 paper (Section 4.2) notes that real-world ACLs rarely cause BDD blowup.

- **One flow per filter**: The question returns at most one example flow per filter. Finding additional examples requires modifying constraints to exclude the first result.

- **Header constraint impact**: Tighter header constraints (specific IPs, ports) reduce the BDD size and speed up satisfiability checking.

- **No parallelization**: Unlike `filterLineReachability`, each filter is analyzed sequentially. The per-filter cost is typically low.

## Known Limitations

1. **Single example only**: The question returns one flow, not all flows. Use `invertSearch=true` with additional constraints to find more examples iteratively.

2. **Stateful behavior not modeled**: Like other filter questions, `searchFilters` does not model stateful session tracking. A line matching `tcp established` is analyzed based on TCP flags, not connection state.

3. **Source interface scope**: When `startLocation` is unspecified, all active interfaces are considered. This may include internal interfaces not typically receiving external traffic.

4. **No line-level detail for permit/deny**: The `permit` and `deny` actions find flows matching the overall ACL behavior but do not indicate which specific line caused the result. Use `matchLine` for line-specific queries, or examine the trace in the output.

## Common Sources of Confusion

### "Why did it return this flow instead of a simpler one?"

The BDD satisfiability solver (`satOne`) returns an arbitrary satisfying assignment. It does not optimize for "simple" or "representative" flows. The flow preference (`FlowPreference.TESTFILTER`) biases toward certain IP ranges but makes no guarantees.

If the returned flow seems strange, check:
- Are your header constraints correct?
- Is the flow valid given source interface restrictions?

### "I expected a result but got nothing"

Empty results mean no packet exists matching both your constraints and the query. This is a proof, not a bug. Check:
- Are your header constraints satisfiable (e.g., not `srcIps=1.1.1.1` AND `srcIps=2.2.2.2`)?
- Does the ACL actually have the behavior you are querying for?
- If using `invertSearch`, did you invert the wrong direction?

### "matchLine N returns nothing, but filterLineReachability says line N is reachable"

The `matchLine` query finds flows that *match* line N after passing through lines 0..N-1. This differs from reachability in subtle ways:

1. **Header constraints too narrow**: Your constraints may exclude the packets that would reach line N. Try relaxing constraints or omitting them entirely.

2. **Source interface mismatch**: If line N uses `MatchSrcInterface`, the query respects your `startLocation` parameter. Ensure the location can actually match.

3. **Interaction with `invertSearch`**: If you accidentally set `invertSearch=true`, you're searching for packets *outside* your header constraints that match line N.

### "How do I find multiple example flows, not just one?"

`searchFilters` returns at most one example per filter. To find additional examples:

1. **Exclude the first result**: Add constraints to rule out the returned flow, then re-run:
   ```python
   # First query
   result1 = bf.q.searchFilters(filters="acl1", action="permit").answer()
   first_flow = result1.frame().iloc[0].Flow

   # Second query: exclude first result's destination
   result2 = bf.q.searchFilters(
       filters="acl1",
       action="permit",
       headers=HeaderConstraints(dstIps=f"!{first_flow.dstIp}")
   ).answer()
   ```

2. **Use different constraints**: Search for flows in different header spaces (different protocols, port ranges, etc.).

3. **For comprehensive analysis**: Use `filterLineReachability` to understand the full structure, then target specific lines with `matchLine`.

### "What's the difference between searchFilters and testFilters?"

| Aspect | `testFilters` | `searchFilters` |
|--------|--------------|-----------------|
| Input | Specific packet (concrete) | Packet constraints (symbolic) |
| Output | How that packet is processed | An example packet with specified behavior |
| Use case | "Does this packet get blocked?" | "Can any packet get through?" |

Use `testFilters` when you have a specific flow to test. Use `searchFilters` when you want to find flows or prove properties.

### "The filter behavior doesn't match the real device"

Like other filter questions, `searchFilters` models L3/L4 packet headers but does not model:

- **L7 / application-layer inspection**: Deep packet inspection, URL filtering, application identification
- **L2 headers**: MAC addresses, VLAN tags (except where explicitly modeled)
- **Nested packets**: Packets inside tunnels, GRE payloads, IPsec ESP contents
- **Dynamic state**: Connection tracking, session tables, rate limits

This is particularly relevant for **Palo Alto Networks** firewalls and other next-gen firewalls where application-aware policies are common. Batfish results represent the L3/L4 filtering behavior — real devices may permit or deny additional traffic based on unmodeled features.

## Related Questions

- **`testFilters`**: Test how a specific packet is processed by a filter (concrete analysis)
- **`filterLineReachability`**: Find unreachable lines in filters (comprehensive symbolic analysis)
- **`compareFilters`**: Compare filters line-by-line between snapshots (pairs of lines with different actions)
- **`findMatchingFilterLines`**: Find all lines that match a given packet space

## Design Evolution

The question has evolved significantly from its original Z3-based implementation:

- **batfish/batfish#5334**: Removed earlier Z3-based `AclExplainer` in favor of BDD approach. Z3 was slower and less predictable for ACL analysis than BDDs.
- **batfish/batfish#5345**: Major refactor to use BDDs for all search logic (January 2020). This aligned `searchFilters` with the BDD infrastructure used by `filterLineReachability` and reachability analysis.
- **batfish/batfish#5920**: Usability improvements including better flow preference and clearer output formatting.
- **Differential mode**: Added support for comparing filter behavior across snapshots, enabling change validation workflows.

## References

- [Analyzing ACLs documentation](https://pybatfish.readthedocs.io/en/latest/notebooks/filters.html) — user-facing examples
- [Symbolic Engine documentation](../symbolic_engine/README.md) — overview of BDD-based analysis
- [2023 SIGCOMM paper](https://dl.acm.org/doi/10.1145/3603269.3604873) — Section 4.2 on BDD-based ACL analysis
