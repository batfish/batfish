# Auxiliary files

This folder contains files that can be packaged in a snapshot along with configuration files. 
These files can help inform Batfish of the network structure, both physical (e.g., Layer1 wiring) 
and logical (e.g., device roles).

Below are descriptions of supported auxiliary files

## Layer 1 topology
* Purpose: inform Batifsh of the physical structure of the network to help accurately infer the Layer 3 topology
* Packaging: `layer1_topology.json` file, as a sibling to the `configs` folder
* Exampe format: [`example_layer1_topology.json`](example_layer1_topology.json)
