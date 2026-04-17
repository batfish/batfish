# EVPN-VXLAN Support in Batfish

Batfish models EVPN-VXLAN fabrics — including EVPN Type 5 IP prefix routes, VRF
route leaking, route target communities, import/export policies, and Layer 3
VXLAN forwarding through VTEP tunnels. This article demonstrates these
capabilities using a Juniper ERB (Edge-Routed Bridging) fabric.

## Example Network

The demonstration network is a four-node Juniper EVPN-VXLAN fabric. It redistributes a CE route (192.168.99.0/24) into the overlay as an EVPN Type 5 route.

```
                ┌─────────┐
                │  edge1  │   AS 65200
                │ (CE)    │   192.168.99.0/24
                └────┬────┘
                     │  eBGP (TENANT-A VRF)
                ┌────┴────┐
                │ router1 │   AS 65100 / 65000 (overlay)
                │ (PE/GW) │   VTEP: 172.16.0.100
                └────┬────┘
                     │  eBGP underlay + iBGP overlay
                ┌────┴────┐
                │ node2-1 │   AS 65011 / 65000 (overlay)
                │ (Spine) │   Route Reflector
                └────┬────┘
                     │  eBGP underlay + iBGP overlay
                ┌────┴────┐
                │ node1-1 │   AS 65001 / 65000 (overlay)
                │ (Leaf)  │   VTEP: 172.16.0.1
                └─────────┘
```

- **edge1** is a CE device advertising `192.168.99.0/24` via eBGP into
  router1's TENANT-A VRF.
- **router1** is a PE/gateway that receives the eBGP route and redistributes
  tenant VRF prefixes as EVPN Type 5 routes into the overlay.
- **node2-1** is a spine acting as an iBGP EVPN route reflector.
- **node1-1** is a leaf that imports EVPN Type 5 routes into its TENANT-A VRF.

Both router1 and node1-1 have IRB interfaces in TENANT-A (irb.100, irb.200) and
TENANT-B (irb.300, irb.400), with VLANs mapped to VNIs for Layer 2 VXLAN.

---

## EVPN Type 5 Route Propagation

An EVPN Type 5 (IP Prefix) route carries a Layer 3 VRF prefix across the VXLAN
overlay. In the fabric above, the flow is:

1. **edge1** advertises `192.168.99.0/24` via eBGP to router1's TENANT-A VRF.
2. **router1** redistributes the prefix into EVPN as a Type 5 route, tagging it
   with route target `target:65000:10000` and route distinguisher
   `172.16.0.100:10000`.
3. **node2-1** (the route reflector) receives and reflects the EVPN route.
4. **node1-1** imports the EVPN route into its TENANT-A VRF.

After computing the data plane, the EVPN RIB on node2-1 contains the Type 5
route:

```
Node:    node2-1
VRF:     default (EVPN RIB)
Prefix:  192.168.99.0/24
Type:    EVPN Type 5
```

And node1-1's TENANT-A main RIB contains the imported prefix:

```
Node:    node1-1
VRF:     TENANT-A
Prefix:  192.168.99.0/24
Protocol: iBGP
Admin:   170
```

The route appears in node1-1's VRF as an iBGP route with admin distance 170,
which is the standard distance for routes learned via EVPN Type 5 import. This
matches what you would see on a live Junos device under
`show route table TENANT-A.inet.0`.

---

## VRF Routing Instance Configuration

Each VTEP configures tenant VRFs as Junos routing instances with
`instance-type vrf`. The key EVPN-specific settings are under `protocols evpn
ip-prefix-routes`:

```
routing-instances {
    TENANT-A {
        instance-type vrf;
        protocols {
            evpn {
                ip-prefix-routes {
                    advertise direct-nexthop;
                    encapsulation vxlan;
                    vni 50000;
                }
            }
        }
        route-distinguisher 172.16.0.100:10000;
        vrf-target target:65000:10000;
    }
}
```

Batfish requires the `vni`, `advertise`, and `encapsulation` fields to create
the IP-VRF. If any are missing, a warning is raised and the VRF is not
configured for EVPN leaking. The `vni` here (50000) is the Layer 3 VNI used for
VXLAN encapsulation on the symmetric IRB data plane — distinct from the per-VLAN
Layer 2 VNIs (like 10100 for VLAN100) used for bridged traffic.

The `vtep-source-interface` under `switch-options` identifies the loopback whose
IP becomes the VTEP address for tunnel endpoints:

```
switch-options {
    vtep-source-interface lo0.0;
}
```

---

## Route Targets and Import/Export Policies

Route targets control which VRFs exchange routes. The simplest form uses
`vrf-target`, which sets both the import and export community:

```
vrf-target target:65000:10000;
```

When a route is exported from this VRF, it is tagged with `target:65000:10000`.
When importing, only EVPN routes carrying a matching route target are accepted.

### Separate Import and Export Targets

Import and export targets can be set independently:

```
vrf-target import target:65000:10000;
vrf-target export target:65000:20000;
```

### Explicit Import/Export Policies

For finer-grained control, `vrf-import` and `vrf-export` policies can be used
alongside or instead of simple RT matching:

```
routing-instances {
    TENANT-A {
        vrf-import TENANT-A-import;
        vrf-export TENANT-A-export;
    }
}
```

When `vrf-import` is set, it replaces simple RT-based import filtering — the
policy itself must match on the route target community to accept routes. This
lets a single VRF import from multiple tenants:

```
policy-statement TENANT-A-import {
    term from-tenant-a {
        from community TENANT-A-RT;    /* target:65000:10000 */
        then accept;
    }
    term from-tenant-b {
        from community TENANT-B-RT;    /* target:65000:20000 */
        then accept;
    }
    term default {
        then reject;
    }
}
```

### IP-Prefix-Routes Import/Export Policies

The `ip-prefix-routes` block can also carry its own import and export policies:

```
protocols {
    evpn {
        ip-prefix-routes {
            import TENANT-A-ipr-import;
            vni 50000;
            advertise direct-nexthop;
            encapsulation vxlan;
        }
    }
}
```

When both `vrf-import` and `ip-prefix-routes import` are present, they are
evaluated conjunctively — a route must pass both policies to be accepted. The
ip-prefix-routes import policy can set route attributes like tags:

```
policy-statement TENANT-A-ipr-import {
    term 1 {
        then {
            tag 999;
            accept;
        }
    }
}
```

In this testrig, node1-1 has this policy. After data plane computation,
the imported 192.168.99.0/24 route in node1-1's TENANT-A VRF carries `tag 999`,
confirming the ip-prefix-routes import policy was evaluated.

---

## VRF Export and Route Filtering

The `vrf-export` policy controls which routes from a tenant VRF are
redistributed into the EVPN overlay. This is useful for blocking specific
prefixes or attaching communities during export.

In the test fabric, router1 has:

```
vrf-export TENANT-A-vrf-export-redist;
```

This policy rejects `10.10.10.0/24` (a static route in the VRF) and attaches a
`gateway-community` to all other routes:

```
policy-statement TENANT-A-vrf-export-redist {
    term reject-blocked {
        from {
            route-filter 10.10.10.0/24 exact;
        }
        then reject;
    }
    term accept-rest {
        then {
            community add gateway-community;
            accept;
        }
    }
}
```

After data plane computation:

- `10.10.10.0/24` **is present** in router1's TENANT-A VRF (it's a
  local static route)
- `10.10.10.0/24` **is absent** from node1-1's TENANT-A VRF (the
  vrf-export policy rejected it)
- `192.168.99.0/24` **is present** in node1-1's TENANT-A VRF (the
  vrf-export policy accepted it)
- EVPN routes for connected prefixes like `172.16.100.0/24` carry the
  `gateway-community` (`target:65000:99`) set by the vrf-export policy

This demonstrates that Batfish correctly evaluates `vrf-export` during route
redistribution into the EVPN overlay.

---

## Self-Import Loop Prevention

A subtle issue arises when a VRF contains routes learned via eBGP (admin
distance 170). When the VRF exports these routes as EVPN Type 5 and the same
device's VRF attempts to re-import them, the EVPN route (also admin distance
170) could replace the original, triggering a withdrawal loop.

Batfish prevents this by filtering EVPN routes whose route distinguisher matches
the importing VRF's own RD during cross-VRF import. In the test fabric,
router1's TENANT-A uses RD `172.16.0.100:10000`. When its own EVPN Type 5
routes appear in the default-VRF EVPN RIB, they are not re-imported into
TENANT-A because the RDs match.

This ensures the eBGP-learned `192.168.99.0/24` in router1's TENANT-A remains
stable — matching the behavior observed on live Junos devices.

---

## Traceroute Through VXLAN Tunnels

Batfish traceroute models the Layer 3 forwarding path through VXLAN tunnels.
When a route's next hop is a remote VTEP, the trace shows the packet being
forwarded into a VXLAN tunnel with the L3 VNI and remote VTEP IP.

A traceroute from node1-1 (irb.100 in TENANT-A) to edge1's network:

```python
bf.q.traceroute(
    startLocation="@enter(node1-1[irb.100])",
    headers=HeaderConstraints(
        srcIps="172.16.100.10",
        dstIps="192.168.99.10"
    )
).answer()
```

The trace hops are:

```
Hop 1: node1-1
  RECEIVED    on irb.100 (VRF: TENANT-A)
  FORWARDED   into VXLAN tunnel with VNI: 50000 and VTEP: 172.16.0.100
  TRANSMITTED on nve~50000

Hop 2: router1
  RECEIVED    on nve~50000 (VRF: TENANT-A)
  FORWARDED   out ge-0/0/3.0 with resolved next-hop IP: 10.99.0.1
  TRANSMITTED on ge-0/0/3.0

Hop 3: edge1
  RECEIVED    on ge-0/0/1.0
  FORWARDED   out ge-0/0/2.0
  DELIVERED_TO_SUBNET via ge-0/0/2.0
```

Key details visible in the trace:

- **`Forwarded into VXLAN tunnel`**: The FORWARDED step on node1-1 shows the
  VNI and remote VTEP IP. This is how Batfish represents a packet entering the
  VXLAN overlay.
- **`nve~50000`**: The virtual tunnel interface name encodes the L3 VNI. The
  packet exits the source VTEP on this interface and arrives at the remote VTEP
  on the same interface name.
- **VRF context**: The packet enters node1-1 in VRF TENANT-A and exits the
  tunnel at router1 still in VRF TENANT-A — the L3 VNI maps the tunnel to the
  correct tenant VRF on both sides.
- **Route used**: The forwarding step references the iBGP route to
  `192.168.99.0/24` with `nextHop: vtep` — confirming the EVPN-imported route
  drives the forwarding decision.

In production networks with multiple VRFs, different VNIs are used for different
tenants. A trace in VRF "Lab-DMZ" might use VNI 2005, while VRF "Lab" uses VNI
2000 — each tunnel carrying traffic for a separate routing domain across the
shared underlay.

---

## Batfish Questions

### Routes

Use `bf.q.routes()` to inspect the main RIB of each VRF. The EVPN Type 5
imported routes appear alongside connected and static routes:

```python
bf.q.routes().answer()
```

| Node    | VRF      | Network            | Protocol  | Admin | Next Hop              |
|---------|----------|--------------------|-----------|-------|-----------------------|
| node1-1 | TENANT-A | 172.16.100.0/24    | connected | 0     | irb.100               |
| node1-1 | TENANT-A | 172.16.200.0/24    | connected | 0     | irb.200               |
| node1-1 | TENANT-A | 192.168.99.0/24    | ibgp      | 170   | vni 50000 vtep 172.16.0.100 |
| router1 | TENANT-A | 192.168.99.0/24    | bgp       | 170   | 10.99.0.1 (ge-0/0/3.0)|
| router1 | TENANT-A | 10.10.10.0/24      | static    | 5     | discard               |

The EVPN-imported route on node1-1 has a VTEP next hop — the packet will be
VXLAN-encapsulated to reach it. The same prefix on router1 uses a normal IP next
hop since it was learned via eBGP from edge1.

### EVPN RIB

Use `bf.q.evpnRib()` to inspect the EVPN RIB separately. This shows Type 5
routes with their route distinguisher, route targets, and originator:

```python
bf.q.evpnRib().answer()
```

| Node    | VRF     | Prefix          | RD                   | Route Targets        |
|---------|---------|-----------------|----------------------|----------------------|
| node2-1 | default | 192.168.99.0/24 | 172.16.0.100:10000   | target:65000:10000   |
| node1-1 | default | 192.168.99.0/24 | 172.16.0.100:10000   | target:65000:10000   |

---

## Junos Configuration Reference

The following Junos configuration constructs are supported for EVPN-VXLAN:

| Configuration                                                     | Purpose                                      |
|-------------------------------------------------------------------|----------------------------------------------|
| `routing-instances <name> instance-type vrf`                      | Defines a tenant VRF                         |
| `routing-instances <name> route-distinguisher`                    | RD for EVPN route identification             |
| `routing-instances <name> vrf-target`                             | Sets import and export route target          |
| `routing-instances <name> vrf-target import`                      | Sets import RT only                          |
| `routing-instances <name> vrf-target export`                      | Sets export RT only                          |
| `routing-instances <name> vrf-import`                             | Explicit import policy (replaces RT matching)|
| `routing-instances <name> vrf-export`                             | Explicit export/redistribution policy        |
| `routing-instances <name> protocols evpn ip-prefix-routes vni`    | L3 VNI for Type 5 encapsulation              |
| `routing-instances <name> protocols evpn ip-prefix-routes advertise` | Enables prefix advertisement              |
| `routing-instances <name> protocols evpn ip-prefix-routes encapsulation` | VXLAN encapsulation type              |
| `routing-instances <name> protocols evpn ip-prefix-routes import` | Per-IPR import policy                        |
| `routing-instances <name> protocols evpn ip-prefix-routes export` | Per-IPR export policy                        |
| `switch-options vtep-source-interface`                            | Loopback for VTEP source IP                  |
| `vlans <name> vxlan vni`                                          | Maps VLAN to L2 VNI                          |
| `protocols bgp group <name> family evpn signaling`                | Enables EVPN address family on BGP peers     |
