# Post-processing

Post-processing is a critical stage in the Batfish pipeline that refines and finalizes the vendor-independent configuration model after conversion from vendor-specific formats. This stage ensures that configurations are complete, consistent, and ready for data plane computation and analysis.

## Why Post-processing is Needed

After vendor-specific configurations are converted to the vendor-independent model, several important attributes and relationships are not yet fully resolved. Post-processing addresses these gaps by:

1. **Handling Interface Dependencies**: Interfaces often depend on other interfaces (e.g., VLAN interfaces depend on physical interfaces, child interfaces depend on aggregate interfaces). These dependencies must be resolved to determine which interfaces can be active.

2. **Computing Derived Values**: Many configuration values are derived from other values or interface state. For example:
   - Aggregate interface bandwidth is the sum of its member bandwidths
   - IGP costs are computed from interface bandwidth when not explicitly configured
   - Redundant interface bandwidth needs special handling

3. **Applying Runtime Constraints**: User-provided blacklists, topology information, and other runtime data must be applied to determine the actual active network state.

4. **Initializing Protocol State**: Routing protocols like OSPF and EIGRP need initialization of neighbor relationships and cost values.

## Input

Post-processing operates on the following inputs:

- **Vendor-independent (VI) configuration objects**: Java objects representing network device configurations after conversion from vendor-specific formats
- **Runtime data**: User-provided data including:
  - Node blacklists (devices to exclude)
  - Interface blacklists (interfaces to exclude)
  - Layer 1 topology information (physical connections)
  - Synthesized topology information

## Output

Post-processing produces modified vendor-independent configuration objects with the following changes:

### Interface State Changes
- **Physical interfaces and interfaces depending on them may be deactivated** when:
  - Interface is admin down
  - Interface is blacklisted
  - Interface dependencies are unmet (e.g., VLAN interface with no active physical members)
  - Node is blacklisted

### Derived Values Computed
- **Bandwidth/speed is updated**:
  - Aggregate interfaces (e.g., port-channels): bandwidth = sum of member bandwidths
  - Redundant interfaces (e.g., HSRP/VRRP groups): bandwidth = maximum member bandwidth
- **IGP costs are initialized**:
  - OSPF interface costs computed from bandwidth when not explicitly configured
  - EIGRP metric values (bandwidth component) computed from interface bandwidth
- **Additional IP addresses may be configured** for protocol requirements

### Protocol Initialization
- OSPF neighbor configurations initialized
- EIGRP neighbor configurations initialized

### Metadata
- **Autocomplete metadata** for UI features (node names, interface names, prefixes, etc.)

## Step-by-Step Process

The post-processing stage, implemented in `Batfish.postProcessSnapshot()`, executes the following steps in order:

### Step 1: Apply Blacklists

```java
// Apply node blacklist (disable all interfaces on blacklisted nodes)
processNodeBlacklist(blacklistedNodes, configurations);

// Apply interface blacklist (disable specific interfaces)
processInterfaceBlacklist(interfaceBlacklist, configurations);
```

Blacklists are applied first to ensure that blacklisted nodes/interfaces remain inactive regardless of other processing.

### Step 2: Process Admin-Down Interfaces

```java
disconnectAdminDownInterfaces(configurations);
```

Interfaces that are administratively down are marked. Currently, this is a no-op pending a decision on correlating admin and line status.

### Step 3: Process Management Interfaces (Optional)

```java
if (_settings.ignoreManagementInterfaces()) {
    processManagementInterfaces(configurations);
}
```

If configured, management interfaces are identified and can be handled specially (e.g., excluded from certain analyses).

### Step 4: Initialize Topology

```java
Layer1Topologies l1Topologies = Layer1TopologiesFactory.create(
    rawLayer1PhysicalTopology,
    synthesizedLayer1Topology,
    configurations);
```

Layer 1 topology information is assembled from raw topology files and synthesized topology, providing the physical connection context needed for interface dependency resolution.

### Step 5: Process Interface Dependencies

```java
postProcessInterfaceDependencies(configurations, l1Topologies);
```

This critical step deactivates interfaces that cannot be operational based on their dependencies:

- **VLAN interfaces**: Deactivated if all member physical interfaces are inactive
- **Physical interfaces**: Deactivated if:
  - Not included in Layer 1 topology (no detected connections)
  - Explicitly marked as inactive
  - Dependency chain is broken

The method uses `getInterfacesToDeactivate()` to identify interfaces that should be deactivated based on the current state and topology.

### Step 6: Disable Unusable VLAN Interfaces

```java
disableUnusableVlanInterfaces(configurations);
```

An additional pass to ensure VLAN interfaces without active members are disabled.

### Step 7: Process Aggregated Interfaces

```java
postProcessAggregatedInterfaces(configurations);
```

For each configuration:
1. **Populate channel group members**: Link child interfaces to their parent aggregate interface
2. **Compute aggregated bandwidth**: Set bandwidth = sum of active member bandwidths

```java
private void postProcessAggregatedInterfacesHelper(Map<String, Interface> interfaces) {
    // Populate aggregated interfaces with members referring to them
    interfaces.forEach((ifaceName, iface) ->
        populateChannelGroupMembers(interfaces, ifaceName, iface));

    // Compute bandwidth for aggregated interfaces
    computeAggregatedInterfaceBandwidths(interfaces);
}
```

### Step 8: Process Redundant Interfaces

```java
postProcessRedundantInterfaces(configurations);
```

For each VRF, compute bandwidth for redundant interfaces (HSRP/VRRP groups):
- Bandwidth = maximum bandwidth among active members

### Step 9: Initialize OSPF

```java
OspfTopologyUtils.initNeighborConfigs(nc);
postProcessOspfCosts(configurations);
```

1. Initialize OSPF neighbor configurations
2. Compute OSPF interface costs where not explicitly configured:
   - Cost is derived from reference bandwidth divided by interface bandwidth
   - Uses default reference bandwidth if not configured

### Step 10: Initialize EIGRP

```java
postProcessEigrpCosts(configurations); // Must be after postProcessAggregatedInterfaces
EigrpTopologyUtils.initNeighborConfigs(nc);
```

1. Initialize EIGRP neighbor configurations
2. Compute EIGRP bandwidth metric for aggregate and child interfaces:
   - If not explicitly configured, bandwidth component = interface bandwidth in kbps
   - Must run after aggregated interface processing to use computed bandwidth

### Step 11: Compute Completion Metadata

```java
computeAndStoreCompletionMetadata(snapshot, configurations);
```

Generate autocomplete metadata including:
- Filter names (ACLs, firewall rules)
- Interface names
- IP addresses
- Location information
- MLAG IDs
- Node names
- Prefixes
- Routing policy names
- Structure names
- VRF names
- Zone names

## Implementation Details

### Key Classes and Methods

#### `Batfish.postProcessSnapshot(NetworkSnapshot, Map<String, Configuration>)`

Main entry point for post-processing. Coordinates all post-processing steps in the correct order.

**Location**: `projects/batfish/src/main/java/org/batfish/main/Batfish.java:1991`

**Method Signature**:
```java
private void postProcessSnapshot(
    NetworkSnapshot snapshot,
    Map<String, Configuration> configurations)
```

**Processing Order**:
1. `updateBlacklistedAndInactiveConfigs()` - Steps 1-6
2. `postProcessAggregatedInterfaces()` - Step 7
3. `postProcessRedundantInterfaces()` - Step 8
4. `OspfTopologyUtils.initNeighborConfigs()` - Step 9a
5. `postProcessOspfCosts()` - Step 9b
6. `postProcessEigrpCosts()` - Step 10a
7. `EigrpTopologyUtils.initNeighborConfigs()` - Step 10b

#### `postProcessInterfaceDependencies(Map<String, Configuration>, Layer1Topologies)`

Resolves interface dependencies and deactivates interfaces that cannot be operational.

**Location**: `Batfish.java:1659`

**Key Logic**:
```java
static void postProcessInterfaceDependencies(
    Map<String, Configuration> configurations,
    Layer1Topologies layer1Topologies) {
    getInterfacesToDeactivate(configurations, layer1Topologies)
        .forEach((iface, inactiveReason) ->
            configurations.get(iface.getHostname())
                .getAllInterfaces()
                .get(iface.getInterface())
                .deactivate(inactiveReason));
}
```

#### `postProcessAggregatedInterfaces(Map<String, Configuration>)`

Processes aggregate (port-channel) interfaces and their members.

**Location**: `Batfish.java:1602`

**Key Operations**:
1. Populates `Interface._channelGroupMembers` with child interfaces
2. Calls `computeAggregatedInterfaceBandwidths()` to sum member bandwidths

#### `postProcessOspfCosts(Map<String, Configuration>)`

Initializes OSPF interface costs based on bandwidth.

**Location**: `Batfish.java:1691`

**Key Logic**:
```java
private void postProcessOspfCosts(Map<String, Configuration> configurations) {
    configurations.values().forEach(c ->
        c.getVrfs().values().forEach(vrf -> {
            vrf.getOspfProcesses().values()
                .forEach(p -> p.initInterfaceCosts(c));
        }));
}
```

#### `postProcessEigrpCosts(Map<String, Configuration>)`

Initializes EIGRP bandwidth metrics based on interface bandwidth.

**Location**: `Batfish.java:1671`

**Key Logic**:
```java
private void postProcessEigrpCosts(Map<String, Configuration> configurations) {
    configurations.values().stream()
        .flatMap(c -> c.getAllInterfaces().values().stream())
        .filter(iface -> iface.getEigrp() != null
            && (iface.getInterfaceType() == InterfaceType.AGGREGATED
                || iface.getInterfaceType() == InterfaceType.AGGREGATE_CHILD))
        .forEach(iface -> {
            EigrpMetricValues metricValues = iface.getEigrp().getMetric().getValues();
            if (metricValues.getBandwidth() == null) {
                Double bw = iface.getBandwidth();
                metricValues.setBandwidth(bw.longValue() / 1000); // convert to kbps
            }
        });
}
```

### Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                     Vendor-Independent Configs                   │
│                      (from Conversion Stage)                     │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│                  Apply Blacklists (Nodes/Interfaces)             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Process Interface Dependencies & Topology           │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│            Process Aggregated & Redundant Interfaces             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│                  Initialize OSPF and EIGRP                       │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│                    Compute Metadata                              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         v
┌─────────────────────────────────────────────────────────────────┐
│              Finalized Vendor-Independent Model                  │
│                    (Ready for Data Plane)                        │
└─────────────────────────────────────────────────────────────────┘
```

## Common Post-processing Scenarios

### Scenario 1: Aggregate Interface Bandwidth Calculation

**Before Post-processing**:
```java
// Port-channel1 (aggregate interface)
Interface po1 = new Interface("Port-channel1");
po1.setBandwidth(0.0); // Not yet computed

// Member interfaces
Interface eth1 = new Interface("Ethernet1/1/1");
eth1.setBandwidth(1.0e10); // 10 Gbps
eth1.setChannelGroup(1);

Interface eth2 = new Interface("Ethernet1/1/2");
eth2.setBandwidth(1.0e10); // 10 Gbps
eth2.setChannelGroup(1);
```

**After Post-processing**:
```java
// Port-channel1 bandwidth computed
po1.getBandwidth(); // 2.0e10 (20 Gbps = sum of members)
po1.getChannelGroupMembers(); // [Ethernet1/1/1, Ethernet1/1/2]
```

### Scenario 2: OSPF Cost Initialization

**Before Post-processing**:
```java
Interface gig1 = new Interface("GigabitEthernet1");
gig1.setBandwidth(1.0e9); // 1 Gbps

OspfInterface ospfIface = new OspfInterface();
ospfIface.setCost(null); // Not explicitly configured
```

**After Post-processing**:
```java
// Cost computed from reference bandwidth / interface bandwidth
// Assuming default reference bandwidth of 100 Mbps
ospfIface.getCost(); // 0.1 (100 Mbps / 1000 Mbps)
```

### Scenario 3: VLAN Interface Deactivation

**Before Post-processing**:
```java
// VLAN interface
Interface vlan10 = new Interface("Vlan10");
vlan10.setActive(true);

// Physical member
Interface gig1 = new Interface("GigabitEthernet1");
gig1.setAdminUp(false); // Admin down
```

**After Post-processing**:
```java
vlan10.getActive(); // false (deactivated due to no active members)
vlan10.getInactiveReason(); // "All members are inactive"
```

## Error Handling and Validation

Post-processing includes several validation checks:

1. **Bandwidth Validation**: Ensures bandwidth values are non-null for interfaces that require them (e.g., for IGP cost computation)

2. **Dependency Validation**: Cycles in interface dependencies are detected and broken appropriately

3. **Topology Validation**: Interfaces without topology connections are flagged for deactivation

4. **Blacklist Validation**: Blacklisted nodes/interfaces are consistently processed across all operations

## Integration with Other Pipeline Stages

### Before Post-processing (Conversion Stage)

The conversion stage produces vendor-independent configurations with:
- Basic interface properties (name, addresses, admin status)
- Protocol configurations (OSPF, EIGRP, BGP, etc.)
- Vendor-specific features normalized to generic model

### After Post-processing (Data Plane Stage)

The finalized configurations feed into data plane computation:
- Active interfaces contribute to topology
- IGP costs influence route selection
- Protocol state enables neighbor relationship simulation

## Performance Considerations

Post-processing is designed for efficiency:

1. **Single-Pass Algorithms**: Most operations traverse data structures once
2. **Parallel Processing**: Independent operations (e.g., per-node processing) can run in parallel
3. **Lazy Computation**: Some values (e.g., topology) are computed only when needed
4. **Caching**: Repeated operations (e.g., bandwidth lookups) use cached values

## Testing and Validation

Post-processing logic is tested through:

- **Unit tests**: Individual post-processing methods (see `BatfishTest.java`)
- **Integration tests**: Full pipeline processing with known expected outputs
- **Vendor-specific tests**: Ensure post-processing handles vendor-specific features correctly

## Related Documentation

- [Architecture Overview](../architecture/README.md)
- [Pipeline Overview](../architecture/pipeline_overview.md)
- [Conversion](../conversion/README.md)
- [Data Plane](../data_plane/README.md)

