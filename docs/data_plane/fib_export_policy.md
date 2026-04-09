# FIB Export Policy

## Overview

A FIB export policy filters routes exported from the main RIB into the FIB.
This applies to all routes regardless of source protocol.

- **Denied routes**: remain in the main RIB (visible for protocol
  advertisements and redistribution) but are excluded from the FIB
  (not used for packet forwarding).
- **Permitted routes**: installed in the FIB normally.

This differs from [BGP table-map](table_map.md), which filters at the
protocol-RIB-to-main-RIB boundary using `nonRouting`. FIB export operates
one level later — routes are in the main RIB but excluded from forwarding.

```
Protocol RIBs  --[table-map]--> Main RIB --[FIB export policy]--> FIB
                 (nonRouting)               (nonForwarding)
```

### Vendor Support

| Vendor | Syntax | Status in Batfish |
|--------|--------|-------------------|
| Juniper | `routing-options forwarding-table export <policy>` | Implemented (deny/permit) |

On Juniper, this policy also supports `then load-balance per-packet` for
ECMP selection. Batfish models all ECMP paths regardless, so the
per-packet/per-flow distinction has no behavioral effect. Standard route
attribute mutations (metric, next-hop, etc.) have no effect in this context
on real Juniper routers.

### Vendor-Independent Model

The VI model field is `Vrf.fibExportPolicy` — the name of a `RoutingPolicy`
to evaluate on each route during FIB construction.

### Data Plane

In `VirtualRouter.computeFib()`, if a FIB export policy is configured, it is
passed to `FibImpl` as a predicate. During FIB construction, each main RIB
route is evaluated against the policy. Denied routes are excluded from the
FIB.

Note: because the policy is applied at FIB construction time rather than at
main RIB insertion time, the `nonForwarding` flag on route objects in the
main RIB does not reflect FIB export policy denials. A route denied by the
FIB export policy will have `nonForwarding=false` in the main RIB but will
still be absent from the FIB. If we later need `nonForwarding` to be
authoritative on main RIB routes (e.g., for a routes question column), the
implementation should be changed to stamp the flag on routes as they enter
the main RIB instead.

---

## Juniper Implementation

### Syntax

```
routing-options {
    forwarding-table {
        export <policy-name>;
    }
}
```

### Pipeline

```
Config           "forwarding-table export FIB_FILTER"
    |
Parsing          FlatJuniper_routing_instances.g4: rof_export rule
    |
Extraction       ConfigurationBuilder.exitRof_export()
                 -> RoutingInstance._forwardingTableExportPolicy
    |
Conversion       JuniperConfiguration.convertForwardingTableExport()
                 -> Vrf.fibExportPolicy
```

### Key Files

| Layer | File | What |
|-------|------|------|
| VS model | `representation/juniper/RoutingInstance.java` | `_forwardingTableExportPolicy` field |
| Extraction | `grammar/flatjuniper/ConfigurationBuilder.java` | `exitRof_export()` |
| Conversion | `representation/juniper/JuniperConfiguration.java` | `convertForwardingTableExport()` |
| Data plane | `dataplane/ibdp/VirtualRouter.java` | `computeFib()` |
| FIB | `datamodel/FibImpl.java` | `fibExportFilter` parameter |
