# Batfish Use Cases

This document outlines common use cases for Batfish and provides examples of how to address them.

## Network Compliance Validation

### Use Case: Ensuring Configuration Standards Compliance

**Problem**: Organizations need to ensure that all network devices comply with internal standards and best practices.

**Solution with Batfish**:

1. Define compliance rules as Batfish questions
2. Create a snapshot of your network
3. Run compliance questions against the snapshot
4. Generate reports highlighting violations

**Example**:

```python
# Check that all interfaces have descriptions
interface_props = bf.q.interfaceProperties(nodes=".*", properties=["Description"]).answer()
interfaces_without_description = interface_props[interface_props["Description"].isna()]
```

### Use Case: Regulatory Compliance

**Problem**: Organizations must demonstrate compliance with regulatory requirements (e.g., PCI DSS, HIPAA).

**Solution with Batfish**:

1. Translate regulatory requirements into Batfish questions
2. Create a snapshot of your network
3. Run compliance questions against the snapshot
4. Generate compliance reports for auditors

**Example**:

```python
# Check that sensitive subnets are properly isolated
sensitive_subnets = ["10.1.2.0/24", "10.1.3.0/24"]  # PCI zones
for subnet in sensitive_subnets:
    reachability = bf.q.reachability(
        pathConstraints=PathConstraints(startLocation="@enter(Inet)"),
        headers=HeaderConstraints(dstIps=subnet),
        actions="SUCCESS"
    ).answer()
    # Analyze which external sources can reach sensitive subnets
```

## Change Management

### Use Case: Pre-deployment Validation

**Problem**: Network changes can have unintended consequences that are difficult to predict.

**Solution with Batfish**:

1. Create a snapshot of the current network
2. Create a snapshot with proposed changes
3. Run differential questions to compare behavior
4. Identify any unintended consequences

**Example**:

```python
# Compare reachability before and after changes
base_reach = bf.q.reachability(
    snapshot="base",
    pathConstraints=PathConstraints(startLocation="@enter(Internet)"),
    headers=HeaderConstraints(dstIps="10.0.0.0/8"),
    actions="SUCCESS"
).answer()

change_reach = bf.q.reachability(
    snapshot="change",
    pathConstraints=PathConstraints(startLocation="@enter(Internet)"),
    headers=HeaderConstraints(dstIps="10.0.0.0/8"),
    actions="SUCCESS"
).answer()

# Analyze differences
```

### Use Case: Network Migration Planning

**Problem**: Migrating to a new network architecture requires careful planning to ensure service continuity.

**Solution with Batfish**:

1. Create a snapshot of the current network
2. Create snapshots for each migration phase
3. Validate that each phase maintains required connectivity
4. Identify potential issues before they occur in production

**Example**:

```python
# Verify that critical services remain accessible during migration
for phase in ["phase1", "phase2", "phase3"]:
    service_reach = bf.q.reachability(
        snapshot=phase,
        pathConstraints=PathConstraints(startLocation="@enter(client_network)"),
        headers=HeaderConstraints(dstIps="10.100.0.0/24", dstPorts="80,443"),
        actions="SUCCESS"
    ).answer()
    # Verify service accessibility in each phase
```

## Troubleshooting

### Use Case: Connectivity Issues

**Problem**: Users report intermittent connectivity issues that are difficult to reproduce.

**Solution with Batfish**:

1. Create a snapshot of the current network
2. Use traceroute questions to analyze packet flow
3. Identify where packets might be dropped or misrouted
4. Analyze device configurations at problem points

**Example**:

```python
# Trace packets from source to destination
traceroute = bf.q.traceroute(
    startLocation="host1",
    headers=HeaderConstraints(dstIps="10.2.3.4", srcIps="10.1.2.3")
).answer()

# Analyze where packets are dropped
drops = traceroute[traceroute["Flow_Disposition"] != "ACCEPTED"]
```

### Use Case: ACL Troubleshooting

**Problem**: Complex ACLs can have unexpected behavior, especially with overlapping rules.

**Solution with Batfish**:

1. Create a snapshot of the network
2. Use searchFilters questions to analyze ACL behavior
3. Identify which rules match specific traffic
4. Find potential rule conflicts or oversights

**Example**:

```python
# Check which ACL rules match specific traffic
acl_matches = bf.q.searchFilters(
    filters="acl_name",
    headers=HeaderConstraints(srcIps="10.1.2.3", dstIps="10.4.5.6", dstPorts="443")
).answer()
```

## Network Security

### Use Case: Security Zone Validation

**Problem**: Ensuring proper isolation between security zones is critical but complex.

**Solution with Batfish**:

1. Create a snapshot of the network
2. Define security zones as sets of nodes or interfaces
3. Use reachability questions to verify isolation
4. Identify any unauthorized paths between zones

**Example**:

```python
# Check for unauthorized access between zones
untrusted_to_trusted = bf.q.reachability(
    pathConstraints=PathConstraints(startLocation="@enter(untrusted_zone)"),
    headers=HeaderConstraints(dstIps="10.100.0.0/16"),  # trusted zone
    actions="SUCCESS"
).answer()
```

### Use Case: Firewall Rule Optimization

**Problem**: Firewall rule sets grow over time, becoming inefficient and hard to maintain.

**Solution with Batfish**:

1. Create a snapshot of the network
2. Analyze firewall rules for redundancy, shadowing, and inefficiency
3. Identify rules that can be consolidated or removed
4. Validate that proposed changes maintain the same security posture

**Example**:

```python
# Find redundant or shadowed rules
filter_analysis = bf.q.filterLineReachability().answer()
shadowed_rules = filter_analysis[filter_analysis["Unreachable_Line_Action"] != "UNMATCHABLE"]
```

## Network Documentation

### Use Case: Automated Network Documentation

**Problem**: Keeping network documentation up-to-date is time-consuming and often neglected.

**Solution with Batfish**:

1. Create a snapshot of the current network
2. Use Batfish questions to extract network information
3. Generate documentation automatically
4. Update documentation as part of the change management process

**Example**:

```python
# Generate IP address usage documentation
ip_owners = bf.q.ipOwners().answer()
interface_props = bf.q.interfaceProperties().answer()

# Combine to create IP address documentation
ip_documentation = pd.merge(ip_owners, interface_props, on=["Interface", "Node"])
```

## Network Design Validation

### Use Case: Redundancy Validation

**Problem**: Ensuring network redundancy works as designed is critical for high availability.

**Solution with Batfish**:

1. Create a snapshot of the network
2. Simulate failure scenarios using Batfish
3. Verify that traffic is properly rerouted
4. Identify single points of failure

**Example**:

```python
# Check if service remains available when a link fails
for interface in critical_interfaces:
    failure_reach = bf.q.reachability(
        pathConstraints=PathConstraints(startLocation="@enter(client_network)"),
        headers=HeaderConstraints(dstIps="10.100.0.0/24"),
        actions="SUCCESS",
        failureInfo={"interface": interface}
    ).answer()
    # Verify service remains accessible
```

### Use Case: Capacity Planning

**Problem**: Ensuring the network has sufficient capacity for current and future needs.

**Solution with Batfish**:

1. Create a snapshot of the current network
2. Analyze routing and forwarding paths
3. Identify potential bottlenecks
4. Simulate traffic patterns to validate capacity

**Example**:

```python
# Identify potential bottlenecks
routes = bf.q.routes().answer()
route_counts = routes.groupby("Next_Hop_Interface").size().reset_index(name="Route_Count")
high_use_interfaces = route_counts.sort_values("Route_Count", ascending=False).head(10)
```
