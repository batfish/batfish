
# What is Batfish?

Batfish is a network configuration analysis engine that enables network engineers to rapidly and safely evolve their network by guaranteeting that planned or current configurations are correct.

Batfish supports many types of correctness checks, including

1. Compliance and best-practices guidelines, e.g.:
  - Flag undefined but referenced structures (e.g., ACLs, route maps)
  - Flag defined but unused structures
  - Ensure that all interface MTUs are per the network's standard
  - AAA, SNMP, and NTP configuration is correct
  - Devices can only be accessed using SSHv2 and password is not null
  - Logging is on

2. Consistency of configuration across two or more devices, e.g.: 
  - BGP sessions are symmetrically configured across neighbors
  - IPSec/VPN tunnels symmetrically configured with the same key
  - All interface IP addresses are unique
  - Identically-named structures (e.g., ACLs, route maps) across devices have identical functionality

3. Checks on data flow, e.g.:
  - Path (shape) between two devices is as expected (e.g., traverses a firewall, valley-free routing)
  - Number of paths between two devices is as expected (i.e., correct multi-path configuration)
  - Paths for two devices inside the data center never leaves the data center
  - Certain services (e.g., DNS) are globally reachable and not blocked by any ACL in the network
  - Certain senstive services can be reached only from certain places in the network
  - All pairs of top-of-rack switches can reach each other

4. Fault-tolerance, e.g.: 
  - End-to-end reachability is not impacted for *any* flow after any single-link or -device failure
  - Traffic correctly fails over after a failure

5. "Differential" analysis of two sets of configuration, e.g.:
  - End-to-end reachability is identical across new and old configurations
  - Planned ACL changes have no collateral damage, e.g., relative to the current configuration, only flows that are intended to be (un)blocked are (un)blocked; no other flow is impacted.

# What devices does Batfish support? 

Batfish supports configurations for a large and growing set of network devices, including:
  - Cisco (Nexus, IOS, IOS-XR, ASA)
  - Juniper (JunOS, Firewall)
  - Arista
  - Dell Force10
  - MRV
  - VyOS

Let us know if your device is not on the list, and we'll try to support it.

