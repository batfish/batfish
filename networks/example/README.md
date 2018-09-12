## Example network

This contains an example network for a school campus. There are two snapshots for this network which can be used to run various features in Batfish.
1. live: The snapshot of the device configurations currently deployed in this network.
2. candidate: The snapshot of the device configurations intended to be deployed in the future.

Two deliberate differences were introduced in the two snapshots to excercise the diffing capabilities of Batfish.
1. In `as2dept1` wrong access-list was applied in the out direction.
2. The `candidate` snapshot contains some bgp announcements received from external networks.

Topology of the network in the two snapshots is the same and can be seen in **example-network.png** (the image is just for illustration purposes).
