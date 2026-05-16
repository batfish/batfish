# AWS Network Modeling in Batfish

## 1. Introduction to AWS Network Modeling in Batfish

Batfish provides modeling and analysis capabilities for AWS cloud networks. This document describes Batfish's approach to modeling AWS Virtual Private Clouds (VPCs) and related networking components, enabling users to analyze connectivity, security, and routing within their AWS environments.

Batfish's AWS modeling capabilities allow users to:
- Analyze traffic flows between AWS resources
- Verify security group and network ACL configurations
- Troubleshoot connectivity issues
- Ensure compliance with security policies

This document provides a technical overview of how Batfish models AWS networks, including the input format, parsing process, conversion to the vendor-independent model, and the modeling of various AWS components.

## 2. Core AWS Networking Concepts

AWS provides a rich set of networking components that enable users to build complex network architectures. Key components include:

- **Virtual Private Clouds (VPCs)**: Isolated virtual networks that provide the foundation for AWS resources
- **Subnets**: Subdivisions of VPCs that can span availability zones
- **Internet Gateways (IGWs)**: Allow communication between VPC resources and the internet
- **NAT Gateways**: Enable outbound internet connectivity for resources in private subnets
- **VPC Peering**: Direct connections between VPCs
- **Transit Gateways**: Hub-and-spoke connectivity between multiple VPCs and on-premises networks
- **VPN Connections**: Secure connections between AWS and on-premises networks
- **Direct Connect**: Dedicated network connections from on-premises to AWS via a Direct Connect Gateway and Virtual Interfaces
- **Security Groups**: Stateful firewalls that control traffic at the instance level
- **Network ACLs**: Stateless firewalls that control traffic at the subnet level
- **VPC Endpoints**: Private connections to AWS services without traversing the internet

Batfish models these components and their interactions to provide a comprehensive view of the AWS network.

## 3. Batfish Input and Processing

### Input Format

Batfish expects AWS configuration data in a specific format, which consists of the output of various AWS describe commands packaged as JSON files:

```
<snapshot>
    |------------aws_configs
                       |-------------<region-name>
                                               |-------------Vpcs.json
                                                                ...
```

The region-level hierarchy is optional if only one region is present in the data. More information on the snapshot packaging is [here](https://batfish.readthedocs.io/en/latest/formats.html#aws).

### Parsing Process

Batfish parses these files into `AwsConfiguration.java`, which contains a list of regions. Within each region, there are maps for various concepts such as instances, internet gateways, etc. Each map links concept names to Java objects representing them.

### Conversion to Vendor-Independent Model

The conversion to the vendor-independent (VI) model begins at `AwsConfiguration.java`. The overall process involves:

1. Creating nodes for most concepts, including VPCs, subnets, instances, and gateways
2. Creating interfaces on these nodes and assigning IP addresses to establish connectivity
3. Processing security groups to control traffic to and from instances
4. Processing network ACLs to control traffic at subnet nodes
5. Processing route tables and installing static routes to enable routing

## 4. AWS Network Topology in Batfish

Batfish models the AWS network topology by creating nodes for various AWS components and connecting them with interfaces. The key topology connections include:

- Instances connect to subnet routers
- Subnet routers connect to VPC routers (for intra-VPC traffic)
- Subnet routers connect to internet gateways (for internet traffic)
- Subnet routers connect to VPN gateways (for site traffic)

## 5. Traffic Flow Modeling

Batfish models various traffic flows within AWS networks:

### Between Instances in the Same Subnet

When the destination IP is in the private IP space:
- Instances communicate with each other directly via a shared LAN

When the destination IP is a public IP in the subnet:
- [Src-instance] → subnet-router → Dst-instance
- The first hop occurs because the source instance has a default route pointing to the subnet router
- The second hop occurs because of a static route on the subnet router for the public IP

### Between Instances Across Subnets

- [Src-instance] → src-subnet-router → vpc-router → dst-subnet-router → dst-instance
- The second hop to the VPC router happens if the source subnet's route table points to the VPC for the VPC-level private address space
- The third hop happens because of static routes for individual subnets on the VPC router

### From Instance to Internet

- [src-instance] → src-subnet → internet-gateway → aws-bb → internet
- The second hop to the internet gateway happens if the source subnet has a proper entry (e.g., default) to the internet gateway
- The internet gateway gets the default route from the internet via AWS backbone

### From Internet to Public IP

- [internet] → aws-bb → internet-gateway → subnet-router → instance
- The second hop to the internet gateway happens because the gateway announces all of its public IPs to the AWS backbone
- The third hop happens because of static routes on the gateway pointing to the subnet router

### Traffic To and From On-Premises Networks

- The VPN gateway (AWS side) establishes IPSec tunnels to the customer gateway (on-premises)
- Static routing or BGP may be configured on top of these tunnels
- With BGP, the VPN gateway announces VPC prefixes to the customer gateway
- Traffic flow depends on subnet routing tables, BGP-learned announcements, and static routes

### Traffic To and From On-Premises Networks via Direct Connect

For hybrid networks using AWS Direct Connect with a Transit Gateway attachment:

- [src-instance] → src-subnet-router → vpc-router → tgw → dx-gateway → on-prem-router
- The TGW route table for the DX attachment receives BGP routes from the DXGW peer; a customer prefix advertised over the DX VIF reaches the TGW via BGP and is then forwarded toward the DXGW
- The DXGW relays customer-advertised prefixes to attached TGWs (filtered by the per-association `AllowedPrefixesToDirectConnectGateway` list)
- The DXGW advertises the allowed-prefix list to the on-prem customer router over the VIF BGP session, matching AWS's documented behavior
- **TGW route preference**: when both Direct Connect and VPN attachments propagate the same prefix into the same TGW route table, AWS prefers the DX path. Batfish encodes this with elevated BGP local-preference on the DX peer's import policy.

## 6. AWS Component Modeling

### 6.1 VPCs and Subnets

VPCs and subnets are modeled as nodes in the Batfish network model. Each VPC has a router that connects to subnet routers, and each subnet has a router that connects to instances within the subnet.

### 6.2 Internet Connectivity

Internet connectivity is modeled by creating:

1. An AWS backbone (AWS-BB) node representing Amazon's backbone network (ASN 16509)
2. An Internet node
3. Connections between AWS-BB, Internet Gateways, and the Internet node

Routing is configured as follows:
- Internet Gateways have a static default route pointing to AWS-BB
- AWS-BB has static routes pointing to Internet Gateways for public addresses in their VPCs
- AWS-BB peers with the Internet using BGP, announcing public address spaces of VPCs

This design enables connectivity between VPCs and the Internet, allowing traffic from on-premises networks to reach public VPC addresses and vice versa.

### 6.3 Security Groups

AWS Security Groups are stateful firewalls that control traffic at the instance level. Key characteristics include:

- They only "allow" packets as defined in the rules; by default, everything is denied
- They are stateful: if a request is allowed, the response is automatically allowed
- Sources can include other security groups

In Batfish, Security Groups are modeled as ACL lines. The implementation:
1. Maintains a reverse mapping between security group IDs and instances having them
2. Populates a map containing security group IDs mapped to sets of IP wildcards
3. Uses these IP wildcards to populate ACL fields when a security group ID is mentioned in a rule

### 6.4 Network ACLs

Network ACLs are stateless firewalls that control traffic at the subnet level. In Batfish, they are modeled as ACLs applied to subnet interfaces.

### 6.5 VPC Peering

VPC peering connections are modeled using VRFs (Virtual Routing and Forwarding instances) per peering connection on VPC nodes:

1. Interfaces are created on the two VPC nodes and placed in a peering-specific VRF
2. Default routes are created in the peering VRF pointing to the other VPC
3. For each subnet using the connection:
   - An interface is created on the VPC node in the peering VRF
   - An interface is created on the subnet node (default VRF)
   - These interfaces are connected
   - Static routes are created to direct traffic appropriately

### 6.6 Transit Gateways

Transit gateways connect multiple VPCs and VPN connections in a hub-and-spoke topology. Key characteristics include:

- A transit gateway has multiple attachments (VPCs or VPNs)
- VPCs must be in the same region as the transit gateway
- Transit gateways need to be enabled per availability zone
- A transit gateway can have multiple routing tables

In Batfish, transit gateways are modeled as follows:

For VPC attachments:
- Each VPC attachment is an interface on the transit gateway
- Each routing table is a separate VRF on the transit gateway
- The attachment interface is in the VRF of its associated routing table
- At VPCs and subnets, connectivity is modeled similar to VPC peering connections

For VPN attachments:
- The transit gateway gets two interfaces, one for each IPSec tunnel

Recent updates to the transit gateway modeling include:
- Creating multiple nodes for each transit gateway (one per subnet it attaches to, plus a master node)
- Configuring interfaces and routing to accurately model AWS behavior with NACLs

### 6.7 NAT Gateways

NAT Gateways enable outbound internet connectivity for instances in private subnets. Key characteristics include:

- NAT Gateways are placed in public subnets and have a public IP
- Instances reach the NAT using their subnet routing table
- NAT Gateways use ports 1024-65535 and support TCP, UDP, and ICMP
- Network ACLs are evaluated on the way to the NAT and on the way out

In Batfish, NAT Gateways are modeled by creating a link between the VPC router and the NAT Gateway, bypassing the NAT subnet's routing table while still evaluating network ACLs.

### 6.8 VPC Endpoints

VPC endpoints provide private connections to AWS services. There are two types:

1. Interface endpoints:
   - Generate one or more nodes, one in each subnet they attach to
   - Act like instances within the subnet
   - Can be assigned security groups

2. Gateway endpoints:
   - Generate one node with two interfaces (one facing the VPC, one facing the service)
   - Connect to the VPC similar to IGWs and VGWs
   - Have static routes to service prefixes
   - Include an incoming filter to reject non-service-prefix destinations

### 6.9 Direct Connect

AWS Direct Connect provides dedicated connectivity between on-premises networks and AWS via a **Direct Connect Gateway (DXGW)** that is attached to one or more **Transit Gateways** (or Virtual Private Gateways). On-premises traffic enters AWS over a **Virtual Interface (VIF)**, which is a tagged Ethernet sub-interface on a physical Direct Connect connection. Batfish currently models **Transit VIFs** end-to-end (DXGW → TGW → VPC); Private VIFs (DXGW → VGW) and Public VIFs are not yet modeled.

#### Required input files

The following AWS describe-output JSON files must be present in the snapshot to enable Direct Connect modeling. The Direct Connect API is global (one call per account returns all DX resources):

- **`DirectConnectGateways.json`** — `dx:DescribeDirectConnectGateways`. Provides the DXGW ID, name, and Amazon-side ASN.
- **`VirtualInterfaces.json`** — `dx:DescribeVirtualInterfaces`. Provides VIF ID, type (`transit`/`private`/`public`), the parent DXGW, VLAN, customer ASN, Amazon and customer tunnel-inside addresses, and BGP peer info.
- **`DirectConnectGatewayAssociations.json`** — `dx:DescribeDirectConnectGatewayAssociations`. Provides the DXGW-to-TGW (or DXGW-to-VGW) association, including the critical `AllowedPrefixesToDirectConnectGateway` list.
- **`TransitGatewayAttachments.json`** — already collected. Entries with `ResourceType: direct-connect-gateway` link a TGW attachment to a DXGW. Prior to Direct Connect support, these entries were silently dropped.

The `dx:DescribeDirectConnectGatewayAttachments` API (DXGW ↔ VIF mapping) is also useful but is reconstructable from the parent DXGW IDs in `VirtualInterfaces.json`.

#### Topology overview

```
on-prem-router ── BGP/eBGP ── [DXGW node] ── BGP/eBGP unnumbered ── [TGW node] ── ... ── VPC
   AS 65001        VIF subnet     AS 64513      link-local L1            AS 64512
                   (e.g.                       (per route table)
                    169.254.10.x/30)
```

The on-premises router and the DXGW share an IP subnet on the VIF and run eBGP. The DXGW and TGW share a link-local subnet (L1-paired interfaces) and run eBGP unnumbered. Each side speaks its respective Amazon-side ASN (per-DXGW for DXGW; per-TGW for TGW).

#### Node and routing model

**`DirectConnectGateway` Configuration node** (one per DXGW; created in `Region.toConfigurationNodes()`)

Uses a single default VRF for both customer-facing (VIF) and TGW-facing interfaces, so that routes received on one side are immediately available for forwarding on the other.

- A `bgp-loopback` interface with a link-local address hosts the BGP process.
- For each `VirtualInterface` whose `DirectConnectGatewayId` matches this DXGW, a VIF interface is created with the Amazon-side `ConcreteInterfaceAddress` (e.g., 169.254.10.1/30). A `BgpActivePeerConfig` is added with the customer's address as the peer and the customer ASN as remote-AS.
- For each `DirectConnectGatewayAssociation` involving this DXGW:
  - Each prefix in `AllowedPrefixesToDirectConnectGateway` is installed as a static null-route. These statics are advertised to the on-prem customer over the VIF (matching AWS's "advertise the allowed-prefix list" behavior). They use a high admin distance so that more-specific BGP routes from the TGW peer win in the FIB.
  - A per-association TGW-export policy (`tgwExportPolicyName`) is built that filters routes received from on-prem to those within or equal to one of the allowed prefixes (using `PrefixRange.sameAsOrMoreSpecificThan`). This is the boundary filter that AWS applies between the DXGW and the TGW route table.

**TGW changes for `DIRECT_CONNECT_GATEWAY` attachments** (in `TransitGateway.connectAttachment`)

The `DIRECT_CONNECT_GATEWAY` case in `TransitGateway`'s attachment-dispatch switch does the following:

1. Locates the matching `DirectConnectGatewayAssociation` for `(DXGW, TGW)`.
2. Ensures a BGP process exists on the TGW VRF for the route table the DX attachment is associated with (creates one if not — shared with VPN attachments in the same VRF).
3. Calls `Utils.connect()` to create link-local interfaces between the TGW VRF and the DXGW default VRF, with an L1 edge.
4. Builds a **per-route-table import policy** (`dxImportPolicyName`) that:
   - Reads any AWS Direct Connect traffic-engineering community on the route (`7224:7300` HIGH, `7224:7200` MEDIUM, `7224:7100` LOW).
   - Sets BGP local-preference accordingly: HIGH=300, MEDIUM=200, LOW=150 (no community → MEDIUM).
   - All three values exceed the default BGP local-preference (100) used for VPN routes, so DX routes always win against VPN routes for the same prefix in the TGW's BGP best-path selection.
5. Creates **`BgpUnnumberedPeerConfig`** entries on both sides:
   - On the TGW (in the DX route table's VRF): peer-interface = TGW's link-local interface to the DXGW; export = `bgpExportPolicyName(vrf)`; import = the DX import policy from step 4.
   - On the DXGW (default VRF): peer-interface = DXGW's link-local interface to the TGW; export = the per-association `tgwExportPolicyName`; import = `DXGW_IMPORT_POLICY_NAME` (accept all BGP).

**Static-route attachment dispatch** (in `addTransitGatewayStaticRouteAttachment`)

When a TGW route table has a static route targeting a DX attachment, the TGW installs a static route with the next-hop set to the DXGW's link-local interface. This handles user-configured static routes (as opposed to BGP-propagated routes).

#### Information flow

**Customer prefix → AWS:**
1. On-prem advertises a prefix via BGP over the VIF.
2. The DXGW receives it (default VRF, unfiltered import).
3. For each TGW peer, the DXGW's per-association export policy filters by `AllowedPrefixesToDirectConnectGateway` (same-as-or-more-specific). Matching prefixes are advertised to the TGW.
4. The TGW's DX import policy tags the route with the appropriate local-preference based on community (or the MEDIUM default).
5. The route enters the TGW's VRF for the DX route table, available for forwarding from VPCs.

**AWS prefix → on-prem:**
1. The TGW VRF holds VPC CIDRs as static routes (installed by `propagateRoutesVpc`) and any other routes for that route table.
2. The TGW VRF's BGP export policy (`ACCEPT_ALL_BGP_AND_STATIC`) advertises these to the DXGW peer.
3. The DXGW's default VRF receives them (unfiltered import).
4. The DXGW's VIF export policy advertises the originated allowed-prefix statics to the on-prem customer. These represent the summary prefixes the customer is permitted to reach via DX.
5. Forwarding within an allowed prefix on the DXGW uses the more-specific BGP route from the TGW (lower admin distance than the static null-route).

#### Route-table isolation

Each TGW route table has its own VRF on the TGW node. The DX attachment is associated with exactly one route table; BGP peering between the DXGW and the TGW is established in *only that VRF*. As a consequence:

- VPCs propagated to a different TGW route table than the DX attachment are NOT visible on the DXGW.
- Multiple DX attachments to different route tables on the same TGW result in independent BGP peers (one per route table) — each with its own DX import policy.

This isolation is implemented structurally via per-VRF BGP processes; no extra filtering is needed.

#### TGW route preference (DX > VPN)

AWS documents the following preference order on a Transit Gateway: **static > Direct Connect propagated > VPN propagated > peering propagated**. Batfish models the DX > VPN portion via BGP local-preference:

| Source | Local-preference on TGW |
|---|---|
| Direct Connect (community `7224:7300`) | 300 |
| Direct Connect (community `7224:7200` or default) | 200 |
| Direct Connect (community `7224:7100`) | 150 |
| VPN-propagated BGP route | 100 (default) |

Static routes win because they have admin distance 1, lower than BGP's 20.

#### Customer traffic-engineering communities

Customers attach BGP communities to advertisements over the VIF to control AWS-side path preference among multiple DX paths to the same prefix:

| Community | Meaning | Use case |
|---|---|---|
| `7224:7300` | High preference | Active path |
| `7224:7200` | Medium preference (default) | Active/active ECMP |
| `7224:7100` | Low preference | Passive/backup path |

Communities are honored on the AWS-side BGP best-path selection within a single TGW route table. They do not influence on-premises route preference for outbound traffic — that is controlled by the customer's own BGP policy. See [AWS DX routing policies and BGP communities](https://docs.aws.amazon.com/directconnect/latest/UserGuide/routing-and-bgp.html).

#### Limitations and known gaps

- **Transit VIFs only.** Private VIFs (DXGW → VGW) and Public VIFs are not yet modeled. The `VirtualInterface` parser accepts entries of any type, but only Transit VIFs are wired into the data plane.
- **`RouteFilterPrefixes` on VIFs is ignored.** This per-VIF prefix filter applies primarily to Public VIFs and would need to be wired as an inbound BGP filter when those are added.
- **Multiple DXGWs sharing a VIF** is rare; not specifically tested.
- **MACsec, LAGs, IPv6, SiteLink, and BGP MD5 authentication** are not represented.
- **BGP session liveness from VIF telemetry is intentionally not consulted.** The `BgpStatus` and `BgpPeerState` fields on each VIF's BGP peer (e.g., `up`/`down`) are not read. Batfish models *configuration*: BGP sessions converge based on the parsed snapshot's interfaces, IPs, ASNs, and policies. Reading the runtime status field would conflate config with operational state and prevent users from exploring what-if scenarios — the value of asking Batfish "what would happen if I fixed this BGP config?" is lost if Batfish hard-coded the session to "down" because AWS happened to report it as such at snapshot time. Users who want to model a session as down should adjust the underlying config (e.g., remove the BGP peer, change the customer ASN to a non-matching value).
- **Cross-account DXGW associations** (DXGW owned by one account, TGW owned by another) are parsed but not specifically de-duplicated like cross-account TGWs are.

## 7. Supported AWS Services

Batfish supports modeling of various AWS services and their network interactions, including:

- EC2 instances
- RDS databases
- Load balancers
- VPC endpoints for AWS services
- Transit gateways
- VPN connections
- Direct Connect (Transit VIFs end-to-end via TGW; Private/Public VIFs not yet modeled)

## 8. Use Cases and Applications

### 8.1 Network Visibility and Documentation

Batfish provides comprehensive visibility into AWS network topology, including:
- VPC-to-VPC connectivity
- Subnet-to-subnet connectivity
- Instance-to-instance connectivity
- Internet accessibility
- On-premises connectivity

This visibility helps users understand their network architecture and document it effectively.

### 8.2 Security Analysis

Batfish enables various security analyses:

- Identifying instances accessible from the internet
- Verifying security group configurations
- Validating network ACL rules
- Ensuring proper isolation between environments
- Detecting potential security vulnerabilities

### 8.3 Connectivity Troubleshooting

Batfish helps troubleshoot connectivity issues by:
- Tracing packet paths through the network
- Identifying blocking ACLs or security groups
- Verifying routing table configurations
- Analyzing VPC peering and transit gateway setups

## 9. Implementation Details

Batfish's AWS modeling is implemented in Java, with key classes including:
- `AwsConfiguration.java`: Top-level class for AWS configuration
- `Region.java`: Represents an AWS region
- `Vpc.java`: Represents a VPC
- `Subnet.java`: Represents a subnet
- `Instance.java`: Represents an EC2 instance
- `SecurityGroup.java`: Represents a security group
- `VpcPeeringConnection.java`: Represents a VPC peering connection
- `TransitGateway.java`: Represents a transit gateway
- `VpnConnection.java`: Represents a VPN connection (terminating at a VGW or TGW)
- `DirectConnectGateway.java`: Represents an AWS Direct Connect Gateway (DXGW)
- `DirectConnectGatewayAssociation.java`: Represents a DXGW-to-TGW (or DXGW-to-VGW) association, including the `AllowedPrefixesToDirectConnectGateway` filter
- `DirectConnectVirtualInterface.java`: Represents a Direct Connect Virtual Interface (Transit/Private/Public VIF)

The conversion process transforms these AWS-specific objects into Batfish's vendor-independent model, which is then used for analysis.

