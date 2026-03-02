# Cisco ACI Network Modeling in Batfish

## 1. Introduction

Batfish provides modeling and analysis capabilities for Cisco ACI (Application Centric Infrastructure) fabrics. This document describes Batfish's approach to modeling ACI networks, enabling users to analyze connectivity, contracts, routing, and topology within their ACI environments.

Cisco ACI is a software-defined networking (SDN) solution that uses a policy-based architecture. Unlike traditional network devices where configuration is distributed across individual switches, ACI uses a centralized policy model where:

- **Contracts** define allowed communication between endpoint groups
- **Tenants** provide multi-tenancy isolation
- **Bridge Domains** define Layer 2 forwarding
- **VRFs** define Layer 3 isolation
- **Fabric Nodes** (spines and leaves) form the physical underlay

Batfish's ACI modeling allows users to:
- Analyze traffic flows between EPGs through contracts
- Verify contract rules and security policies
- Troubleshoot connectivity issues between tenants/VRFs
- Understand fabric topology (spine-leaf architecture)
- Analyze external connectivity via L3Out (BGP, OSPF, static routes)

## 2. Snapshot Format and Directory Structure

### Standard Format (Single Fabric)

For a single ACI fabric, place the ACI configuration file in the `configs/` directory:

```
<snapshot>/
├── configs/
│   ├── ACI-fabric1.json     # ACI config export
│   ├── switch1.conf         # Other vendor configs
│   └── ...
├── topology.json            # Optional: external topology
└── hosts.json               # Optional: host information
```

The ACI configuration file must contain a valid `polUni` JSON structure exported from APIC.

### Multi-Fabric Format (Recommended)

For multiple ACI fabrics (e.g., DC1, DC2), use the `cisco_aci_configs/` directory structure:

```
<snapshot>/
├── cisco_aci_configs/
│   ├── DC1/
│   │   ├── config.json           # DC1 fabric config (polUni export)
│   │   └── fabric_links.json     # Optional: explicit fabric topology
│   ├── DC2/
│   │   ├── config.json           # DC2 fabric config
│   │   └── fabric_links.json     # Optional: explicit fabric topology
│   └── ...
├── configs/
│   └── ...                       # Other vendor configs
└── topology.json                 # External topology (non-ACI links)
```

### ACI Configuration Export

The ACI configuration must be exported from APIC as JSON. Use one of these methods:

**Method 1: APIC REST API**
```bash
# Export full configuration
curl -k -u admin:password \
  "https://apic/api/mo/uni.json?rsp-subtree=full&rsp-prop-include=config-only" \
  -o aci-config.json
```

**Method 2: APIC GUI**
1. Navigate to **Admin > Import/Export > Export Policy**
2. Create a new export policy
3. Export as JSON format
4. Download the exported file

### Fabric Links (Optional)

The optional `fabric_links.json` file provides explicit fabric topology from APIC's operational data. This is useful when you want to use actual fabric link information instead of the synthesized spine-leaf topology:

```json
[
  {
    "node1Id": "101",
    "node1Interface": "Ethernet1/1",
    "node2Id": "201",
    "node2Interface": "Ethernet1/1",
    "linkState": "up"
  },
  {
    "node1Id": "101",
    "node1Interface": "Ethernet1/2",
    "node2Id": "202",
    "node2Interface": "Ethernet1/1",
    "linkState": "up"
  }
]
```

To export fabric links from APIC:
```bash
curl -k -u admin:password \
  "https://apic/api/class/fabricLink.json" \
  -o fabric_links.json
```

## 3. Core ACI Concepts in Batfish

### Tenants (`fvTenant`)

Tenants are the primary container for policies in ACI. Batfish creates separate VRF contexts for each tenant's VRFs, maintaining proper isolation.

```
Tenant
├── VRFs (fvCtx)
├── Bridge Domains (fvBD)
├── Application Profiles (fvAp)
│   └── EPGs (fvAEPg)
├── Contracts (vzBrCP)
└── L3Outs (l3extOut)
```

### Bridge Domains and Subnets

Bridge domains are converted to VLAN interfaces with gateway addresses:

| ACI Object | Batfish Representation |
|------------|------------------------|
| Bridge Domain | Vlan interface on each leaf |
| Subnet | Interface IP address |
| VRF association | VRF assignment on interface |

### Endpoint Groups (EPGs)

EPGs are logical groupings that contract policies apply to:

| EPG Attribute | Batfish Handling |
|---------------|------------------|
| Bridge Domain | Interface VLAN assignment |
| Provided Contracts | ACL permitting traffic to this EPG |
| Consumed Contracts | ACL permitting traffic from this EPG |
| Path Attachments | Physical interface assignment |

### Contracts

Contracts define allowed communication between EPGs. Batfish converts contracts to `IpAccessList` objects:

```
Contract (vzBrCP)
├── Subject (vzSubj)
│   └── Filters (vzRsSubjFiltAtt)
│       └── Filter Entries (vzEntry)
│           ├── Protocol (tcp/udp/icmp)
│           ├── Ports (source/destination)
│           └── IP addresses
```

**Contract Direction:**
- **Provider EPG**: ACL allows traffic *to* the EPG (inbound)
- **Consumer EPG**: ACL allows traffic *from* the EPG (outbound)

### L3Out (External Connectivity)

L3Out configurations are converted to external routing:

| L3Out Component | Batfish Representation |
|-----------------|------------------------|
| BGP Peer | BgpPeer in BgpProcess |
| OSPF | OspfProcess with areas |
| Static Routes | StaticRoute in VRF |
| External EPG | ACL for external traffic |

## 4. Fabric Topology Modeling

### Spine-Leaf Architecture

Batfish automatically models the ACI spine-leaf topology:

```
                    +-------+
                    |Spine 1|
                    +-------+
                   /    |    \
                  /     |     \
            +-------+ +-------+ +-------+
            |Leaf 1| |Leaf 2| |Leaf 3|
            +-------+ +-------+ +-------+
                 |         |         |
            [Endpoints] [Endpoints] [Endpoints]
```

**Topology Rules:**
1. Every leaf connects to every spine (full mesh)
2. Leaves do not connect directly to other leaves (unless VPC peers)
3. Spines do not connect directly to other spines

### Fabric Node Roles

| Role | Description | Batfish Handling |
|------|-------------|------------------|
| `spine` | Core switch | Routes between leaves |
| `leaf` | ToR switch | Connects endpoints, applies contracts |
| `controller` | APIC controller | Excluded from topology |

**Special Case - Service Nodes:**
Nodes with names containing `-service-` or `-services-` are treated as leaf switches. For example:
- `SW-DC1-Services-NSAA03-SET-01` → treated as `leaf`

### Layer 1 Edge Generation

Layer 1 edges are created from multiple sources:

1. **Path Attachments**: Physical interfaces discovered from EPG-to-interface mappings
2. **Fabric Links**: Explicit topology from `fabric_links.json` (if provided)
3. **Synthesized Topology**: Full mesh spine-leaf connections (fallback)

## 5. Interface Name Normalization

ACI uses short interface names in path attachment tDn values (e.g., `eth1/3`), but Batfish uses canonical names (e.g., `Ethernet1/3`). The normalization is applied automatically:

| Short Name | Canonical Name |
|------------|----------------|
| `eth1/3` | `Ethernet1/3` |
| `po1` | `port-channel1` |
| `lo0` | `Loopback0` |
| `vl100` | `Vlan100` |

This ensures Layer 1 topology edges reference interfaces that exist in the configuration.

## 6. Traffic Flow Modeling

### Intra-Fabric Communication

Traffic between EPGs in the same fabric:

1. Source EPG interface → Contract ACL check → Destination EPG interface
2. Contract must be provided by one EPG and consumed by the other
3. Traffic is subject to VRF isolation

### Inter-Fabric Communication

Traffic between different ACI fabrics (DC1 ↔ DC2):

1. Detected from L3Out configurations with inter-fabric indicators
2. Layer 1 edges created between border leaves
3. BGP/OSPF routing enables cross-fabric traffic

### External Communication (L3Out)

Traffic to/from external networks:

1. L3Out defines external connectivity (BGP, OSPF, static)
2. External EPG defines external subnets
3. Contracts control what traffic can enter/exit

## 7. Conversion to Vendor-Independent Model

### Configuration Objects

Each fabric node becomes a `Configuration` object:

```java
Configuration config = Configuration.builder()
    .setHostname(fabricNode.getName())
    .setConfigurationFormat(ConfigurationFormat.CISCO_ACI)
    .setDefaultInboundAction(LineAction.PERMIT)  // ACI defaults to permit
    .build();
```

### Interface Creation

Interfaces are created from multiple sources:

| Source | Interface Type | Purpose |
|--------|---------------|---------|
| Bridge Domain | Vlan interface | Gateway for BD subnets |
| Path Attachment | Ethernet interface | Physical port for EPG |
| L3Out | Routed interface | External connectivity |
| Loopback | Loopback interface | Router ID, management |

### ACL Generation

Contracts are converted to ACLs:

```java
// Contract -> ACL conversion
IpAccessList contractAcl = IpAccessList.builder()
    .setName(getContractAclName(contract.getName()))
    .setLines(contractLines)  // From filter entries
    .build();
```

### Routing Configuration

L3Out routing protocols are converted:

| ACI Protocol | Batfish Model |
|--------------|---------------|
| BGP | BgpProcess with BgpPeer |
| OSPF | OspfProcess with areas |
| Static | StaticRoute entries |

## 8. Supported ACI Objects

### Fully Supported

| ACI Class | Name | Support Level |
|-----------|------|---------------|
| `fvTenant` | Tenant | Full |
| `fvCtx` | VRF | Full |
| `fvBD` | Bridge Domain | Full |
| `fvAEPg` | Endpoint Group | Full |
| `fvRsPathAtt` | Path Attachment | Full |
| `vzBrCP` | Contract | Full |
| `vzFilter` | Filter | Full |
| `vzEntry` | Filter Entry | Full |
| `l3extOut` | L3Out | Full (BGP, OSPF, Static) |
| `fabricNodePEp` | Fabric Node | Full |

### Partially Supported

| ACI Class | Name | Limitations |
|-----------|------|-------------|
| `l2extOut` | L2Out | Basic connectivity only |
| `vzTaboo` | Taboo Contract | Converted to deny ACLs |
| `fvRsConsIf` | Contract Interface | Consumer-side only |

### Not Yet Supported

| ACI Class | Name | Notes |
|-----------|------|-------|
| `fvAEPgPol` | EPG Policy | Future |
| `qosInstPol` | QoS | Future |
| `monInstPol` | Monitoring | Future |

## 9. Known Limitations

1. **Contract Scope**: Only tenant-scoped contracts are fully supported. VRF-scoped and global contracts have limited support.

2. **Microsegmentation**: Microsegmentation EPGs (uSeg EPGs) are treated as regular EPGs.

3. **Service Graphs**: Service graph chaining (traffic steering through firewalls/load balancers) is not modeled.

4. **Multi-Site**: ACI Multi-Site is not explicitly supported; each site should be treated as a separate fabric.

5. **Contracts with Targets**: Contracts with specific targets (e.g., `targetDscp`) only consider the base filter criteria.

6. **Implicit Contracts**: Default/implicit contracts (any-to-any within VRF) are not automatically added.

## 10. Troubleshooting

### Common Issues

**"Interface not found" errors in Layer 1 topology:**
- Cause: Interface name mismatch between topology and config
- Solution: Ensure interface names are normalized (use canonical names)

**EPG connectivity not working:**
- Cause: Missing or misconfigured contract
- Solution: Verify contract is provided by one EPG and consumed by the other

**VRF isolation not enforced:**
- Cause: Multiple VRFs using the same BD
- Solution: Check BD-to-VRF associations in ACI config

### Debug Logging

Enable debug logging for ACI parsing:

```bash
java -Dbatfish.loglevel=debug -jar batfish.jar ...
```

Look for messages like:
- `Parsing tenant: tenant1`
- `Created X interfaces from path attachments`
- `Normalized interface eth1/3 to Ethernet1/3`

## 11. References

- [Cisco ACI Documentation](https://www.cisco.com/c/en/us/solutions/data-center-virtualization/application-centric-infrastructure/index.html)
- [ACI REST API Guide](https://developer.cisco.com/site/aci/)
- [Batfish Documentation](https://batfish.readthedocs.io/)
