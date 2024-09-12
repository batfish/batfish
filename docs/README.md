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
    * _What it does_:
        * Process vendor data files into parse trees.
    * _When to modify_:
        * You want to add support for a new network device.
        * You want Batfish to silently ignore some syntax it is reporting as unrecognized
        * You want to add full pipeline support for a new feature whose syntax Batfish reports as
          unrecognized, such that the changes will eventually be reflected in the output of one or
          more questions.
        * Batfish is improperly throwing away config text it already recognizes near an unrecognized
          construct.

2. [Extraction](extraction/README.md)
    * _What it does_:
        * Extract data from parse trees into vendor-specific (VS) configuration classes.
    * _When to modify_:
        * You want to add support for a new network device.
        * You added support for new syntax in the parser, and now you need to extract data from the
          resulting parse tree into the VS model so Batfish can use it.
        * You modified the VS model, and need to update what gets populated using the existing data
          in the parse tree.

3. [Conversion](conversion/README.md)
    * _What it does_:
        * Convert vendor-specific (VS) configuration classes into unified vendor-independent (VI)
          configuration classes
    * _When to modify_:
        * You want to add support for a new network device.
        * You modified either the VS or VI model, and need to ensure the correct VI data gets
          populated.
        * You want Batfish to use some existing data in the VS model that never makes it to the VI
          model, i.e. complete a partial implementation.

4. [Post-processing](post_processing/README.md)
    * _What it does_:
        * Finalize IGP costs and interface up status
        * Perform a best-effort cleanup of VI structures broken due to conversion bugs, and warn
          when changes are made. For example, post-processing may remove undefined references in the
          VI model that were created by faulty conversion code.
        * Uses data from earlier stages and optional wiring information from the uploaded snapshot.
    * _When to modify_:
        * You want to fix a bug in the calculation of final IGP costs or interface up status not
          caused by a bug in conversion.
        * You want to add a sanity check / cleanup step for VI structures.
        * You want to add some modification of VI structures that is *only* possible after *all*
          conversion jobs have completed.
5. [Data plane generation](data_plane/README.md)
    * _What it does_:
        * Compute RIBs; FIBs; L1-L3, overlay, and application topologies
    * _When to modify_:
        * You want to add or modify support for a routing protocol in order to change the routes
          Batfish produces, e.g. adding a new BGP add-path selection criterion.
        * You want to change what FIB entries are produced from the RIBs.
6. [Forwarding analysis](forwarding_analysis/README.md)
    * _What it does_:
        * Compute ARP behavior for each interface
        * Compute forwarding behavior for each VRF and ingress interface
    * _When to modify_:
        * **N.B. the vast majority of changes to other pipeline stages do not necessitate changes
          to forwarding analysis. Furthermore, changes to forwarding analysis tend to necessitate
          changes to both the traceroute and BDD reachability engines. Make sure you have a deep
          understanding of all of these components before making modifications here.**
        * You made changes to the ARP or forwarding data model that are not being properly
          reflected in the output of traceroute or reachability type questions, e.g. a hop in a
          trace is going to the wrong set of neighbors.

### Asking questions about the network

The types of questions you can ask about a network are
detailed [here](https://pybatfish.readthedocs.io/en/latest/questions.html).

See the [question development doc](question_development/README.md) for details on:

* how the question pipeline works
* how to add and modify questions

## Symbolic analysis engine
Batfish includes several questions based on _symbolic analysis_, which can efficiently 
reason about the behavior (very) large sets of packets. More information about the symbolic analysis engine 
and the questions implemented on top of it is available [here](symbolic_engine/README.md).

## Setting up your environment for Batfish development

At minimum, to develop in Bafish you will need to follow the
[building and running](building_and_running/README.md) steps. That document explains how to:

* build batfish
* run batfish
* run tests
* build a batfish docker image you can deploy in your network

For a smoother experience, we recommend you use Intellij IDEA, which you should set up
according to these [instructions](intellij_setup/README.md).

If you are going to to contribute code to Batfish, also read the
[contributing guide](contributing/README.md).