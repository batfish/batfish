# Flow Dispositions

The disposition of a flow is what ultimately happens to it. We group the dispositions into two categories: the _success_
dispositions `ACCEPTED`, `DELIVERED_TO_SUBNET`, and `EXITS_NETWORK` mean the network delivered the flow somewhere
(a specific endpoint or network boundary); the _failure_ dispositions `DENIED_IN`, `DENIED_OUT`, `INSUFFICIENT_INFO`, `LOOP`, `NEIGHBOR_UNREACHABLE`, `NO_ROUTE`, and `NULL_ROUTE`
mean the network dropped the flow for some reason. 
Flow dispositions are shown in [Traceroute](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html?highlight=traceroute#Traceroute) and [Reachability](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html?highlight=reachability#Reachability) answers, and [Reachability](https://pybatfish.readthedocs.io/en/latest/notebooks/forwarding.html?highlight=reachability#Reachability) questions support searching for flows with a particular disposition or set of dispositions.

## Flow disposition meanings
Since Batfish snapshots are not expected to be complete (e.g. there may be other peer networks or ISPs excluded from the snapshot), we use
heuristics to assign some of the dispositions. In particular, the dispostions `DELIVERED_TO_SUBNET`, `EXITS_NETWORK`, 
`INSUFFICIENT_INFO` and `NEIGHBOR_UNREACHABLE` are used when a flow is forwarded out an interface and no device in the 
snapshot can receive it (i.e., no device responds to ARP). In this case, we use heuristics to decide whether the flow 
should be considered as being successfully delivered/handed-off
(`DELIVERED_TO_SUBNET` or `EXITS_NETWORK`) or dropped (`INSUFFICIENT_INFO` or `NEIGHBOR_UNREACHABLE`).

The following table gives the conditions when each disposition is used. Terminology:
* **Owned IPs** are configured on some interface in the snapshot.
* **Internal IPs** are either owned, or are contained in the connected subnet of some interface in the snapshot.
* **External IPs** are not internal.
* **Full subnets** are subnets whose addressable IPs (excluding network and broadcast IPs) are all owned.

| Disposition | Type | Description |
| --- | --- | --- |
| `ACCEPTED` | Success | The flow reaches an interface that owns the destination IP address. |
| `DENIED_IN` | Failure | The flow was denied by an ingress filter/ACL. |
| `DENIED_OUT` | Failure | The flow was denied by an egress filter/ACL. |
| `DELIVERED_TO_SUBNET` | Success | The flow is forwarded out an interface and ARP fails. The ARP IP is the destination IP, and is **unowned** and in the interface's connected subnet. |
| `EXITS_NETWORK` | Success | The flow is forwarded out an interface and ARP fails. The destination IP is **external**. |
| `INSUFFICIENT_INFO` | Failure | The flow is forwarded out an interface and ARP fails. It appears the flow should be forwarded to some other device in the network, but the connectivity is missing. The connected subnet is not **full**, so the snapshot may be missing a device. There are two cases: When ARPing for the destination IP, the destination IP is internal to network but not in the interface's connected subnet. When ARPing for a different IP, the ARP IP is either owned, or the destination IP is internal.
| `LOOP` | Failure | The flow entered a forwarding loop. |
| `NEIGHBOR_UNREACHABLE` | Failure | The flow is forwarded out an interface and ARP fails. Either the interface is **full**, or the ARP IP is the destination IP, and is **owned** and contained in a connected subnet elsewhere in the network.
| `NO_ROUTE` | Failure | The flow reached a VRF whose FIB did not have a route matching the destination IP. |
| `NULL_ROUTE` | Failure | The flow reached a VRF whose FIB had a longest-matching route that was a null route. |

Disposition assignment is implemented in [ForwardingAnalysisImpl](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/ForwardingAnalysisImpl.java).

See also:
* [FlowDisposition](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/datamodel/FlowDisposition.java): the enum defining the set of dispositions, and categorization into success and failure dispositions.
* [DispositionSpecifier](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/specifier/DispositionSpecifier.java): specifier used for question inputs (e.g. ). See also the [pybatfish docs](https://pybatfish.readthedocs.io/en/latest/specifiers.html?highlight=specifier#disposition-specifier).
* [IpOwners](https://github.com/batfish/batfish/blob/master/projects/common/src/main/java/org/batfish/common/topology/IpOwners.java)