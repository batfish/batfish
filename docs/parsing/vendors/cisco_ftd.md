# Cisco Firepower Threat Defense (FTD) Parsing Support

## Overview
Batfish support for Cisco FTD is currently in active development. While it shares architectural similarities with Cisco ASA, FTD is implemented as a distinct vendor module with its own grammar and control plane extraction logic.

## Feature Support Matrix

| Feature Category   | Feature                | Parsing | VI Conversion | Notes                                                                            |
| :----------------- | :--------------------- | :-----: | :-----------: | :------------------------------------------------------------------------------- |
| **Interfaces**     | Physical/Subinterfaces |    ✅    |       ✅       | Ethernet/Port-channel/Management parsing, admin state, `nameif`, IPs.            |
|                    | Security Levels        |    ✅    |       ✅       | Parsed. `nameif` mapped to Batfish Zones; default allow/deny when no ACL.        |
| **Routing**        | Static Routes          |    ✅    |       ✅       | Standard static routes supported.                                                |
|                    | OSPFv2                 |    ✅    |       ✅       | Process ID, Router ID, Areas, Network statements, Passive Interfaces.            |
|                    | BGP                    |    ✅    |       ✅       | Basic `router bgp`, router-id, neighbor remote-as, v4 AF activation.             |
| **Access Control** | Extended/Advanced ACLs |    ✅    |       ✅       | `permit`/`deny`, protocol, src/dst IPs, ports. *Time-ranges parsed but ignored.* |
|                    | Service Object Groups  |    ✅    |       ✅       | `object-group service` with `service-object`/`port-object` used in ACL ports.    |
|                    | Network Objects        |    ✅    |       ✅       | `object network`, `object-group network` (host, subnet, nested groups).          |
|                    | Access Groups          |    ✅    |       ✅       | Binding ACLs to interfaces (`in`/`out`).                                         |
| **NAT**            | Auto NAT               |    ✅    |       ✅       | Source/dest/static/dynamic NAT converted to transformations.                     |
|                    | Manual NAT (Twice NAT) |    ✅    |       ⚠️       | Basic before/after-auto twice-NAT conversion for static mappings.               |
| **VPN**            | Site-to-Site (IPsec)   |    ✅    |       ⚠️       | `crypto map`, `crypto ipsec`, `crypto ikev2`, `tunnel-group` parsed; partial VI. |
|                    | Remote Access          |    ❌    |       ❌       | AnyConnect/SSL VPN not implemented.                                              |
| **Policy**         | Service Policy (MPF)   |    ✅    |       ✅       | Class-map/policy-map syntax converted to synthetic ACLs.                         |

## Gap Analysis & Roadmap

To achieve "Complete" basic FTD support, the following roadmap is recommended:

### Phase 1: NAT & Connectivity (Completed)
1.  **NAT Conversion Logic**:
    *   ✅ Implement `FtdConfiguration.toVendorIndependent` logic to convert `FtdNatRule` instances into Batfish `Trace` transformations (Source NAT, Dest NAT).
    *   ✅ Handle recursive object lookups for NAT address pools.
2.  **Security Zones**:
    *   ✅ Map `nameif` (e.g., "inside", "outside") to Batfish `Zone` constructs.
    *   ✅ Convert Interface Security Levels (0-100) into basic default permit/deny logic if explicit ACLs are missing.

### Phase 2: Advanced Routing & Policy (Medium Priority)
3.  **BGP Support**:
    *   ✅ Basic parsing and conversion for router-id and neighbors.
    *   Extend AFI/SAFI, timers, and route-policy support.
4.  **Service Policy (MPF)**:
    *   ✅ Extract `policy-map`, `class-map`, and `service-policy` with references.
    *   ✅ Convert basic MPF matching logic to synthetic Batfish ACLs.
    *   ✅ Capture per-class `inspect`/`set`/`parameters` lines in the vendor model (not yet used in VI conversion).

### Phase 3: VPN & Remote Access (Low Priority)
5.  **IPsec VPN**:
    *   ✅ Parse `crypto map`, `crypto ikev2`, `tunnel-group`.
    *   ⚠️ Convert phase2 proposals/policies and IKE keys; no interface binding yet.
    *   Model VPN tunnels as point-to-point interfaces in Batfish topology.


## Code Structure

### Key Classes

*   **Parser**: `org.batfish.grammar.cisco_ftd.FtdCombinedParser`
*   **Extractor**: `org.batfish.grammar.cisco_ftd.FtdControlPlaneExtractor`
    *   Contains the logic for walking the parse tree and populating the configuration object.
*   **Configuration Model**: `org.batfish.representation.cisco_ftd.FtdConfiguration`
    *   Specific Vendor Specifc (VS) model for FTD.
    *   Holds lists of `Interface`, `FtdRoute`, `FtdAccessList`, etc.
*   **VI Conversion**: Logic within `FtdConfiguration.toVendorIndependentConfigurations()` converts the VS model to the main Batfish `Configuration` model.

## Testing

Tests are located in `projects/batfish/src/test/java/org/batfish/grammar/cisco_ftd/`.

*   **`FtdGrammarTest.java`**: Main entry point for grammar and extraction tests. Uses `testconfigs/` resources.
*   **`FtdOspfTest.java`**: Focused tests for OSPF routing logic.
*   **`FtdBgpTest.java`**: Tests for BGP process and neighbor configuration.
*   **`FtdAccessListTest.java`**: Detailed tests for ACL matching, object expansion, and port ranges.
*   **`FtdNatTest.java`**: Tests for NAT rule parsing and conversion.
*   **`FtdZoneTest.java`**: Tests for Security Zone creation and interface assignment.
*   **`FtdNetworkObjectGroupTest.java`**: Comprehensive tests for network object groups, nested groups, and object references.

To run all FTD tests:
```bash
bazel test //projects/batfish/src/test/java/org/batfish/grammar/cisco_ftd/...
```
