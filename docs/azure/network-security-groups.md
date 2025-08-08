# Azure Security Groups

In Azure, the primary objects used for filtering traffic are called **Network Security Groups (NSGs)**. NSGs allow you to control inbound and outbound traffic to Azure resources.

## Batfish Modeling

In Batfish, traffic filtering is primarily achieved using **Access Control Lists (ACLs)**. While NSGs and ACLs serve similar purposes, there are some key differences:

- **Ordering**: NSG rules are evaluated based on their priority, while ACLs process rules sequentially from the first to the last.
- **Direction**: NSGs apply directionality at the rule level (inbound or outbound), whereas ACLs are applied as a whole to a specific direction (in or out).
- **Service Tags**: NSGs can utilize service tags to identify specific areas based on context (e.g., current subnet range, target subnet’s NAT Gateway, etc.).

### Conversion of NSGs to ACLs

To enable Batfish to utilize NSGs, they must be converted into ACLs. This conversion process allows Batfish to effectively model the security rules defined by NSGs.

- **Application on Subnets**: Just as in Azure, NSGs can be applied to subnets. In this case, two ACLs (one for each direction) are set on the subnet's LAN-facing interface.
- **Application on Interfaces**: NSGs can also be applied directly to interfaces. Similar to subnets, two ACLs are configured on the specified interface.

## Packet Flow

### From Host to Anywhere

When a host sends a packet to an external destination, the flow is as follows:

1. **Packet Initiation**: The host sends a packet from `[host-private-ip]` to `[google-ip]`.
2. **Subnet Router**: The packet reaches the subnet router.
3. **Filtering**: Filters are applied on the LAN-facing interface of the subnet router (e.g., Block, Deny, Accept).
4. **Forwarding**: Based on the filtering results, the packet is either forwarded or dropped.

### From Anywhere to Host

When a packet is sent from an external source to a host, the flow is as follows:

1. **Packet Reception**: A packet arrives at the subnet router from an external source `[somewhere]` to `[host-private-ip]`.
2. **Filtering**: Filters are applied on the LAN-facing interface of the subnet router (e.g., Block, Deny, Accept).
3. **Forwarding**: Based on the filtering results, the packet is either forwarded to the host or dropped.
