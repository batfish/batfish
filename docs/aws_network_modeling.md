# AWS Network Modeling in Batfish

## 1. Introduction to AWS Network Modeling in Batfish

Batfish provides comprehensive modeling and analysis capabilities for AWS cloud networks. This document describes Batfish's approach to modeling AWS Virtual Private Clouds (VPCs) and related networking components, enabling users to analyze connectivity, security, and routing within their AWS environments.

Batfish's AWS modeling capabilities allow users to:
- Visualize AWS network topology
- Analyze traffic flows between AWS resources
- Verify security group and network ACL configurations
- Troubleshoot connectivity issues
- Validate changes before deployment
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

The region-level hierarchy is optional if only one region is present in the data.

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

In the past, VPC routers were connected directly to internet and VPN gateways, but this approach was revised since routing tables are subnet-based.

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

## 7. Supported AWS Services

Batfish supports modeling of various AWS services and their network interactions, including:

- EC2 instances
- RDS databases
- Load balancers
- VPC endpoints for AWS services
- Transit gateways
- VPN connections
- Direct Connect (limited support)

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

### 8.4 Change Validation

Before implementing changes, Batfish can:
- Compare network behavior before and after changes
- Identify unintended consequences of changes
- Verify that changes achieve their intended goals
- Ensure compliance with security policies

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

The conversion process transforms these AWS-specific objects into Batfish's vendor-independent model, which is then used for analysis.

## 10. Future Enhancements

Potential future enhancements to Batfish's AWS modeling include:

- Support for additional AWS services
- Enhanced modeling of AWS Load Balancers
- Improved handling of Direct Connect
- Support for AWS Global Accelerator
- Integration with AWS CloudFormation and Terraform for pre-deployment validation
- Support for multi-account AWS environments
- Enhanced visualization of AWS network topology
- Support for AWS Transit Gateway Connect
- Modeling of AWS Network Firewall

These enhancements will further improve Batfish's ability to model and analyze AWS networks.