# Cisco FTD-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Cisco Firepower Threat Defense (FTD) configurations in Batfish.

## FTD Configuration Structure

Cisco FTD configurations share similarities with Cisco ASA but have several unique characteristics:

1. **Stanza-Based Configuration**: FTD uses a stanza-based configuration model where related commands are grouped under hierarchical stanzas
2. **Mixed Command Styles**: Supports both ASA-like subcommand style and newer object-oriented configuration
3. **Cryptochecksum Line**: A special header line that appears at the start of the configuration

## FTD Grammar Structure

The FTD grammar is split into several files:

- `FtdLexer.g4`: Defines tokens for the lexer
- `FtdParser.g4`: Main parser file containing the start rule (`ftd_configuration`)
- `Ftd_common.g4`: Common grammar rules used by other parser files
- `Ftd_acl.g4`: Access Control List-specific grammar
- `Ftd_bgp.g4`: BGP-specific grammar
- `Ftd_crypto.g4`: Crypto (IPsec/IKEv2) specific grammar
- `Ftd_failover.g4`: High availability failover configuration
- `Ftd_interface.g4`: Interface configuration
- `Ftd_nat.g4`: NAT (Network Address Translation) grammar
- `Ftd_object.g4`: Object definitions (network objects, service objects)
- `Ftd_ospf.g4`: OSPF routing protocol grammar
- `Ftd_route.g4`: Static route configuration
- `Ftd_tunnel_group.g4`: VPN tunnel group configuration

## Code Organization

Cisco FTD uses the vendor-scoped code organization pattern:

```
projects/batfish/src/main/
├── antlr4/org/batfish/vendor/cisco_ftd/grammar/   # ANTLR .g4 files
├── java/org/batfish/vendor/cisco_ftd/
│   ├── grammar/                                   # Parser, extractor, base lexer
│   └── representation/                            # FTD-specific data model
└── test/java/org/batfish/vendor_cisco_ftd/         # All tests
```

## Common FTD Parsing Patterns

### Stanza-Based Configuration

FTD configurations use a stanza-based approach where commands are nested under configuration headers. The grammar uses a hierarchical `stanza` rule that can contain nested stanzas:

```
interface GigabitEthernet0/0
  nameif inside
  security-level 100
  ip address 10.1.1.1 255.255.255.0
```

### Object-Based Configuration

FTD uses named objects that can be referenced in multiple places:

```
object network INTERNAL_SERVER
  host 192.168.1.10
object service SSH
  service tcp source eq ssh
```

These are extracted into `FtdNetworkObject` and `FtdServiceObjectGroup` representations.

### NAT Configuration

FTD NAT configuration uses a unique syntax with auto-NAT rules and manual NAT rules:

```
nat (inside,outside) source dynamic INTERNAL INTERFACE interface
nat (any,out) source static ANY_ANY ACCESS_ACL
```

### Crypto Configuration

FTD uses crypto maps and tunnel groups for VPN configuration:

```
crypto map CMAP 10 match address CRYPTO_ACL
crypto map CMAP 10 set peer 1.2.3.4
crypto ikev2 policy 10 encryption aes-256 integrity sha256
tunnel-group 1.2.3.4 type ipsec-l2l
```

## Special Considerations

### Cryptochecksum Line

FTD configurations begin with a cryptochecksum line that must be parsed as a single token:

```
: checksum 0x8a7b6c5d 0x1234 0x5678
```

This is handled by the `CRYPTOCHECKSUM` lexer rule.

### Name Resolution

FTD supports name-to-address mappings via the `names` command:

```
name 192.168.1.1 SERVER1
```

These are tracked in the `FtdConfiguration` and resolved during extraction.

### Version Detection

FTD configuration format is detected by looking for the cryptochecksum line or FTD-specific keywords.

## Implementation Status

### Fully Supported Features

- Interface configuration (nameif, security-level, IP addresses)
- Named network objects and object groups
- Named service objects and object groups
- Access control lists (extended and object-group based)
- Static routes
- BGP configuration
- OSPF configuration
- NAT rules (auto and manual NAT)
- Crypto maps and IKEv2 policies
- Tunnel groups for site-to-site VPN

### Partially Supported Features

- Some advanced crypto options may have warnings
- Complex multi-context configurations

### Not Yet Supported

- Some advanced inspection policies
- FXOS chassis-level configuration (for Firepower 4100/9300 series)

## Testing

FTD tests are located in `projects/batfish/src/test/java/org/batfish/vendor/cisco_ftd/`:

- `FtdGrammarTest.java`: General grammar and parsing tests
- `FtdAccessListTest.java`: ACL-specific tests
- `FtdBgpTest.java`: BGP configuration tests
- `FtdNatTest.java`: NAT configuration tests
- `FtdOspfTest.java`: OSPF configuration tests
- `FtdVpnTest.java`: VPN/crypto tests
- `FtdRealisticConfigTest.java`: Tests with realistic configuration snippets

Test configurations are in `projects/batfish/src/test/resources/org/batfish/vendor/cisco_ftd/`.

## References

- [Parsing Documentation](../README.md)
- [Implementation Guide](../implementation_guide.md)
- [Cisco NX-OS Vendor Code](../../projects/batfish/src/main/java/org/batfish/vendor/cisco_nxos/) - Reference implementation for vendor-scoped pattern
