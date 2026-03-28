# searchRoutePolicies

## Overview

`searchRoutePolicies` uses symbolic analysis to find BGP route announcements for which a route policy exhibits a specified behavior (permit or deny). Unlike `testRoutePolicies` which tests specific concrete routes, this question searches over the *entire space* of possible routes to find examples that satisfy given constraints.

**Location**: `projects/minesweeper/src/main/java/org/batfish/minesweeper/question/searchroutepolicies/`

**Question definition**: `questions/experimental/searchRoutePolicies.json`

## Why This Question Exists

Network operators need to verify properties about route policies that hold for *all* possible routes, not just a handful of test cases:

1. **Policy verification**: Prove that a route policy will never permit routes matching certain criteria (e.g., routes with specific communities are always denied).

2. **Route leak detection**: Find if there exist any routes that could leak through a policy when they should not.

3. **Output constraint validation**: Verify that permitted routes are modified correctly (e.g., local preference is always set to a specific value for certain prefixes).

4. **Exhaustive testing**: Traditional testing with a few example routes cannot cover the infinite space of possible BGP announcements. Symbolic analysis provides mathematical guarantees.

The 2023 SIGCOMM paper describes this as part of Batfish's verification capabilities: symbolic analysis allows reasoning about all possible inputs rather than just sampled test cases.

## How It Works

### High-Level Flow

1. **Resolve specifiers** - Identify which route policies on which nodes to analyze
2. **Build atomic predicates** - Compute atomic predicates for communities and AS-paths across all policies
3. **Symbolic route analysis** - For each policy, use `TransferBDD` to compute the policy's behavior symbolically
4. **Apply constraints** - Intersect the symbolic results with user-provided input/output constraints
5. **Generate witness** - If the constraints are satisfiable, extract a concrete example route
6. **Run testRoutePolicies** - Trace the discovered route through the policy for detailed output and attribute diff

### Symbolic Route Representation

The core of the analysis uses Binary Decision Diagrams (BDDs) to represent sets of routes symbolically. The `BDDRoute` class encodes all attributes of a BGP route announcement:

- **Prefix and prefix length**: 32 + 6 bits
- **Local preference, MED, tag**: 32 bits each
- **Communities**: One BDD variable per atomic predicate
- **AS-path**: A `BDDDomain` encoding which atomic predicate matches
- **Origin type, protocol**: Enumerated domains
- **Next-hop IP**: 32 bits

A single `BDDRoute` can represent infinitely many concrete routes. For example, a BDD constraint like "prefix is in 10.0.0.0/8 AND has community 65000:100" represents all routes matching those criteria.

### TransferBDD: Symbolic Policy Execution

`TransferBDD` is the symbolic interpreter for route policies. Given a routing policy, it computes a list of `TransferReturn` objects, one per execution path through the policy:

```
TransferReturn = (BDDRoute outputRoute, BDD inputConstraints, boolean accepted)
```

Each `TransferReturn` represents:
- The set of input routes that take this path (`inputConstraints`)
- The symbolic output route after policy modifications (`outputRoute`)
- Whether the path accepts or rejects the route (`accepted`)

For a route map with multiple clauses, `TransferBDD` explores all paths:

```
route-map EXAMPLE
  10 match community LARGE permit
     set local-preference 200
  20 match prefix-list INTERNAL permit
     set local-preference 100
  30 deny
```

This produces three paths:
1. Input has LARGE community -> permit, LP=200
2. Input matches INTERNAL, no LARGE community -> permit, LP=100
3. Input matches neither -> deny

### Atomic Predicates for Communities and AS-Paths

Communities and AS-paths are matched via regular expressions. To handle these efficiently, the analysis computes *atomic predicates* - a minimal partition of the space such that each regex either matches all values in an atomic predicate or none.

For example, given regexes `.*:100` and `65000:.*`:
- Atomic predicate 0: Matches both (e.g., `65000:100`)
- Atomic predicate 1: Matches only `.*:100` (e.g., `65001:100`)
- Atomic predicate 2: Matches only `65000:.*` (e.g., `65000:200`)
- Atomic predicate 3: Matches neither

`ConfigAtomicPredicates` computes these partitions and allocates BDD variables to track which atomic predicates are satisfied by a route.

### Constraint Solving

The answerer combines multiple BDD constraints:

```java
BDD solution = inputConstraints          // routes that take this path
    .and(userInputConstraints)           // user's constraints on input route
    .and(outputConstraints)              // (for permit) user's constraints on output
```

If the conjunction is satisfiable (not the zero BDD), `ModelGeneration.constraintsToModel()` extracts a satisfying assignment and converts it to a concrete `Bgpv4Route`.

### Path Options

The `pathOption` parameter controls how results are generated:

- **SINGLE** (default): Return one example route satisfying constraints (fastest)
- **PER_PATH**: Return one example per execution path through the policy (useful for understanding all policy behaviors)
- **NON_OVERLAP**: Return examples for different paths with non-overlapping prefixes (useful for test case generation)

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `SearchRoutePoliciesAnswerer` | Orchestrates the analysis: resolves specifiers, builds atomic predicates, searches each policy |
| `SearchRoutePoliciesQuestion` | Question parameters: nodes, policies, input/output constraints, action, path options |
| `BgpRouteConstraints` | User-specified constraints on route attributes (prefix, communities, LP, MED, AS-path, etc.) |
| `TransferBDD` | Symbolic interpreter that computes policy behavior as BDD constraints per execution path |
| `BDDRoute` | Symbolic representation of a BGP route using BDDs for each attribute |
| `TransferReturn` | Result of symbolic analysis: input constraints, output route, and accept/reject for one path |
| `ConfigAtomicPredicates` | Computes atomic predicates for communities and AS-path regexes |
| `ModelGeneration` | Converts satisfying BDD assignments to concrete routes |
| `RegexConstraints` | User constraints on communities/AS-paths as positive/negative regex lists |

## Output Schema

The question calls `testRoutePolicies` internally to trace discovered routes through the policy, so it reuses the same output schema:

| Column | Description |
|--------|-------------|
| `Node` | The node that has the policy |
| `Policy_Name` | The name of the analyzed route policy |
| `Input_Route` | A concrete BGP route that satisfies the input constraints |
| `Action` | PERMIT or DENY (matches the requested action) |
| `Output_Route` | The route after policy processing (for PERMIT action) |
| `Difference` | Changes made to the route by the policy |
| `Trace` | Execution trace through the policy (experimental) |

When `pathOption` is PER_PATH or NON_OVERLAP, multiple rows may be returned per policy.

## Performance Considerations

- **Atomic predicate computation**: Done once per configuration, shared across all policies on that node. The number of atomic predicates grows with the number of distinct community/AS-path regexes (batfish/batfish#8625).

- **Path explosion**: Route maps with many clauses can have exponentially many execution paths. The SINGLE path option avoids exploring all paths.

- **BDD factory**: Each policy analysis uses its own BDD factory to avoid variable conflicts. A recent fix (batfish/batfish#9521) ensures correct factory usage when generating output constraints.

- **Unsupported features**: Paths encountering unsupported features are analyzed last, reducing false positive potential. Unsupported features are logged as warnings.

## Known Limitations

1. **Incomplete route policy support**: Not all route policy constructs are supported. The question logs unsupported features as warnings. Common unsupported features include:
   - Some complex AS-path manipulations
   - Certain vendor-specific match conditions
   - Advanced community operations

2. **BGP routes only**: The analysis is designed for BGP route announcements. While there is some support for static routes (batfish/batfish#9339), the primary use case is BGP.

3. **Output constraints with deny**: Output constraints can only be specified when the action is `permit`, since denied routes have no meaningful output.

4. **AS-path prepending complexity**: AS-path output constraints must account for prepending that occurs along the execution path. The analysis handles this by updating atomic predicates to reflect prepended ASes (batfish/batfish#8630).

5. **Environment dependencies**: Some policy behaviors depend on runtime environment (e.g., BGP session properties, track status). The analysis uses symbolic placeholders for some of these; others may require explicit specification.

## Common Sources of Confusion

### "The question returned no results but I expected matches"

Several causes:

1. **Unsupported features**: If the policy uses unsupported constructs, the analysis may be incomplete. Check warnings in the answer.

2. **Overconstrained**: The combination of input constraints, output constraints, and policy behavior may be unsatisfiable. Try relaxing constraints.

3. **Wrong action**: If searching for `deny` but all matching routes are permitted (or vice versa), no results are returned.

### "The output route has unexpected values"

The question uses symbolic placeholders for environment-dependent values:
- `NextHopSelf`: The policy sets next-hop to self; actual IP depends on session
- `NextHopBgpPeerAddress`: The policy sets next-hop to peer address

These symbolic values are returned when the concrete IP cannot be determined without session context.

### Difference from `testRoutePolicies`

| Aspect | `testRoutePolicies` | `searchRoutePolicies` |
|--------|--------------------|-----------------------|
| Input | Concrete routes you specify | Constraints on routes |
| Analysis | Concrete execution | Symbolic/exhaustive |
| Use case | Test known scenarios | Find unknown scenarios |
| Speed | Fast | Slower (symbolic reasoning) |
| Completeness | Tests what you provide | Covers all matching routes |

## Related Questions

- **`testRoutePolicies`**: Test specific concrete routes through a policy (complementary - use this to validate results from searchRoutePolicies)
- **`compareRoutePolicies`**: Symbolically compare two policies to find routes they treat differently (batfish/batfish#8612)
- **`routes`**: View actual routes in the computed data plane (useful for understanding real traffic patterns)

## References

- [Symbolic Engine Documentation](../symbolic_engine/README.md) - Detailed explanation of BDD-based analysis
- batfish/batfish#8622 - PR adding per-path analysis support
- batfish/batfish#8630 - PR adding AS-path output constraints
- batfish/batfish#8861 - PR allowing community-list names as constraints
- batfish/batfish#9521 - Fix for BDD factory usage in output constraints
