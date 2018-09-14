## Example network

This contains an example network for a school campus. There are three snapshots for this network which can be used to run various features in Batfish.
1. live: The snapshot of the device configurations currently deployed in this network.
2. live-with-bgp-announcements: This is the same as the live snapshot but also contains some bgp announcements received from external networks. 
2. candidate: The snapshot of the device configurations intended to be deployed in the future.

A deliberate change was introduced in the candidate snapshot to excercise the diffing capabilities of Batfish:
	In `as2dept1` wrong access-list was applied in the out direction.

Topology of the network in the three snapshots is the same and can be seen in **example-network.png** (the image is just for illustration purposes).
