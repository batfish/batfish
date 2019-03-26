parser grammar CumulusNcluParser;

/* This is only needed if parser grammar is spread across files */
import
CumulusNclu_common, CumulusNclu_bgp, CumulusNclu_interface, CumulusNclu_routing;

options {
  superClass =
  'org.batfish.grammar.cumulus_nclu.parsing.CumulusNcluBaseParser';
  tokenVocab = CumulusNcluLexer;
}

// goal rule

cumulus_nclu_configuration
:
  NEWLINE? statement+ NEWLINE? EOF
;

// other rules

statement
:
  s_extra_configuration
  | s_net_add
  | s_net_add_unrecognized
  | s_null
;

s_extra_configuration
:
  EXTRA_CONFIGURATION_HEADER text = EXTRA_CONFIGURATION
  EXTRA_CONFIGURATION_FOOTER NEWLINE
;

s_net_add
:
  NET ADD
  (
    a_bgp
    | a_bond
    | a_bridge
    | a_dns
    | a_hostname
    | a_interface
    | a_loopback
    | a_routing
    | a_time
    | a_vlan
    | a_vrf
    | a_vxlan
  )
;

a_bga_bond
:
  BOND name = word
  (
    bond_bond
    | bond_bridge
    | bond_clag
  )
;

bond_bond
:
  BOND bobo_slaves
;

bobo_slaves
:
  SLAVES slaves = glob NEWLINE
;

bond_bridge
:
  BRIDGE bob_access
;

bob_access
:
  ACCESS vlan = vlan_id NEWLINE
;

bond_clag
:
  CLAG boc_id
;

boc_id
:
  ID id = uint16 NEWLINE
;

a_bridge
:
  BRIDGE bridge_bridge
;

bridge_bridge
:
  BRIDGE
  (
    brbr_ports
    | brbr_vids
    | brbr_vlan_aware
  )
;

brbr_ports
:
  PORTS ports = glob NEWLINE
;

brbr_vids
:
  VIDS ids = range NEWLINE
;

brbr_vlan_aware
:
  VLAN_AWARE NEWLINE
;

a_dns
:
  DNS dns_nameserver
;

dns_nameserver
:
  NAMESERVER
  (
    dn4
    | dn6
  )
;

dn4
:
  IPV4 address = IP_ADDRESS NEWLINE
;

dn6
:
  IPV6 address6 = IPV6_ADDRESS NEWLINE
;

a_hostname
:
  HOSTNAME hostname = word NEWLINE
;

a_loopback
:
  LOOPBACK LO
  (
    l_clag
    | l_ip_address
  )
;

l_clag
:
  CLAG lc_vxlan_anycast_ip
;

lc_vxlan_anycast_ip
:
  VXLAN_ANYCAST_IP ip = IP_ADDRESS NEWLINE
;

l_ip_address
:
  IP ADDRESS address = IP_PREFIX NEWLINE
;

a_time
:
  TIME
  (
    t_ntp
    | t_zone
  )
;

t_ntp
:
  NTP
  (
    tn_server
    | tn_source
  )
;

tn_server
:
  SERVER server = word NEWLINE
;

tn_source
:
  SOURCE source = word NEWLINE
;

t_zone
:
  ZONE zone = word NEWLINE
;

a_vlan
:
  VLAN id = vlan_id
  (
    v_ip_address
    | v_ip_address_virtual
    | v_vlan_id
    | v_vlan_raw_device
    | v_vrf
  )
;

v_ip_address
:
  IP ADDRESS address = IP_PREFIX NEWLINE
;

v_ip_address_virtual
:
  IP ADDRESS_VIRTUAL mac = MAC_ADDRESS address = IP_PREFIX NEWLINE
;

v_vlan_id
:
  VLAN_ID id = vlan_id NEWLINE
;

v_vlan_raw_device
:
  VLAN_RAW_DEVICE device = BRIDGE NEWLINE
;

v_vrf
:
  VRF name = word NEWLINE
;

a_vrf
:
  VRF name = glob
  (
    vrf_vni
    | vrf_vrf_table
  )
;

vrf_vni
:
  VNI vni = vni_number NEWLINE
;

vrf_vrf_table
:
  VRF_TABLE
  (
    AUTO
    | DEC
  ) NEWLINE
;

a_vxlan
:
  VXLAN vni_names = glob
  (
    vx_bridge
    | vx_stp
    | vx_vxlan
  )
;

vx_bridge
:
  BRIDGE
  (
    vxb_access
    | vxb_arp_nd_suppress
    | vxb_learning
  )
;

vxb_access
:
  ACCESS vlan = vlan_id NEWLINE
;

vxb_arp_nd_suppress
:
  ARP_ND_SUPPRESS
  (
    OFF
    | ON
  ) NEWLINE
;

vxb_learning
:
  LEARNING
  (
    OFF
    | ON
  ) NEWLINE
;

vx_stp
:
  STP
  (
    vxs_bpduguard
    | vxs_portbpdufilter
  )
;

vxs_bpduguard
:
  BPDUGUARD NEWLINE
;

vxs_portbpdufilter
:
  PORTBPDUFILTER NEWLINE
;

vx_vxlan
:
  VXLAN
  (
    vxv_id
    | vxv_local_tunnelip
  )
;

vxv_id
:
  ID vni = vni_number NEWLINE
;

vxv_local_tunnelip
:
  LOCAL_TUNNELIP ip = IP_ADDRESS NEWLINE
;

s_net_add_unrecognized
:
  NET ADD word null_rest_of_line
;

s_null
:
  (
    NET
    (
      | COMMIT
      | DEL
    )
  ) null_rest_of_line
;

