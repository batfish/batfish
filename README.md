
**Got questions, feedback, or feature requests? Join our community on [Slack!](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTUxOTJlY2YyNTVlNGQ3MTJkOTIwZTU2YjY3YzRjZWFiYzE4ODE5ODZiNjA4NGI5NTJhZmU2ZTllOTMwZDhjMzA)**

[![codecov](https://codecov.io/gh/batfish/batfish/branch/master/graph/badge.svg)](https://codecov.io/gh/batfish/batfish)

## What is Batfish?

Batfish is a network validation tool that provides correctness guarantees for security, reliability, and compliance by analyzing the configuration of network devices. It builds complete models of network behavior from device configurations and finds violations of network policies (built-in, user-defined, and best-practices).

A primary use case for Batfish is to validate configuration changes *before* deployment (though it can be used to validate deployed configurations as well). Pre-deployment validation is a critical gap in existing network automation workflows. By including Batfish in automation workflows, network engineers can close this gap and ensure that only correct changes are deployed.

**Batfish does NOT require direct access to network devices.** The core analysis requires only the configuration of network devices. This analysis may be enhanced using additional information from the network such as:
* BGP routes received from external peers
* Topology information represented by LLDP/CDP

See [www.batfish.org](http://www.batfish.org) for technical information on how it works. ![Analytics](https://ga-beacon.appspot.com/UA-100596389-3/open-source/batfish?pixel&useReferer)

## What kinds of correctness checks does Batfish support?

[<img src=batfish_video.png width=370>](https://www.youtube.com/channel/UCA-OUW_3IOt9U_s60KvmJYA/videos)
[<img src=batfish_notebook.png width=470>](https://github.com/batfish/pybatfish/tree/master/jupyter_notebooks)

The [Batfish YouTube channel](https://www.youtube.com/channel/UCA-OUW_3IOt9U_s60KvmJYA/videos) (subscribe!) and [Python notebooks](https://github.com/batfish/pybatfish/tree/master/jupyter_notebooks) illustrate many checks. Batfish checks span a range of network behaviors.
#### Configuration Compliance
* Flag undefined-but-referenced or defined-but-unreferenced structures (e.g., ACLs, route maps)
* Configuration settings for MTUs, AAA, NTP, logging, etc. match templates
* Devices can only be accessed using SSHv2 and password is not null
#### Reliability
* End-to-end reachability is not impacted for any flow after any single-link or single-device failure
* Certain services (e.g., DNS) are globally reachable
#### Security
* Sensitive services can be reached only from specific subnets or devices
* Paths between endpoints are as expected (e.g., traverse a firewall, have at least 2 way ECMP, etc...)
#### Change Analysis
* End-to-end reachability is identical across the current and a planned configuration
* Planned ACL or firewall changes are provably correct and causes no collateral damage for other traffic
* Two configurations, potentially from different vendors, are functionally equivalent



## How do I get started?

#### 1. Run the Batfish service
Getting started with Batfish is easy. Just pull and run the latest `allinone` Docker container that includes Batfish as well as example Jupyter notebooks. 

    docker pull batfish/allinone
   
    docker run -v batfish-data:/data -p 8888:8888 -p 9997:9997 -p 9996:9996 batfish/allinone

The second command starts the Batfish service and maps the necessary TCP ports. 

#### 2. Browse example notebooks (optional)

If you are new to Batfish, consider walking through our notebooks which highlight different capabilities and use cases of Batfish. Point your browser to [http://localhost:8888](http://localhost:8888), and in the `Password or token:` prompt, enter the token that Jupyter showed when you ran the container (e.g. **token=abcdef123456...**). 

Jupyter will show you the list of available notebooks. "Getting Started with Batfish" is a good one to start with. This [README](https://github.com/batfish/pybatfish/tree/master/jupyter_notebooks) explains what each notebook does. 


#### 3. Install Pybatfish

To analyze your network configurations, you also need [Pybatfish](https://www.github.com/batfish/pybatfish), a Python SDK to interact with the Batfish service. Though not strictly necessary, we recommend that you install Pybatfish in a [Python 3 virtual environment](https://docs.python.org/3/library/venv.html). 

To install Pybatfish run the following commands (in a virtual environment if applicable):

    python -m pip install --upgrade git+https://github.com/batfish/pybatfish.git

#### 4. Develop your analysis 

Afer installing Pybatfish, use your Python environment of choice (e.g., PyCharm, interactive Python shell, Jupyter, ..) to interact with Batfish. The [notebooks](https://github.com/batfish/pybatfish/tree/master/jupyter_notebooks) provide examples of such scripts. 

See complete documentation of Pybatfish on [readthedocs](https://pybatfish.readthedocs.io/en/latest/quickstart.html).


## System Requirements for running Batfish

Batfish can be run on any operating system that supports Docker. The containers are actively tested on Mac OS X and Ubuntu 16.04 LTS.

To get started with the example Jupyter notebooks, all you need is a reasonably capable laptop:

* Dual core CPU
* 8 GB RAM
* 256 GB hard-drive

When you transition to running Batfish on your own network, we recommend a server that at least has:

* Quad-core CPU with 2 threads per CPU
* 32 GB RAM
* 256 GB hard-drive


## Supported Network Device and Operating System List

Batfish supports configurations for a large and growing set of (physical and virtual) devices, including:

* Arista
* Aruba
* AWS (VPCs, Network ACLs, VPN GW, NAT GW, Internet GW, Security Groups, etc…)
* Cisco (All Cisco NX-OS, IOS, IOS-XE, IOS-XR and ASA devices)
* Dell Force10
* F5 BIG-IP
* Foundry
* iptables (on hosts)
* Juniper (All JunOS platforms: MX, EX, QFX, SRX, T-series, PTX)
* MRV
* Palo Alto Networks
* Quagga / FRR
* Quanta
* VyOS

If you'd like support for additional vendors or currently-unsupported configuration features, let us know via [Slack](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTUxOTJlY2YyNTVlNGQ3MTJkOTIwZTU2YjY3YzRjZWFiYzE4ODE5ODZiNjA4NGI5NTJhZmU2ZTllOTMwZDhjMzA) or [GitHub](https://github.com/batfish/batfish/issues/new). We'll try to add support. Or, you can -- we welcome pull requests! :)

## License and Dependencies

Batfish is released under The Apache Software License, Version 2.0. All
third-party dependencies are compatible with this licensing. A full list of
dependencies and their licenses can be generated by running
`mvn -f projects license:aggregate-add-third-party`.
