# BGP Table-Map

## Overview

BGP table-map applies a routing policy to BGP routes before they are installed
into the main RIB. This controls which BGP routes are used for forwarding.

- **Denied routes**: remain in the BGP RIB (advertised to peers) but are not
  installed in the main RIB (not used for forwarding).
- **Permitted routes**: installed in the main RIB normally. Depending on the
  vendor, the router may also modify route attributes (e.g., metric,
  next-hop) before installation; Batfish does not yet model this.

Table-map uses the `nonRouting` flag to exclude denied routes from the main
RIB. See also [FIB export policy](fib_export_policy.md), which filters at
the RIB-to-FIB boundary using `nonForwarding`.

### Vendor Support

| Vendor | Syntax | Status in Batfish |
|--------|--------|-------------------|
| FRR | `table-map <route-map>` | Implemented (deny/permit) |
| Cisco NX-OS | `table-map <route-map> [filter]` | Parsed, not converted |
| Cisco IOS/IOS-XR | `table-map <route-map>` | Recognized, ignored |

### Vendor-Independent Model

The VI model field is `BgpProcess.tableMapPolicy` — the name of a
`RoutingPolicy` to evaluate on each BGP route during the unstage step.

### Data Plane

In `BgpRoutingProcess.unstage()`, if a table-map policy is configured, each
route advertisement is evaluated against the policy:
- Withdrawals pass through unchanged.
- Announcements permitted by the policy are installed normally.
- Announcements denied by the policy are rebuilt with `nonRouting=true`.

---

## FRR Implementation

### Syntax

```
router bgp <asn>
  table-map <route-map-name>
```

In FRR, the route-map is applied to a *copy* of the route attributes. Only
**metric** and **next-hop** are read from the modified copy and used for the
installed route. All other `set` clause effects are discarded.

### Pipeline

```
Config           "table-map DENY_ALL"
    |
Parsing          Frr_bgp.g4: rb_table_map rule
    |
Extraction       FrrConfigurationBuilder.exitRb_table_map()
                 -> BgpVrf._tableMap
    |
Conversion       FrrConversions.convertBgpTableMap()
                 -> BgpProcess.tableMapPolicy
                 -> warnings for set clauses
```

### Key Files

| Layer | File | What |
|-------|------|------|
| VS model | `representation/frr/BgpVrf.java` | `_tableMap` field |
| Extraction | `grammar/frr/FrrConfigurationBuilder.java` | `exitRb_table_map()` |
| Conversion | `representation/frr/FrrConversions.java` | `convertBgpTableMap()` |

### Conversion Warnings

During conversion, warnings are emitted for `set` clauses in table-map
route-map permit entries:

- **Metric / next-hop**: redFlag — valid on the router but not yet modeled.
- **All other set clauses**: RISKY — no-ops even on the real router in
  table-map context.

### Reference

- FRR docs: https://docs.frrouting.org/en/latest/bgp.html#clicmd-table-map-ROUTE-MAP-NAME
