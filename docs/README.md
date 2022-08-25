# :warning: This is a guide for Batfish developers :warning:

**If you are interested in trying out Batfish on your network, check out
our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html)
instead.**

***

# Batfish developer documentation

This document is aimed at developers interested in understanding and improving Batfish.

As a first step before attempting to modify Batfish code, you should read this entire document to
gain a rough understanding of Batfish's processing stages. This will enable you to identify which
modules you will need to modify to make your intended change. Then you should follow the links for
the detailed documentation on developing each such module. Once you are ready to proceed, set up
your [development environment](#setting-up-your-environment-for-batfish-development) using the
provided instructions, and you should be ready to go.

If you get stuck after reading all relevant documentation, you can ask questions on the
[Batfish Slack.](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTcyYzY3M2Q0NWUyYTRhYjdlM2IzYzRhZGU1NWFlNGU2MzlhNDY3OTJmMDIyMjQzYmRlNjhkMTRjNWIwNTUwNTQ)

## Pipeline overview

User interaction with Batfish can be roughly divided into two classes of operations:

* [Uploading and processing information about the network](#uploading-and-processing-information-about-a-network)
* [Asking questions about the network](#asking-questions-about-the-network)

### Uploading and processing information about a network

Batfish consumes information about a network in the form of
a snapshot.

See:

* [Uploading configurations](https://pybatfish.readthedocs.io/en/latest/notebooks/interacting.html#Uploading-configurations)
* [Format of vendor and supplemental data](https://pybatfish.readthedocs.io/en/latest/formats.html)

Initialization of an uploaded snapshot occurs in the following stages. For detailed information on
a stage's purpose, inputs, outputs, and how to modify it, click on its name.

1. [Parsing](parsing/README.md)
    * Process vendor data files into parse trees
2. [Extraction](extraction/README.md)
    * Extract data from parse trees into vendor-specific (VS) configuration classes
3. [Conversion](conversion/README.md)
    * Convert vendor-specific (VS) configuration classes into unified vendor-independent (VI)
      configuration classes
4. [Post-processing](post_processing/README.md)
    * Finalize IGP costs and interface up status
    * Uses data from earlier stages and optional wiring information from the uploaded snapshot.
5. [Data plane generation](data_plane/README.md)
    * Compute RIBs; FIBs; L1-L3, overlay, and application topologies
6. [Forwarding analysis](forwarding_analysis/README.md)
    * Compute ARP behavior for each interface
    * Compute forwarding behavior for each VRF and ingress interface

### Asking questions about the network

The types of questions you can ask about a network are
detailed [here](https://pybatfish.readthedocs.io/en/latest/questions.html).

See the [question development doc](question_development/README.md) for details on:

* how the question pipeline works
* how to add and modify questions

## Setting up your environment for Batfish development

At minimum, to develop in Bafish you will need to follow the
[building and running](building_and_running/README.md) steps. That document explains how to:

* build batfish
* run batfish
* run tests

For a smoother experience, we recommend you use Intellij IDEA, which you should set up
according to these [instructions](intellij_setup/README.md).


