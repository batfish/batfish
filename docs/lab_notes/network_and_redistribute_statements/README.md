## Vendor-specific BGP network and redistribute behavior lab notes

* Author: Ari Fogel
* Last updated: 2022-09-21

## Objective

Determine behavior across vendors of BGP `network` and `redistribute` statements that match routes
for a given network:

* attributes
* whether multiple routes for the same network can be introduced into the BGP RIB
    * By a network statement
    * By a redistribute statement
    * By a combination of network and redistribute statements

## Background

Most (all?) devices that export from the BGP RIB support two methods of introducing non-BGP routes
from the main RIB into the BGP RIB:

* `network` statements
* `redistribute` statements

TODO: more?

## Procedure

On each device, instantiate a bgp process (with neighbor if necessary) that redistributes static
routes for network 10.10.0.0/24 via both network and redistribute commands.

Each command should take a route-map that can differentiate between distinct ECMP static routes, and
set communities differently for distinct routes. We accomplish this by applying distinct tags to the
static routes, and then setting a standard community based on the tag. We set distinct communities
for network statements and for redistribute statements. In cases where it appears you can get a
route both from network and redistribute statements, the route-maps should also guarantee that the
bgp route entries are all equally preferred. For this, we explicitly set the same origin type,
weight, metric, and local preference.

## Results

### A10

* There is no dependency between redistribute and network statements
    * You can get up to one route each from network and redistribute
* network
    * **Without a route-map, a network statement unconditionally creates a bgp rib entry for the
      network**
        * **This happens even if the device has no route for the network**
    * Only the lowest NHIP route for a prefix is matched against the route-map
* redistribute
    * All routes for a prefix are matched against the route-map
    * **Regardless of which route for a prefix matches, the lowest NHIP for the prefix is the NHIP
      of the resulting BGP RIB entry (even if the route with lowest NHIP is denied)**
        * **However, the actions taken are those of the permitting route-map clause on the route
          that is actually permitted**
    * resulting origin is INCOMPLETE

### EOS

* Only one route per prefix is ever a candidate for redistribution via either network or
  redistribute commands
    * Limited testing shows that it is always the route for a given prefix with the highest
      next-hop-ip
        * **In Batfish, we should only run the route with the highest nhip for a prefix through
          redistribution policy**
* The route is run first through redistribute policy.
    * If it is permitted by redistribute policy, then it is not run through network policy
    * If it is denied by redistribute policy, then it is run through network policy.
    * **In Batfish, we should do one of the following:**
        * **concatenate generated network policy at the end of generated redistribute policy**
        * **Have a list of redistribution policies (redistribute first, then network), and only
          match the first**
            * **This will guarantee we do not accidentally apply intermediate modifications from
              redistribute policy if it ends up denying**
* Default origin type is the same for network and redistribute, but it varies by source protocol
    * Connected -> IGP
    * Static -> incomplete
    * OSPF -> ?
    * IS-IS -> ?

### F5

TODO

### FRR (Cumulus)

* There is no dependency between redistribute and network statements
    * You can get up to one route each from network and redistribute
* network
    * The route to be matched has all ECMP-identifying attributes cleared
        * match tag fails
            * tag is cleared before running through policy
        * match source-protocol succeeds
        * match ip next-hop succeeds only if matching 0.0.0.0
            * nh is set to 0.0.0.0 before running through policy
    * resulting origin is IGP
* redistribute
    * The route to be matched has some properties preserved for both match and transformed bgp rib
      entry
        * match tag succeeds
            * tag ends up in bgp rib entry
    * Only the highest NHIP route for a prefix is matched against route-map
    * **Regardless of which route for a prefix matches,the NHIP of the resulting BGP RIB entry will
      be the lowest NHIP of any route for that prefix (even though in general the route with the
      lowest NHIP is not a candidate).**
        * **However, all other properties of the highest NHIP route end up in the BGP rib entry,
          e.g. tag**
    * resulting origin is INCOMPLETE

### IOS

* Only one route per prefix is ever a candidate for redistribution via either network or
  redistribute commands
    * Limited testing shows that it is always the route for a given prefix with the highest
      next-hop-ip
        * **In Batfish, we should only run the route with the highest nhip for a prefix through
          redistribution policy**
* The existence of a network statement causes all routes for that network to not be run through
  redistribute commands.
    * **In Batfish, we should filter out all network statement networks from policy generated for
      redistribute commands**

### IOS-XR

* All routes for a given prefix are run through redistribute/network route-policy
    * The order _appears_ to be from lowest NHIP to highest NHIP
    * The first route to pass is the one whose NHIP is used
    * All actions from all routes for the prefix are applied
        * if any route for the prefix is dropped, then no route for the prefix is used
    * Only one route ends up being redistributed, and its properties are a composite from multiple
      runs of the policy as described above
        * This is similar to what is done for aggregate-address on XR
* First, routes for a prefix go through network route-policy
    * If network route-policy passes **all** routes for the prefix, then routes for the prefix
      are **not** passed through redistribute policy
    * Otherwise, routes for that prefix are passed through redistribute route-policy
* This behavior is complex, and supporting it properly in Batfish will require significant changes
  to the BGP pipeline
    * Suggest as first pass just running lowest NHIP route for each prefix first through network
      route--policy, and then through redistribute policy if the route is dropped by network
      route-policy

### NXOS

* There is no dependency between redistribute and network statements
    * You can get up to one route each from network and redistribute
* network routes are preferred to redistribute routes
    * bgp data shows that even when all settable properties are the same, paths with path-type
      local (network) are preferred over paths with path-type redist (redistribute)
* redistribute
    * **Only the route for a prefix with the highest NHIP is matched against redistribute
      route-map**
    * The NHIP of the resulting BGP rib entry is always 0.0.0.0
    * The default resulting origin type is **INCOMPLETE**
* network
    * **All routes for a prefix are matched against the network route-map**
        * However, only one may become a BGP rib entry
            * If a lower NHIP route is denied, then a higher NHIP route may still be permitted
        * The passing route with the **lowest NHIP** will be the one installed into the BGP RIB
    * The NHIP of the resulting BGP rib entry is always 0.0.0.0
    * The default resulting origin type is **IGP**

## Batfish behavior

Every device type has different behavior for bgp `network` and `redistribute` statements.

Lab-discovered behavior to support:

- [x] A `redistribute` or `network` statement will only ever introduce at most one route into bgp
  for a given prefix
- [ ] Depending on device type, a `route-map` or `route-policy` for such a statement may be applied
  to only the route for a prefix with the highest NHIP, or the lowest NHIP, or all routes for that
  prefix
- [ ] The NHIP that goes into the BGP RIB entry may not be the NHIP of the route applied against the
  policy
- [ ] Some devices can get one BGP RIB entry from a `network` statement and an additional BGP RIB
  entry from a `redistribute` statement, independently.
  Devices types:
    - [ ] A10
    - [ ] FRR
    - [x] NXOS: batfish/batfish#7660
- [X] On NXOS, a BGP RIB entry introduced by a `network` statement is preferred (not
  ECMP-equivalent) over an entry introduced by a `redistribute` statement when all current
  Batfish-supported attributes are set equal by respective route-maps (tie-breaking priority at
  least higher than origin type). We may need a new attribute for this.
    - batfish/batfish#7690
- [ ] NHIP is almost always preserved from IGP route via `redistribute` statement, but always
  cleared via `network` statement. This matters for iBGP, and also affects IGP cost to next hop
- [ ] On A10, a `network` statement without a route-map unconditionally introduces a BGP RIB entry
  for the given prefix, regardless of whether there is any route in the main RIB with that prefix
- [ ] Some devices will not install local BGP RIB entries unless there is at least one active BGP
  session
- [ ] On IOS, the presence of a `network` statement for a prefix prevents all routes for that prefix
  from being run through `redistribute` statements, whether or not any such routes match
  the `network` route-map. Easily solvable by adding filter at top of policy generated
  for `redistribute` statements.
