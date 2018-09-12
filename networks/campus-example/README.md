## Example campus network

There are two snapshots of the example campus network which can be used to run various features in Batfish.
1. example
2. example-with-delta

Two deliberate differences were introduced in the two snapshots to excercise the diffing capabilities of Batfish.
1. In `as2dept1` wrong access-list was applied in the out direction.
2. `example-with-delta` contains some bgp announcements received from external networks.

The topology of the two networks is same and can be seen in **example-network.png** (the image is just for illustration purposes).
