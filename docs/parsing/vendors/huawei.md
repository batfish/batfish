# Huawei VRP ANTLR4 Grammar Files

This directory contains the ANTLR4 grammar files for parsing Huawei VRP (Versatile Routing Platform) network device configurations.

## Files Created

### Core Grammar Files

1. **HuaweiLexer.g4** - Token definitions for Huawei VRP keywords
   - Defines all Huawei-specific keywords (SYSNAME, INTERFACE, RETURN, etc.)
   - Number types (UINT8, UINT16, UINT32, DEC, FLOAT)
   - IP address and prefix patterns
   - Special characters and operators

2. **HuaweiParser.g4** - Main parser with entry point
   - `huawei_configuration` - Top-level rule
   - Imports all module grammars
   - Defines `s_stanza` for top-level configuration blocks
   - Supports `RETURN` statement for mode exits

3. **Huawei_common.g4** - Shared types and utility rules
   - Common types: IP addresses, prefixes, numbers
   - `interface_name` - Parses interface names (GigabitEthernet, Vlanif, LoopBack, Eth-Trunk)
   - `description_line` - Description statements
   - `null_rest_of_line` - Consumes remaining line (for ignored commands)

### Module Grammar Files

4. **Huawei_system.g4** - System-level configuration
   - `s_sysname` - Sets device hostname
   - Basic system configuration support

5. **Huawei_interface.g4** - Interface configuration
   - Supports interface types:
     - GigabitEthernet (e.g., GigabitEthernet0/0/0)
     - Vlanif (e.g., Vlanif100)
     - LoopBack (e.g., LoopBack0)
     - Eth-Trunk (e.g., Eth-Trunk1)
   - Interface commands:
     - `description` - Interface description
     - `ip address` - IP address assignment
     - `shutdown` / `undo shutdown` - Administrative state

6. **Huawei_vlan.g4** - VLAN configuration (stub)
   - `vlan` - Single VLAN configuration
   - `vlan batch` - Multiple VLAN creation
   - Phase 1: Parses but ignores VLAN sub-commands

7. **Huawei_bgp.g4** - BGP protocol (stub)
   - BGP process configuration
   - Phase 1: Parses but ignores BGP sub-commands

8. **Huawei_ospf.g4** - OSPF protocol (stub)
   - OSPF process configuration
   - Phase 1: Parses but ignores OSPF sub-commands

9. **Huawei_static.g4** - Static routes
   - `ip route-static` - Static route configuration
   - Supports:
     - `ip route-static X.X.X.X Y.Y.Y.Y Z.Z.Z.Z` (destination/mask/next-hop)
     - `ip route-static X.X.X.X/M Z.Z.Z.Z` (prefix/next-hop)
     - Optional preference and track parameters

10. **Huawei_acl.g4** - Access control lists (stub)
    - Basic and advanced ACL support
    - Numbered and named ACLs
    - Phase 1: Parses but ignores ACL rules

11. **Huawei_nat.g4** - NAT configuration (stub)
    - NAT configuration mode
    - Phase 1: Parses but ignores NAT commands

12. **Huawei_vrf.g4** - VRF configuration (stub)
    - `ip vpn-instance` - VRF definition (Huawei's VRF term)
    - Phase 1: Parses but ignores VRF sub-commands

13. **Huawei_ignored.g4** - Commands to parse but ignore
    - Catches unsupported or unknown commands
    - Prevents parse failures during Phase 1

## Phase 1 Capabilities

The grammar can parse a minimal Huawei configuration like:

```
sysname Router1
interface GigabitEthernet0/0/0
 description Uplink to core
 ip address 192.168.1.1 255.255.255.0
return
interface Vlanif100
 description Management VLAN
 ip address 10.0.0.1 255.255.255.0
return
vlan batch 10,20,30-40
ip route-static 0.0.0.0 0.0.0.0 192.168.1.254
return
```

## Key Huawei VRP Syntax Differences from Cisco

1. **Hierarchy**: Uses `sysname` instead of `hostname`
2. **Interface naming**: Uses different format (GigabitEthernet0/0/0 vs GigabitEthernet0/0)
3. **Negation**: Uses `undo` instead of `no`
4. **Exit**: Uses `return` to exit configuration modes
5. **VRF**: Uses `vpn-instance` instead of `vrf definition`
6. **Batch operations**: Supports `vlan batch` for creating multiple VLANs

## Structure Follows Cisco Grammar Patterns

The Huawei grammar follows the same modular structure as the Batfish Cisco grammar:
- Lexer with keyword definitions
- Main parser importing module grammars
- Common types in shared grammar file
- Separate grammar files per feature/protocol
- Stub implementations for features not yet implemented

## Next Steps (Future Phases)

- Expand interface support (more sub-commands)
- Implement BGP configuration parsing
- Implement OSPF configuration parsing
- Expand ACL rule parsing
- Add NAT configuration support
- Implement VRF/L3VPN support
- Add protocol-specific features (MPLS, RSVP-TE, etc.)
- Add service configuration (DHCP, NAT, etc.)
