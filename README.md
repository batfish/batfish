
**Got questions, feedback, or feature requests? Join our community on [Slack!](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTUxOTJlY2YyNTVlNGQ3MTJkOTIwZTU2YjY3YzRjZWFiYzE4ODE5ODZiNjA4NGI5NTJhZmU2ZTllOTMwZDhjMzA)**

[![codecov](https://codecov.io/gh/batfish/batfish/branch/master/graph/badge.svg)](https://codecov.io/gh/batfish/batfish)

## What is Batfish?

Batfish is a network configuration analysis tool that can find bugs and guarantee the correctness of (planned or current) network configurations. It enables network engineers to rapidly and safely evolve their network, without fear of outages or security breaches. See [www.batfish.org](http://www.batfish.org) for technical information on how it works.

**Batfish does not require access to network devices.** It can work with configuration files pulled by tools like RANCID or those generated via templates. ![Analytics](https://ga-beacon.appspot.com/UA-100596389-3/open-source/batfish?pixel&useReferer)

## How do I get started?

The quickest way to get started is using one of the pre-built [Docker containers](https://github.com/batfish/docker/).

Or, you can build from sources, the instructions for which are on the [Batfish Wiki](https://github.com/batfish/batfish/wiki).

Batfish is also available as a commercially-supported service by [Intentionet](https://www.intentionet.com). Drop a line to [info@intentionet.com](mailto:info@intentionet.com) for more information.

## What kinds of correctness checks does Batfish support?

Batfish supports many types of correctness checks, including

1. ##### Compliance and best-practices guidelines, e.g.:
  - Flag undefined-but-referenced or defined-but-unreferenced structures (e.g., ACLs, route maps)
  - Configuration settings for MTUs, AAA, NTP, logging, etc. are correct
  - Devices can only be accessed using SSHv2 and password is not null

2. ##### Consistency of configuration across two or more devices, e.g.:
  - BGP sessions and IPSec tunnels are compatibly configured across neighbors
  - All interface IP addresses are unique
  - Identically-named structures (e.g., ACLs, route maps) across devices have identical functionality

3. ##### Checks on data flow, e.g.:
  - Sensitive services can be reached only from specific subnets or devices
  - Certain services (e.g., DNS) are globally reachable and not blocked by any ACL in the network
  - Paths between endpoints are as expected (e.g., traverse a firewall)

4. ##### Fault-tolerance, e.g.:
  - End-to-end reachability is not impacted for *any* flow after *any* single-link or -device failure
  - Traffic correctly fails over after a failure

5. ##### "Differential" analysis of two sets of configuration, e.g.:
  - End-to-end reachability is identical across new and old configurations
  - Planned ACL or firewall changes are provably correct, including not causing collateral damage for other traffic
  - Two configurations, potentially from different vendors, are functionally equivalent

## What configuration formats does Batfish support?

Batfish supports configurations for a large and growing set of (physical and virtual) devices, including:
  - Arista
  - Aruba
  - AWS VPCs
  - Cisco (All Cisco NX-OS, IOS, IOS-XR, IOS-XR devices including Cisco ASA)
  - Dell Force10
  - Foundry
  - iptables (on hosts)
  - Juniper (All JunOS plaforms: MX, EX, QFX, SRX)
  - MRV
  - Palo Alto Networks
  - Quagga
  - Quanta
  - VyOS

If you'd like support for additional vendors or currently-unsupported configuration features, let us know via [Slack](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTUxOTJlY2YyNTVlNGQ3MTJkOTIwZTU2YjY3YzRjZWFiYzE4ODE5ODZiNjA4NGI5NTJhZmU2ZTllOTMwZDhjMzA)or [GitHub](https://github.com/batfish/batfish/issues/new). We'll try to add support. Or, you can -- we welcome pull requests! :)
