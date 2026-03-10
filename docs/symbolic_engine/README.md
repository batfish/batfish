# Symbolic Engine

Batfish's symbolic engine enables comprehensive network analysis by modeling packets as mathematical variables and using Binary Decision Diagrams (BDDs) to efficiently reason about all possible flows. This approach allows Batfish to provide correctness guarantees that traditional testing (with a limited set of example packets) cannot achieve.

## Table of Contents

- [Overview](#overview)
- [Use Cases](#use-cases)
  - [Search](#search)
  - [Network Behavior Comparison](#network-behavior-comparison)
  - [Intent Verification](#intent-verification)
  - [Change Validation](#change-validation)
  - [Fault Tolerance](#fault-tolerance)
- [BDD Fundamentals](#bdd-fundamentals)
  - [What are BDDs?](#what-are-bdds)
  - [Why BDDs for Network Analysis?](#why-bdds-for-network-analysis)
- [Symbolic Packet Representation](#symbolic-packet-representation)
  - [BDDPacket Structure](#bddpacket-structure)
  - [Packet Fields](#packet-fields)
  - [Packet Transformations](#packet-transformations)
- [Constraint Generation](#constraint-generation)
  - [Header Constraints](#header-constraints)
  - [ACL/Firewall Constraints](#aclfirewall-constraints)
  - [Routing Policy Constraints](#routing-policy-constraints)
- [Reachability Analysis](#reachability-analysis)
  - [Data Flow Analysis](#data-flow-analysis)
  - [Graph Construction](#graph-construction)
  - [State Expressions](#state-expressions)
  - [Fixpoint Computation](#fixpoint-computation)
  - [Analysis Types](#analysis-types)
- [NAT Encoding](#nat-encoding)
- [Implementation Architecture](#implementation-architecture)
  - [Graph Construction Process](#graph-construction-process)
  - [Analysis Execution](#analysis-execution)
  - [Solution Extraction](#solution-extraction)
- [Performance Optimization](#performance-optimization)
- [Common Patterns](#common-patterns)
- [Memory Management](#memory-management)
- [Troubleshooting](#troubleshooting)
- [Related Documentation](#related-documentation)

---

## Overview

### What is Symbolic Analysis?

Traditional network testing uses concrete packet examples:
```python
# Test one packet
test_packet = TCP(dst_ip="10.0.0.1", dst_port=80)
result = forward(test_packet)
```

**Problem**: You can only test a few examples. If a bug exists for packets you didn't test, you won't find it.

**Symbolic analysis** models packets as mathematical variables:
```python
# Test ALL packets matching constraints
constraint = TCP(dst_ip="10.0.0.1", dst_port=80)
result = find_all_flows_matching(constraint)
```

**Result**: Analysis guarantees for **all possible flows** matching the constraints.

### Key Benefits

1. **Comprehensiveness**: Analyzes all possible flows, not just examples
2. **Efficiency**: BDD operations are fast (polynomial time)
3. **Precision**: Exact results, no approximations
4. **Expressiveness**: Complex constraints on headers and paths

### Trade-offs

| Aspect | Concrete Testing | Symbolic Analysis |
|--------|-----------------|-------------------|
| **Coverage** | Limited to tested packets | All possible packets |
| **Time** | Fast per test | Slower (but one test covers all) |
| **Result** | Example results | Complete characterization |
| **Use case** | Quick verification | Correctness guarantees |

---

## Use Cases

### Search

Find flows that match specific structural and behavioral constraints.

#### Forwarding Behavior

**`reachability`**
- Search across all flows matching conditions
- Returns example flows that can reach destination
- Use cases:
  - Verify service accessibility
  - Test network segmentation
  - Validate firewall rules

**Example:**
```python
# Find all HTTPS flows from internet to web server
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10"),
        dst_ports=[443],
        protocols=["TCP"],
        locations="internet"
    )
)
# Returns: All flows from internet that can reach 10.0.1.10:443
```

**`bidirectionalReachability`**
- Two-pass analysis (forward + return)
- Sets up sessions after first pass
- Verifies return paths in presence of sessions
- Use case: Verify bidirectional connectivity

**`detectLoops`**
- Find all flows that will experience forwarding loops
- Returns example flows that loop
- Use case: Detect routing loops

**`loopbackMultipathConsistency`**
- Find flows between loopbacks treated differently by different paths
- Some paths drop, others forward
- Use case: Verify multipath consistency

**`subnetMultipathConsistency`**
- Find flows between subnets with inconsistent multipath treatment
- Use case: Verify ECMP consistency

#### Filters/ACLs

**`searchFilters`**
- Find flows where a filter takes specific action
- Actions: permit, deny
- Use cases:
  - Verify firewall rules
  - Test ACL effectiveness
  - Find unintended permit/deny

**Example:**
```python
# Find flows that firewall denies
result = bf.searchFilters(
    headers=HeaderConstraints(
        dst_ports=[22],
        protocols=["TCP"]
    ),
    action="deny",
    filters="firewall_in"
)
# Returns: All SSH flows that are denied by firewall_in
```

**`filterLineReachability`**
- Find unreachable lines in filters
- Returns rules that never match any flow
- Use case: Clean up unused ACL rules

#### BGP Routing Policies

**`searchRoutePolicies`**
- Find route announcements where policy has specific behavior
- Actions: accept, reject, modify
- Use cases:
  - Verify BGP policy
  - Test route maps
  - Find leaked routes

### Intent Verification

Use comprehensive search to verify network behavior matches intent.

**Example: Service Isolation**
```python
# Intent: Internal service isolated from internet
# Verification: No flow from internet can reach service
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.10.0/24")  # Internal service
    ),
    startLocation="internet",
    returnPermissionsResult=True  # Want flows that FAIL
)

if result.hasFlows():
    print("VIOLATION: Internet can reach internal service")
    print(f"Example violating flow: {result.flows()[0]}")
else:
    print("VERIFIED: Service is isolated from internet")
```

**Example: Redundancy**
```python
# Intent: Multiple paths to critical service
# Verification: At least 2 disjoint paths exist
paths = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.10")
    ),
    startLocation="datacenter1",
    constraints=PathConstraints(
        allowCrossBoxLinks=True  # Want multiple paths
    )
)

if len(paths.getDistinctPaths()) >= 2:
    print("VERIFIED: Redundancy exists")
else:
    print("VIOLATION: Single point of failure")
```

### Network Behavior Comparison

Compare two snapshots to find behavioral differences.

**`compareFilters`**
- Compare two filters (same filter, different versions)
- Find lines that match same flows but treat differently
- Use case: Safe refactoring of ACLs

**Example:**
```python
# Compare ACL before and after change
result = bf.compareFilters(
    filters="acl_outside",
    snapshot1="baseline",
    snapshot2="proposed_change"
)

# Returns: Pairs of lines that differ
for pair in result:
    print(f"Line {pair.line1} in v1 vs {pair.line2} in v2")
    print(f"  Match flows: {pair.flows}")
```

**`differentialReachability`**
- Compare reachability between snapshots
- Find flows that succeed in one but not the other
- Use cases:
  - Validate configuration changes
  - Detect regressions
  - Verify intended effects

**Example:**
```python
# Check what breaks after change
result = bf.differentialReachability(
    headerConstraints=HeaderConstraints(
        dst_ports=[80, 443],
        protocols=["TCP"]
    ),
    snapshot1="before",
    snapshot2="after"
)

# Returns flows that worked before but not after
for flow in result.onlyInSnapshot1:
    print(f"REGRESSION: {flow} no longer works")
```

**`searchFilters` (differential mode)**
- Compare filters in differential mode
- Find flows treated differently between versions
- Use case: Verify ACL changes

### Change Validation

Use differential questions to validate changes before deployment.

**Workflow:**
1. Create snapshot of current network
2. Make proposed changes in test network
3. Create snapshot of test network
4. Run differential analysis
5. Verify changes have intended effect + no collateral damage

**Example: Firewall Rule Change**
```python
# Want: Add permit for new service
# Risk: Accidentally permitting other traffic

baseline = bf.init_snapshot("current_network")
test = bf.init_snapshot("test_network")

# Check: New service is permitted
new_service = bf.reachability(
    headerConstraints=HeaderConstraints(dst_ports=[8080]),
    snapshot=test
)
assert new_service.hasFlows(), "New service not permitted"

# Check: Nothing else changed
diff = bf.differentialReachability(
    headerConstraints=HeaderConstraints(dst_ports=NEQ(8080)),  # Except new service
    snapshot1=baseline,
    snapshot2=test
)
assert not diff.hasFlows(), "Unintended side effects detected"
```

### Fault Tolerance

Compare behavior before and after simulated failures.

**Example: Link Failure**
```python
# Create baseline
normal = bf.init_snapshot("network")

# Simulate failure
bf.set_link_failure(node1="router1", interface1="eth0",
                   node2="router2", interface2="eth0")
failed = bf.init_snapshot("network_with_failure")

# Check impact
diff = bf.differentialReachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.1.0/24")
    ),
    snapshot1=normal,
    snapshot2=failed
)

if diff.onlyInSnapshot1:
    print(f"FAILURE IMPACT: {len(diff.onlyInSnapshot1)} flows broken")
```

---

## BDD Fundamentals

### What are BDDs?

**Binary Decision Diagram (BDD)** is a data structure for representing boolean functions.

**Example**: Represent function `f(x, y, z) = (x AND y) OR z`

```
BDD for f:
    z
   / \
  1   x     (when z=1, result is 1 regardless of x,y)
     / \
    y   0
   / \
  1   0
```

**Key properties:**
- **Canonical**: Same function → same BDD structure
- **Efficient operations**: AND, OR, NOT are fast
- **Compact**: Often exponentially smaller than truth table

### Why BDDs for Network Analysis?

1. **Packet headers are bit vectors**: Can encode as boolean variables
2. **Rules are boolean functions**: permit/deny = true/false
3. **Composition is natural**: Combining rules = logical operations
4. **Existential quantification**: "Is there a packet that...?" = ∃

**Example encoding:**
```
Packet: (src_ip, dst_ip, src_port, dst_port, protocol)
~     ~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
       └──> Encode as ~100 boolean variables

Rule: "permit TCP dst port 80"
~    ~   "permit dst_ip in 10.0.0.0/24 AND protocol=tcp AND dst_port=80"
       └──> Convert to BDD over packet variables
```

---

## Symbolic Packet Representation

### BDDPacket Structure

The [`BDDPacket`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/BDDPacket.java) represents a packet using BDDs:

```java
class BDDPacket {
    // Packet fields as BDDIntegers
    private BDDInteger _srcIp;
    private BDDInteger _dstIp;
    private BDDInteger _srcPort;
    private BDDInteger _dstPort;
    private BDDInteger _protocol;

    // Other fields
    private BDD _ipProtocol;  // ICMP, TCP, UDP, etc.
    private BDDInteger _icmpCode;
    private BDDInteger _icmpType;

    // Packet transformation history
    private List<Transformation> _transformations;
}
```

### Packet Fields

| Field | Type | Bits | Description |
|-------|------|------|-------------|
| `src_ip` | BDDInteger | 32 | Source IP address |
| `dst_ip` | BDDInteger | 32 | Destination IP address |
| `src_port` | BDDInteger | 16 | Source port |
| `dst_port` | BDDInteger | 16 | Destination port |
| `protocol` | BDDInteger | 8 | IP protocol number |
| `ip_protocol` | BDD | - | Encoded protocol (TCP/UDP/ICMP/etc) |
| `icmp_code` | BDDInteger | 8 | ICMP code |
| `icmp_type` | BDDInteger | 8 | ICMP type |

### Packet Transformations

Packets are transformed as they traverse the network:
- NAT: `src_ip`, `dst_ip`, `src_port`, `dst_port` modified
- Tunneling: New headers added
- Encapsulation: Original packet preserved

[`PrimedBDDInteger`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/PrimedBDDInteger.java) represents values that can be modified:

```java
// Original value
BDDInteger dstIp = packet.getDstIp();  // e.g., 10.0.0.1

// After NAT
PrimedBDDInteger primedDstIp = packet.getDstIp().primed();
primedDstIp.setValue(203, 0, 1);  // Set to 203.0.1.0

// Relationship: primed value depends on original value
// Constraint: (original = 10.0.0.1) → (primed = 203.0.1.0)
```

---

## Constraint Generation

### Header Constraints

[`HeaderSpaceToBDD`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/HeaderSpaceToBDD.java) converts user-specified header constraints to BDD:

**Input**: `HeaderConstraints` (from Pybatfish)
```python
HeaderConstraints(
    src_ips=ip_to_header_space("10.0.0.0/24"),
    dst_ips=ip_to_header_space("10.0.1.0/24"),
    src_ports=[22, 80, 443],
    dst_ports=[1024-65535],
    protocols=["TCP"],
    icmp_codes=[0],
    icmp_types=[8]
)
```

**Output**: BDD representing all matching packets

**Conversion process:**
1. **IP spaces**: Convert to BDD using [`IpSpaceToBDD`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/IpSpaceToBDD.java)
2. **Ports**: Create constraints on port BDDInteger
3. **Protocols**: Create constraints on protocol BDDInteger
4. **Combine**: AND all constraints together

**Example:**
```java
// User constraint: TCP, dst port 80
BDD protocolConstraint = packet.getIpProtocol().value(IpProtocol.TCP);
BDD dstPortConstraint = packet.getDstPort().value(80);

// Combined: All TCP packets with dst port 80
BDD combined = protocolConstraint.and(dstPortConstraint);
```

### ACL/Firewall Constraints

[`IpAccessListToBdd`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/IpAccessListToBdd.java) converts ACLs to BDD:

**Input**: `IpAccessList` (vendor-independent ACL)
```java
IpAccessList acl = ...;
List<IpAccessListLine> lines = acl.getLines();
```

**Each line** → BDD representing matching packets:
```java
for (IpAccessListLine line : lines) {
    // Convert line conditions to BDD
    BDD lineBdd = convertLineToBDD(line);

    if (line.getAction() == Action.PERMIT) {
        permitBdd = permitBdd.or(lineBdd);
    } else {
        denyBdd = denyBdd.or(lineBdd);
    }
}
```

**Result**: Two BDDs:
- `permitBdd`: All packets permitted by ACL
- `denyBdd`: All packets denied by ACL

### Routing Policy Constraints

Routing policies (route-maps, route lists) are converted similarly:

**Input**: `RoutingPolicy`
**Conversion**: Apply policy conditions to route attributes
**Output**: Modified route BDDs

---

## Reachability Analysis

### Data Flow Analysis

Reachability analysis is implemented as **data-flow analysis** using BDDs:

**Concept**:
- Build graph where nodes = forwarding stages, edges = packet flow
- Each edge labeled with BDD constraint
- Compute fixpoint: which packets can reach each stage

**Graph example**:
```
PreInInterface → [ingress ACL permits?] → PostInInterface
                                   ↓
                            NodeDropAclIn (denied)

PostInInterface → [VRF has route?] → PreOutVrf
                                ↓
                         NodeDropNoRoute (no route)

PreOutVrf → [egress ACL permits?] → VrfAccept
                              ↓
                       NodeDropAclOut (denied)
```

### Graph Construction

[`BDDReachabilityAnalysisFactory`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityAnalysisFactory.java) builds the reachability graph:

**Inputs:**
1. **Vendor-independent configurations**: All devices in network
2. **ForwardingAnalysis**: FIBs, ARP information
3. **Header constraints**: User-specified packet filtering
4. **Path constraints**: Locations, traversals, restrictions

**Process:**
1. Create state expressions for each forwarding stage
2. Create transitions between states
3. Label transitions with BDD constraints
4. Encode ACLs, FIB rules, NAT rules as BDDs

**Key state expressions:**
- `PreInInterface`: Before ingress ACL
- `PostInInterface`: After ingress ACL
- `PreOutVrf`: Before egress (in VRF)
- `PostOutVrf`: After egress (in VRF)
- `NodeAccept`: Packet accepted by device
- `NodeDrop*`: Various drop reasons
- `ExitsNetwork`: Packet leaves network

### State Expressions

Each state expression is a **BDD** representing the set of packets that can reach that state:

```java
// Initially, ingress locations have user-specified packets
Map<StateExpr, BDD> states = new HashMap<>();
states.put(preInInterface("eth0", "router1"), startPackets);

// Iteratively compute reachable packets
boolean changed = true;
while (changed) {
    changed = false;

    for (StateExpr state : states.keySet()) {
        BDD currentPackets = states.get(state);

        // Find all transitions from this state
        for (Transition transition : state.getTransitions()) {
            // Apply transition constraint
            BDD transitionPackets = currentPackets.and(transition.getGuard());

            // Add packets to destination state
            StateExpr destState = transition.getEndState();
            BDD destPackets = states.get(destState);
            BDD newDestPackets = destPackets.or(transitionPackets);

            if (!newDestPackets.equals(destPackets)) {
                states.put(destState, newDestPackets);
                changed = true;
            }
        }
    }
}
```

### Fixpoint Computation

[`BDDReachabilityUtils.fixpoint()`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/BDDReachabilityUtils.java) computes the fixpoint:

**Algorithm**: Worklist algorithm
1. Initialize worklist with start states
2. While worklist not empty:
   - Pop state
   - Compute forward image
   - Add to destination states
   - If changed, add destination to worklist
3. Stop when no more changes

**Convergence**: Guaranteed because:
- Number of states is finite
- Packet sets only grow (monotonic)
- BDDs have canonical form (easy equality check)

### Analysis Types

#### Backward Analysis (Default)

**More efficient** for most queries:
- Start from target (destination)
- Work backwards to sources
- Only explores relevant paths

**Use when**: Destination constraints are tighter than source

**Example:**
```python
# Want: Which sources can reach 10.0.1.10:80?
# Backward: Start at 10.0.1.10:80, trace back
```

#### Forward Analysis

**Use when**: Source constraints are very tight
- Start from sources
- Work forward to destinations
- Better when few sources, many destinations

**Example:**
```python
# Want: Where can packets from 10.0.1.5 go?
# Forward: Start at 10.0.1.5, trace forward
```

---

## NAT Encoding

NAT rules encode input-output relationships between packet fields:

**Example NAT rule:**
```
ip nat inside source static 10.0.0.5 203.0.1.5
```

**Encoding** using [`PrimedBDDInteger`](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/bdd/PrimedBDDInteger.java):

```java
// Original packet
BDDInteger srcIp = packet.getSrcIp();

// NAT'd packet (primed)
PrimedBDDInteger natSrcIp = srcIp.primed();

// Constraint: If srcIp = 10.0.0.5, then natSrcIp = 203.0.1.5
BDD precondition = srcIp.value(10, 0, 0, 5);
BDD postcondition = natSrcIp.value(203, 0, 1, 5);

// Combined: precondition → postcondition
BDD natConstraint = precondition.imp(postcondition);
```

[`TransformationToTransition`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/transition/TransformationToTransition.java) converts NAT transformations to graph transitions.

---

## Implementation Architecture

### Graph Construction Process

1. **BDDReachabilityAnalysisFactory** orchestrates construction:
   - Creates BDDPacket with BDD variables
   - Converts ACLs using `IpAccessListToBdd`
   - Converts header constraints using `HeaderSpaceToBDD`
   - Generates state nodes for each device/stage
   - Creates transitions with BDD guards

2. **BDDFibGenerator** encodes forwarding:
   - Takes ForwardingAnalysis results
   - Generates FIB lookup transitions
   - Handles ARP resolution
   - Encodes dispositions

3. **TransformationToTransition** encodes NAT:
   - Converts each NAT rule
   - Creates input-output constraints
   - Links pre-NAT and post-NAT states

### Analysis Execution

1. **Initialize**:
   ```java
   BDDPacket packet = new BDDPacket(factory);
   BDD startConstraints = ...; // From user input
   ```

2. **Build graph**:
   ```java
   BDDReachabilityAnalysis analysis = factory.buildAnalysis();
   ```

3. **Compute fixpoint**:
   ```java
   Map<StateExpr, BDD> reachableStates =
       BDDReachabilityUtils.fixpoint(analysis, startConstraints);
   ```

4. **Extract results**:
   ```java
   BDD result = reachableStates.get(targetState);
   ```

### Solution Extraction

[`BDDRepresentativePicker`](https://github.com/batfish/batfish/blob/master/projects/batfish/src/main/java/org/batfish/bddreachability/BDDRepresentativePicker.java) converts BDDs to concrete flows:

```java
// BDD represents set of all matching packets
BDD solutionSet = ...;

// Extract one example packet
BDD solution = solutionSet.satOne(); // One satisfying assignment

// Convert to concrete flow
Flow flow = BDDPacket.flowFromBDD(solution);
```

---

## Performance Optimization

### BDD Variable Ordering

Variable order significantly affects BDD size:
- **Good order**: Compact BDD, fast operations
- **Bad order**: Exponential blowup

**Batfish ordering** (from `BDDPacket`):
```
Variables: [protocol, src_ip, dst_ip, src_port, dst_port, ...]
Order:    Protocol first, then IPs, then ports
```

**Rationale**:
- Protocol separates traffic early
- IPs are most discriminating
- Ports refine within IP flows

### Caching

Batfish caches BDD computations:
- **ACL BDDs**: Convert once, reuse many times
- **FIB lookups**: Cache per-prefix results
- **Route policy BDDs**: Reuse across routes

### Early Pruning

Remove impossible flows early:
- If FIB lookup fails, stop processing
- If ACL denies, don't explore further
- Reduces state space for fixpoint

### Parallelization

Some operations are parallelizable:
- Graph construction per-device
- FIB generation
- ACL conversion (independent rules)

---

## Common Patterns

### Pattern 1: Basic Reachability

**Question**: Can host A reach host B on port 80?

```python
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        src_ips=ip_to_header_space("10.0.0.5"),
        dst_ips=ip_to_header_space("10.0.1.10"),
        dst_ports=[80],
        protocols=["TCP"]
    )
)

if result.hasFlows():
    print(f"Reachable. Example: {result.flows()[0]}")
else:
    print("Not reachable")
```

**Symbolic engine**:
1. Creates BDD for all TCP packets from 10.0.0.5 to 10.0.1.10:80
2. Computes fixpoint through network
3. Returns BDD of packets reaching destination
4. Extracts example flow from BDD

### Pattern 2: Service Isolation

**Question**: Can internet reach internal database?

```python
# Want no flows from internet to database
result = bf.reachability(
    headerConstraints=HeaderConstraints(
        dst_ips=ip_to_header_space("10.0.10.0/24")  # Database
    ),
    startLocation="internet",
    returnPermissionsResult=True  # Want failing flows
)

if result.hasFlows():
    print("VIOLATION: Database accessible from internet")
else:
    print("VERIFIED: Database isolated")
```

**Symbolic engine**:
- Computes ALL flows from internet to database subnet
- If any exist, returns example
- If none exist, returns empty

### Pattern 3: Find Allowed Traffic

**Question**: What traffic can pass this firewall?

```python
result = bf.searchFilters(
    headers=HeaderConstraints(
        src_ips=ip_to_header_space("0.0.0.0/0")  # Any source
    ),
    action="permit",
    filters="firewall_out"
)

for flow in result.flows():
    print(f"Allowed: {flow}")
```

**Symbolic engine**:
- Converts firewall to BDD (permit set)
- Finds all flows in permit set
- Returns examples

---

## Memory Management

**CRITICAL**: BDDs require careful memory management.

See [BDD Best Practices](../development/bdd_best_practices.md) for complete guide.

**Key points**:
1. Every BDD must be freed exactly once
2. Use `with` operations when consuming values
3. Use `id()` when you need copies
4. Test with `numOutstandingBDDs()` to catch leaks

---

## Troubleshooting

### Out of Memory

**Symptoms**: `java.lang.OutOfMemoryError`

**Causes**:
- Large network (many devices, many flows)
- Complex constraints (many header variables)
- BDD memory leaks (not freeing)

**Solutions**:
1. Increase heap: `-Xmx20g`
2. Simplify constraints
3. Check for BDD leaks
4. Reduce variable count

### Slow Analysis

**Symptoms**: Analysis takes > 10 minutes

**Causes**:
- Poor BDD variable ordering
- Large state space
- Many NAT transformations

**Solutions**:
1. Tighten constraints (reduce flow space)
2. Check BDD variable ordering
3. Enable caching
4. Use backward analysis (usually faster)

### No Results Expected

**Symptoms**: Analysis returns empty, but you expect results

**Debug**:
1. Check if start location is correct
2. Verify header constraints
3. Check if ACLs are permitting traffic
4. Verify routes exist (use `bf.get_routes()`)

**Example debug**:
```python
# Check if routes exist
routes = bf.get_routes(nodes="router1", prefix="10.0.1.0/24")
print(f"Routes: {routes}")

# Check if ACL permits
acl = bf.get_filters("firewall_in")
print(f"ACL: {acl}")

# Simplify constraints to isolate issue
```

---

## Related Documentation

- [BDD Best Practices](../development/bdd_best_practices.md): Critical guide for BDD memory management
- [Forwarding Analysis](../forwarding_analysis/README.md): How forwarding analysis works
- [Data Plane](../data_plane/README.md): How data plane is computed
- [Flow Dispositions](../flow_dispositions/README.md): All possible flow outcomes

---

## Summary

**Key concepts:**
1. **Symbolic analysis**: Models packets as BDD variables
2. **Comprehensiveness**: Analyzes ALL possible flows, not examples
3. **BDDs**: Efficient representation and operations on sets of packets
4. **Reachability**: Data-flow analysis over forwarding graph
5. **Fixpoint**: Iterative computation until convergence
6. **Memory management**: Critical for BDD operations

**When to use symbolic questions:**
- Need correctness guarantees
- Testing network policies
- Validating configuration changes
- Verifying intent (isolation, redundancy)
- Comparing network snapshots

**For BDD code development:**
- Always follow BDD best practices
- Test with `numOutstandingBDDs()`
- Use `with` operations to consume values
- Free every BDD exactly once
