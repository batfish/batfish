
## What is Batfish?

Batfish is a network configuration analysis tool that can find bugs and guarantee the correctness of (planned or current) network configurations. It enables network engineers to rapidly and safely evolve their network, without fear of outages or security breaches.

**Batfish does not require access to network devices.** It only needs off line configuration files, e.g., pulled from RANCID or template authoring tools.

## What kinds of correctness checks does Batfish support?

Batfish supports many types of correctness checks, including

1. #####Compliance and best-practices guidelines, e.g.:
  - Flag undefined but referenced structures (e.g., ACLs, route maps)
  - Flag defined but unused structures
  - Ensure that all interface MTUs are per the network's standard
  - AAA, SNMP, and NTP configuration is correct
  - Devices can only be accessed using SSHv2 and password is not null
  - Logging is on

2. #####Consistency of configuration across two or more devices, e.g.: 
  - BGP sessions are compatibly configured across neighbors
  - IPSec/VPN tunnels compatibly configured with the same key
  - All interface IP addresses are unique
  - Identically-named structures (e.g., ACLs, route maps) across devices have identical functionality

3. #####Checks on data flow, e.g.:
  - Path (shape) between two devices is as expected (e.g., traverses a firewall, valley-free routing)
  - Number of paths between two devices is as expected (i.e., correct multi-path configuration)
  - Paths for two devices inside the data center never leaves the data center
  - Certain services (e.g., DNS) are globally reachable and not blocked by any ACL in the network
  - Certain sensitive services can be reached only from specific subnets or devices
  - All pairs of top-of-rack switches can reach each other

4. #####Fault-tolerance, e.g.: 
  - End-to-end reachability is not impacted for *any* flow after *any* single-link or -device failure
  - Traffic correctly fails over after a failure

5. #####"Differential" analysis of two sets of configuration, e.g.:
  - End-to-end reachability is identical across new and old configurations
  - Planned ACL changes have no collateral damage, e.g., relative to the current configuration, only flows that are intended to be (un)blocked are (un)blocked; no other flow is impacted.
  - Two configurations, potentially from different vendors, are semantically equivalent

## How do I get started?

Read the [INSTALL.md] (https://github.com/arifogel/batfish/blob/master/INSTALL.md) file.

Batfish is structured as a service that may be hosted locally or on a remote machine. The service can be accessed via RESTful APIs using any client language. Java and Python clients are included in this repository.

Instead of DIY, if you'd prefer a cloud-hosted, configuration analysis service, drop a line to info@intentionet.com.

## What devices does Batfish support? 

Batfish supports configurations for a large and growing set of (physical and virtual) devices, including:
  - Arista
  - AWS VPCs
  - Cisco (Nexus, IOS, IOS-XR, ASA)
  - Dell Force10
  - Foundry
  - Juniper (JunOS, Firewall)
  - MRV
  - Quagga
  - Quanta
  - VyOS

Report an issue if your device is not on the list, and we'll try to support it. Batfish does not parse all possible configuration directives. If Batfish fails to parse your files, report an issue and we'll try to fix. Or, you can :)
