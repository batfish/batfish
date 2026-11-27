# FortiOS VDOM static routes

For a multi-VDOM FortiGate, use a full configuration backup so Batfish can read
both the global interface definitions and the VDOM-local routes. FortiOS places
the global configuration under `config global` and the per-VDOM configuration
under `config vdom`. See Fortinet's [multi-VDOM backup guide](https://docs.fortinet.com/document/fortigate/7.6.6/administration-guide/87472/backing-up-and-restoring-configurations-in-multi-vdom-mode).

Batfish scopes `config router static` entries by VDOM and sequence number.
Top-level entries and interfaces with no explicit VDOM use `root`. A supported
route with an outgoing interface is installed in that interface's VRF, provided
the interface belongs to the route's VDOM. Batfish models IPv4 static routes
inside VDOM blocks and records parse warnings for unsupported routing and
firewall sections there.
