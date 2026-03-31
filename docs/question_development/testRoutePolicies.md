# testRoutePolicies

## Overview

`testRoutePolicies` evaluates how specified routing policies (route-maps, policy-statements) process given BGP or static routes. It simulates the policy execution and shows whether the route is permitted or denied, along with any attribute transformations.

**Location**: `projects/question/src/main/java/org/batfish/question/testroutepolicies/`

**Question definition**: `questions/experimental/testRoutePolicies.json`

## Why This Question Exists

Network operators need to understand routing policy behavior before deployment:

1. **Policy verification**: Confirm that a route-map accepts or rejects the expected routes and modifies attributes as intended (e.g., setting communities, local preference, MED).
2. **Debugging routing issues**: When routes are not being advertised or received as expected, operators can test specific routes against policies to identify the cause.
3. **Change validation**: Before modifying a routing policy, test both the current and proposed versions against representative routes to ensure intended behavior.
4. **Documentation and training**: Demonstrate how policies process different route types to document expected behavior.

Unlike `searchRoutePolicies` which uses symbolic analysis to find *any* route matching constraints, `testRoutePolicies` provides *concrete* testing of specific routes. This makes it useful for targeted debugging and verification of known routes.

## How It Works

### High-Level Flow

1. **Resolve specifiers** - Identify which routing policies on which nodes to test
2. **Convert input routes** - Transform user-provided BGP/static routes to internal representation
3. **Simulate policy** - Execute the routing policy against each input route
4. **Collect results** - Record action (permit/deny), output route attributes, and execution trace
5. **Compute diffs** - For permitted routes, show what attributes changed

### Policy Evaluation

The core simulation happens in `TestRoutePoliciesAnswerer.processPolicy()`:

```
for each (policy, input_route) pair:
    1. Create Environment with:
       - Configuration context (route-filter-lists, community-sets, etc.)
       - Direction (IN or OUT)
       - BGP session properties (if provided)
       - Input route as original route
       - Output route builder

    2. Execute policy.process(environment)
       - Statements execute sequentially
       - Match conditions check route attributes
       - Set statements modify output route
       - Accept/reject terminates execution

    3. Record Result:
       - PERMIT if policy returns true
       - DENY if policy returns false
       - Output route (for PERMIT)
       - Execution trace (which clauses matched)
```

### Trace Generation

The `Trace` column shows the execution path through the policy. Tracing is implemented by wrapping policy statements in `TraceableStatement` objects that record when they execute:

```
Trace structure for a route-map:
├── "Matched clause 10"
│   ├── "Matched community-list LARGE"
│   └── "Set local-preference 200"
```

**How it works**: During `RoutingPolicy.process()`, each `TraceableStatement` adds its trace element to the `Tracer` when executed. The trace captures which clauses matched, which conditions were evaluated, and which set operations were applied.

**Vendor implementation quality matters**: Trace clarity depends on how well each vendor's extraction code wraps statements with meaningful trace elements. Some vendors have comprehensive tracing; others have minimal or no trace output.

**Call statements and subroutine policies**: Policies that call other policies (Cisco `call`, Juniper `policy`, Arista `match policy`) create nested traces. This can be confusing because:

1. **The called policy's trace appears inline** — it may not be obvious where the main policy ends and the subroutine begins
2. **Multiple levels of nesting** — deeply nested calls produce deeply nested traces
3. **Accept/reject in subroutines** — when a subroutine accepts or rejects, it may not be clear from the trace whether that terminates the entire policy or just the subroutine (vendor-dependent semantics)

For example, a Juniper policy with `policy SUB-POLICY` produces:
```
├── "Matched term MAIN"
│   ├── "Called policy SUB-POLICY"
│   │   ├── "Matched term INNER"
│   │   └── "Set community ADD-COMM"
│   └── "Set local-preference 100"
```

The nesting reflects the call structure, but understanding the control flow requires knowing the vendor's subroutine semantics.

### Direction (IN vs OUT)

The `direction` parameter affects how the policy processes routes:

- **IN**: Simulates routes being received (import policy). The next-hop IP from the input route is preserved.
- **OUT**: Simulates routes being advertised (export policy). The next-hop IP is initially unset (NextHopDiscard) and must be set by the policy or will reflect the "unset" state.

This distinction matters because:
- Export policies typically need to set next-hop (often to self or peer address)
- Import policies typically preserve received next-hop

### Vendor-Specific Matching Behavior

The Environment's `useOutputAttributes` flag controls match semantics:

- **Cisco/IOS-style** (`useOutputAttributes=false`): Match conditions evaluate the *original* (input) route attributes. Setting a value and then matching on it will not match the newly-set value.
- **Juniper-style** (`useOutputAttributes=true`): Match conditions evaluate the *output* route attributes. A `set metric 42` followed by `match metric 42` will match.

This accurately models real vendor behavior and is determined automatically based on configuration format.

### BGP Session Properties

The optional `bgpSessionProperties` parameter provides session context needed for:
- `set next-hop peer-address` - Requires knowing the peer's IP
- AS path operations - May need local/remote AS numbers
- Community operations that reference session properties

Without session properties, these operations may not work correctly or may use default values.

### Static Route Support

Added in batfish/batfish#9334, the question also supports static routes as input. This is useful for testing policies that match on `protocol static` and redistribute static routes into BGP. The static route is converted to a BGP route builder for processing, with:
- Next-hop IP from the static route
- Origin type INCOMPLETE
- Origin mechanism NETWORK

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `TestRoutePoliciesAnswerer` | Orchestrates evaluation: resolves specifiers, converts routes, calls policy simulation, builds result rows |
| `TestRoutePoliciesQuestion` | Question parameters: nodes, policies, inputRoutes, direction, bgpSessionProperties |
| `Result<I,O>` | Holds simulation result: policy ID, input route, action, output route, trace |
| `RoutingPolicyId` | Global identifier for a routing policy (node name, policy name) |
| `RoutingPolicy` | Vendor-independent representation of a routing policy with `process()` method |
| `Environment` | Evaluation context: configuration structures, direction, session properties |
| `Tracer` | Records which policy statements matched during execution |

## Input Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `nodes` | nodeSpec | No | Only examine policies on matching nodes (default: all nodes) |
| `policies` | routingPolicySpec | No | Only consider matching policies (default: all policies) |
| `inputRoutes` | bgpRoutes | Yes | The BGP routes to test against the policies |
| `direction` | string (IN/OUT) | Yes | Whether policy is used for import or export |
| `bgpSessionProperties` | bgpSessionProperties | No | Session context (localAs, remoteAs, localIp, remoteIp) |

## Output Schema

| Column | Description |
|--------|-------------|
| `Node` | The node containing the policy |
| `Policy_Name` | Name of the routing policy |
| `Input_Route` | The input BGP route |
| `Action` | PERMIT or DENY |
| `Output_Route` | The transformed route (null if denied) |
| `Difference` | Attribute changes between input and output routes |
| `Trace` | Execution trace showing which clauses/terms matched |

### Differential Mode

When run in differential mode (comparing two snapshots), additional columns appear:

| Column | Description |
|--------|-------------|
| `Base_Action` | Action in the current snapshot |
| `Delta_Action` | Action in the reference snapshot |
| `Base_Output_Route` | Output route in current snapshot |
| `Delta_Output_Route` | Output route in reference snapshot |
| `Difference` | Differences between the two output routes |

Only routes with different outcomes between snapshots are reported.

## Performance Considerations

- **Parallelization**: Route evaluations run in parallel across (policy, route) pairs (batfish/batfish#7877)
- **No data plane required**: Analysis works directly on the vendor-independent model without computing RIBs/FIBs
- **Scales with input size**: Performance is O(policies * routes * policy_complexity)

## Known Limitations

1. **No symbolic analysis**: Unlike `searchRoutePolicies`, this question tests specific concrete routes only. It cannot find all routes that would match a policy.

2. **Track conditions**: The `successfulTrack` predicate defaults to `alwaysFalse()`. Policies using track-based conditions will evaluate as if all tracks are down unless explicitly configured.

3. **EIGRP process context**: While the infrastructure supports EIGRP process context for metric calculations, the question does not expose this parameter.

4. **Next-hop semantics**: The `NextHopDiscard` type is overloaded to mean both "discard route" and "next-hop not set". In OUT direction results, this ambiguity can be confusing. See the TODO comment in `toQuestionBgpRoute()`.

5. **Limited route types**: Only BGP routes and static routes are supported. Other route types (OSPF, ISIS, EIGRP) cannot be tested directly.

## Common Sources of Confusion

### "Why does my route-map permit the route but not set the next-hop?"

In OUT direction, the output route's next-hop starts as unset (`NextHopDiscard`). If the route-map doesn't explicitly set the next-hop (e.g., via `set ip next-hop peer-address`), the output will show `NextHopDiscard`. This is expected behavior - in real BGP, the next-hop would typically be set by the BGP process after the export policy runs.

### "Why don't my match conditions work after a set statement?"

This depends on vendor behavior. On Cisco/IOS devices, match conditions evaluate the *original* route attributes, not the modified output attributes. This is by design and matches real device behavior. See "Vendor-Specific Matching Behavior" above.

To test Juniper-style behavior, ensure your configuration files are parsed as Juniper format, which will set `useOutputAttributes=true`.

### "The trace is empty or confusing"

Tracing depends on `TraceableStatement` wrappers in vendor extraction code:

1. **Empty traces**: If a vendor's policy conversion doesn't use traceable statements, the trace will be empty. This is a work-in-progress feature (batfish/batfish#7082). Cisco IOS route-maps have the most complete tracing.

2. **Confusing nested traces**: Policies with call statements (`call`, `policy`, `match policy`) produce nested traces that can be hard to interpret. The trace shows *what* executed but doesn't always make clear *why* — especially when subroutine semantics differ between vendors (e.g., whether `accept` in a subroutine terminates the entire policy).

3. **Missing context**: Trace elements describe individual statements but may lack the broader context of which clause or term they belong to, especially for vendors with less mature tracing support.

### "My session property expressions don't work"

If you're using expressions like `set next-hop peer-address` or `match as-path-group`, ensure you've provided `bgpSessionProperties` with the appropriate values. Without session context, these expressions may not behave as expected.

### "What's the difference between testRoutePolicies and searchRoutePolicies?"

| Aspect | `testRoutePolicies` | `searchRoutePolicies` |
|--------|---------------------|----------------------|
| Input | Specific routes (concrete) | Route attribute constraints (symbolic) |
| Output | How those routes are processed | Routes matching criteria the policy would accept/reject |
| Use case | "What happens to this specific route?" | "Can any route with community X be accepted?" |

Use `testRoutePolicies` when you have specific routes to test. Use `searchRoutePolicies` when you want to find routes or prove properties about policy behavior.

## Related Questions

- **`searchRoutePolicies`**: Find routes matching constraints that a policy would accept/reject (symbolic analysis)
- **`compareRoutePolicies`**: Compare two policies to find routes where they behave differently (symbolic analysis)
- **`routes`**: Show the actual routes in the network's RIBs
- **`bgpRib`**: Show BGP-specific RIB contents with all BGP attributes

## Design Evolution

The question has evolved significantly since its initial creation:

- **batfish/batfish#3434**: Initial implementation as `testPolicies`
- **batfish/batfish#3448**: Renamed to `testRoutePolicies`
- **batfish/batfish#7082, #7109**: Added execution tracing support
- **batfish/batfish#7133**: Added OUT direction support for export policies
- **batfish/batfish#8987**: Added BGP session properties parameter
- **batfish/batfish#9334**: Added static route support

## References

- [Analyzing Routing Policies documentation](https://pybatfish.readthedocs.io/en/latest/notebooks/routingProtocols.html) — user-facing examples
- [2023 SIGCOMM paper](https://dl.acm.org/doi/10.1145/3603269.3604873) — Section 4.4 on usability and concrete testing tools
