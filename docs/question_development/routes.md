# routes

## Overview

`routes` displays the contents of routing tables (RIBs) after data plane computation. It supports three RIB types: the main RIB (combined best routes from all protocols), the BGP RIB (BGPv4-specific routes), and the EVPN RIB (EVPN BGP routes). The question supports both single-snapshot queries and differential mode to compare routes across two snapshots.

**Location**: `projects/question/src/main/java/org/batfish/question/routes/`

**Question definitions**:
- `questions/stable/routes.json` (main RIB, default)
- `questions/stable/bgpRib.json` (BGP RIB, uses same implementation with `rib: "bgp"`)

## Why This Question Exists

Routing tables are the foundation of network forwarding behavior. Operators and network engineers need visibility into computed routes to:

1. **Verify route propagation**: Confirm that routes are being learned from expected neighbors with expected attributes (AS path, local preference, communities).

2. **Debug routing issues**: When traffic takes an unexpected path, examining the RIB reveals which routes were selected and why (based on protocol, metric, admin distance).

3. **Validate network changes**: Differential mode shows precisely which routes changed between snapshots, making it straightforward to verify that a configuration change had the intended effect.

4. **Compare protocol-specific state**: The BGP and EVPN RIBs expose BGP-specific attributes (AS path, local preference, communities, origin type, weight) that are lost when routes are imported into the main RIB.

This question implements the "show routes" and "show bgp" functionality that operators use on real devices, but computed from Batfish's simulation of the network.

The 2023 SIGCOMM paper (Section 4.1) describes Batfish's imperative data plane generation, which computes RIBs by simulating protocol message exchange until convergence. The `routes` question exposes these computed RIBs for inspection.

## How It Works

### High-Level Flow

1. **Load data plane**: The answerer requires a computed data plane (`getDataPlane() == true`), which contains the converged RIBs.

2. **Resolve specifiers**: Node and VRF specifiers determine which devices and VRFs to query.

3. **Filter routes**: Routes are filtered by:
   - Prefix/network (with configurable match type)
   - Routing protocol(s)
   - BGP route status (best/backup, for BGP/EVPN RIBs)

4. **Format output**: Routes are converted to table rows with RIB-specific columns.

5. **Sort results**: Rows are sorted by node, VRF, network, and next hop for consistent output.

### RIB Selection

The `rib` parameter selects which routing table to query:

| RIB | Source | Key Attributes |
|-----|--------|----------------|
| `main` (default) | `DataPlane.getRibs()` | Admin distance, metric, next hop, protocol |
| `bgp` | `DataPlane.getBgpRoutes()` / `getBgpBackupRoutes()` | AS path, local pref, communities, origin type, weight, received-from IP |
| `evpn` | `DataPlane.getEvpnRoutes()` / `getEvpnBackupRoutes()` | Same as BGP plus route distinguisher |

### Prefix Match Types

The `prefixMatchType` parameter controls how the `network` filter matches:

| Type | Behavior | Example: `network=10.0.0.0/16` |
|------|----------|-------------------------------|
| `EXACT` | Matches only the specified prefix | Returns routes for 10.0.0.0/16 only |
| `LONGEST_PREFIX_MATCH` | Returns the most specific matching prefix | If 10.0.0.0/24 exists, returns it; otherwise 10.0.0.0/16 |
| `LONGER_PREFIXES` | Matches all prefixes contained within the input | Returns 10.0.0.0/24, 10.0.1.0/24, etc. |
| `SHORTER_PREFIXES` | Matches all prefixes that contain the input | Returns 10.0.0.0/8, 0.0.0.0/0, etc. |

For BGP/EVPN RIBs with `LONGEST_PREFIX_MATCH`, the matching prefix is determined from the best routes table, then both best and backup routes for that prefix are returned if requested.

### Differential Mode

When run with two snapshots, the question computes a diff:

1. **Group routes**: Routes are grouped by primary key (node, VRF, prefix) and secondary key (next hop, protocol, and for BGP: received-from IP, path ID).

2. **Align attributes**: For routes with matching keys in both snapshots, attributes are compared.

3. **Report differences**: Each row indicates presence status (only in snapshot, only in reference, changed, unchanged).

The diff algorithm uses a two-pointer merge to efficiently align sorted attribute lists (see `alignRouteRowAttributes`).

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `RoutesQuestion` | Question definition with RIB type, filters, and match settings |
| `RoutesAnswerer` | Orchestrates answer generation; handles both single and differential modes |
| `RoutesAnswererUtil` | Route filtering, row conversion, and diff computation logic |
| `RouteRowKey` | Primary grouping key: hostname, VRF, prefix |
| `RouteRowSecondaryKey` | Secondary grouping key for diff (abstract base class) |
| `MainRibRouteRowSecondaryKey` | Secondary key for main RIB: next hop, protocol |
| `BgpRouteRowSecondaryKey` | Secondary key for BGP RIB: adds received-from IP, path ID |
| `EvpnRouteRowSecondaryKey` | Secondary key for EVPN RIB: adds route distinguisher, path ID |
| `RouteRowAttribute` | Non-key route attributes for diff comparison |
| `DiffRoutesOutput` | Container for diff results per route key |

## Output Schema

### Main RIB Columns

| Column | Type | Description |
|--------|------|-------------|
| `Node` | NODE | Device name |
| `VRF` | STRING | VRF name |
| `Network` | PREFIX | Route prefix (e.g., `10.0.0.0/24`) |
| `Next_Hop` | NEXT_HOP | Unified next-hop representation (see below) |
| `Next_Hop_IP` | IP | Legacy: next-hop IP address (null for connected/discard routes) |
| `Next_Hop_Interface` | STRING | Legacy: outgoing interface (null for recursive routes) |
| `Protocol` | STRING | Routing protocol: `bgp`, `ospf`, `connected`, `static`, `aggregate`, etc. |
| `Metric` | LONG | Protocol-specific metric (OSPF cost, BGP MED, etc.) |
| `Admin_Distance` | LONG | Administrative distance (0=connected, 20=eBGP, 110=OSPF, 200=iBGP, etc.) |
| `Tag` | LONG | Route tag (if set by policy) |

### Understanding Next-Hop Columns

The `Next_Hop` column is a unified representation that handles all next-hop types:

| Next-Hop Type | Example Value | When Used |
|---------------|---------------|-----------|
| `NextHopIp` | `ip 10.0.0.1` | Standard recursive route via IP |
| `NextHopInterface` | `interface eth0 ip 10.0.0.1` | Directly connected with specific next-hop |
| `NextHopInterface` | `interface eth0` | Connected route (no IP needed) |
| `NextHopDiscard` | `discard` | Null route / blackhole |
| `NextHopVrf` | `vrf CUSTOMER_A` | VRF route leaking |
| `NextHopVtep` | `vtep 192.168.1.1 vni 10001` | VXLAN tunnel endpoint |

**Why unified next-hop?** The legacy `Next_Hop_IP` and `Next_Hop_Interface` columns assume every route has either an IP next-hop or an interface — a model that fits traditional IP networks but breaks for:
- **VRF leaking**: Next-hop is a VRF name, not an IP
- **VXLAN/EVPN**: Next-hop is a VTEP with VNI
- **Connected routes**: No next-hop IP exists
- **Null routes**: Discard, not an IP or interface

The legacy columns remain for backward compatibility but are populated via `LegacyNextHops` extraction — they may be null or incomplete for modern route types. Prefer the `Next_Hop` column for new integrations.

### BGP RIB Additional Columns

| Column | Type | Description |
|--------|------|-------------|
| `Status` | LIST[STRING] | `BEST` (installed in main RIB) or `BACKUP` (kept for fast failover) |
| `AS_Path` | STRING | BGP AS path (empty for locally originated routes) |
| `Local_Pref` | LONG | BGP local preference (default 100, higher preferred) |
| `Communities` | LIST[STRING] | BGP communities (standard, extended, large) |
| `Origin_Protocol` | STRING | Protocol that injected the route into BGP (`network`, `redistribute`, etc.) |
| `Origin_Type` | STRING | `IGP` (network cmd), `EGP` (rare, legacy), `INCOMPLETE` (redistributed) |
| `Originator_Id` | STRING | BGP originator ID — set by route reflectors to prevent loops |
| `Received_From_IP` | IP | BGP neighbor that advertised this route (null for locally originated) |
| `Received_Path_Id` | INTEGER | BGP Add-Path path ID (for multiple paths to same prefix) |
| `Cluster_List` | SET[LONG] | Route reflector cluster IDs traversed |
| `Tunnel_Encapsulation_Attribute` | STRING | BGP tunnel encapsulation (for SR, VXLAN, etc.) |
| `Weight` | INTEGER | Cisco-style weight (local to router, higher preferred, default 0) |

**Note on Origin_Type**: You'll almost never see `EGP` — it's a legacy value from when BGP replaced EGP. Modern routes are either `IGP` (explicitly configured via `network` statement) or `INCOMPLETE` (redistributed from another protocol).

### EVPN RIB Additional Columns

Same as BGP RIB, plus:

| Column | Type | Description |
|--------|------|-------------|
| `Route_Distinguisher` | STRING | EVPN route distinguisher |

### Differential Mode Additional Columns

In differential mode, attribute columns are prefixed with `Base_` and `Delta_`, plus:

| Column | Type | Description |
|--------|------|-------------|
| `Entry_Presence` | STRING | `<In Snapshot>`, `<In Reference>`, `Changed`, or `Unchanged` |

## Performance Considerations

1. **Main RIB exact match optimization**: When `prefixMatchType=EXACT` and a specific network is provided, routes are retrieved directly from `FinalMainRib.getRoutes(prefix)` rather than scanning the entire RIB (batfish/batfish#8355).

2. **No deduplication**: Unlike ACL analysis, route tables are typically unique per device, so no cross-device deduplication is performed.

3. **Sorting for determinism**: Results are sorted for consistent output across runs, which adds minor overhead for large result sets.

## Known Limitations

1. **Best routes only in main RIB**: The main RIB contains only best routes selected from all protocols. To see backup BGP routes, use `rib: "bgp"` with `status: "backup"`.

2. **BGP-specific attributes lost in main RIB**: When BGP routes are installed in the main RIB, BGP-specific attributes (communities, AS path, etc.) are not preserved. Query the BGP RIB to see these attributes.

3. **Internal IPs hidden**: Next hop IPs used internally by Batfish (e.g., 169.254.0.1 for BGP unnumbered) are not displayed to avoid confusion.

4. **LPM for backup routes**: When using `LONGEST_PREFIX_MATCH` with BGP/EVPN RIBs, the matching prefix is determined from best routes. If users request only backup routes, they may get no results if the LPM-determined prefix has no backup routes.

## Common Sources of Confusion

### "Why is the AS path empty for locally originated routes?"

Routes originated via BGP `network` statements or redistribution start with an empty AS path. The AS path is prepended when the route is advertised to eBGP peers, but locally originated routes in the local RIB have no AS path.

### "Why does the main RIB show different metrics than BGP RIB?"

The main RIB's `Metric` column shows the metric used for route selection within Batfish's route selection logic. For BGP routes, this is typically the MED, but the column is generic across protocols. For BGP-specific metrics (local preference, weight, etc.), query the BGP RIB.

### "Why are some BGP routes missing from the main RIB?"

Only the best route for each prefix appears in the main RIB. If a BGP route loses to an OSPF route (lower admin distance), the BGP route exists in the BGP RIB but not the main RIB. Use `rib: "bgp"` to see all BGP routes regardless of whether they're installed.

### "Why don't I see BGP attributes (communities, AS path) in the main RIB?"

The main RIB is protocol-agnostic — it contains only common route attributes (prefix, next-hop, metric, admin distance). BGP-specific attributes are stored only in the BGP RIB. This matches real device behavior: `show ip route` shows less detail than `show bgp`.

### "Why is Next_Hop_IP null for my connected route?"

Connected routes have no next-hop IP — packets are delivered directly to the interface. Use the `Next_Hop` column which shows `interface <name>` for these routes. The `Next_Hop_IP` column only works for routes with IP next-hops.

### "Why does differential mode show so many changes?"

Common causes of route churn in diffs:
1. **Metric changes**: Protocol recalculation caused metric updates
2. **Next-hop changes**: Topology change shifted traffic to different paths
3. **Path ID changes**: BGP Add-Path assigned new path IDs

Use filters (`protocols`, `network`) to focus on routes you care about.

### "The routes don't match what I see on the real device"

Batfish computes routes from configuration — it simulates what *would* happen, not what *is* happening. Differences can arise from:
1. **External routes**: BGP routes from peers outside the snapshot
2. **Runtime state**: Interface flaps, neighbor resets not modeled
3. **Unsupported features**: Some redistribution or policy features may not be fully modeled

For BGP, ensure your snapshot includes external route announcements (via environment BGP announcements) to match production routing.

## Related Questions

- **`bgpRib`**: Alias for `routes` with `rib: "bgp"`. Uses the same `RoutesQuestion` implementation.
- **`evpnRib`**: Query EVPN routes specifically (uses `rib: "evpn"`).
- **`bgpEdges`**: Shows BGP session topology rather than route content.
- **`bgpSessionStatus`**: Shows BGP session state and configuration.
- **`traceroute`**: Uses the main RIB to trace packet paths through the network.

## Design Evolution

Key changes to the routes question over time:

- **batfish/batfish#4005**: Added EVPN RIB support
- **batfish/batfish#7150**: Added backup route support for BGP/EVPN RIBs
- **batfish/batfish#7714**: Added prefix match type options (EXACT, LPM, LONGER, SHORTER)
- **batfish/batfish#8355**: Performance optimization for exact prefix match queries
- **batfish/batfish#8430**: BGP RIB bug fixes and improved test coverage
- **Unified NextHop**: Migration from legacy `Next_Hop_IP`/`Next_Hop_Interface` columns to unified `Next_Hop` supporting VRF leaking, VXLAN, and other modern routing paradigms

## References

- [Route Analysis documentation](https://pybatfish.readthedocs.io/en/latest/notebooks/routingProtocols.html) — user-facing examples
- [2023 SIGCOMM paper](https://dl.acm.org/doi/10.1145/3603269.3604873) — Section 4.1 on data plane generation
- [Data Plane documentation](../data_plane/README.md) — how RIBs and FIBs are computed
