parser grammar CumulusNcluParser;

/* This is only needed if parser grammar is spread across files */
import
CumulusNclu_common, CumulusNclu_bgp, CumulusNclu_frr, CumulusNclu_interface, CumulusNclu_routing;

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
  EXTRA_CONFIGURATION_HEADER
  (
    frr_router
    | frr_username
    | frr_vrf
    | // frr_unrecognized must be last
    frr_unrecognized
  ) EXTRA_CONFIGURATION_FOOTER NEWLINE
;

s_net_add
:
  NET ADD
  (
    a_bgp
    | a_bond
    | a_bridge
    | a_dns
    | a_dot1x
    | a_hostname
    | a_interface
    | a_loopback
    | a_ptp
    | a_routing
    | a_snmp_server
    | a_time
    | a_vlan
    | a_vrf
    | a_vxlan
  )
;

a_bond
:
  BOND bonds = glob
  (
    bond_bond
    | bond_bridge
    | bond_clag_id
    | bond_ip_address
    | bond_mtu
    | bond_vrf
    | stp_common
  )
;

bond_bond
:
  BOND
  (
    bobo_lacp_bypass_allow
    | bobo_slaves
  )
;

bobo_lacp_bypass_allow
:
  LACP_BYPASS_ALLOW NEWLINE
;

bobo_slaves
:
  SLAVES slaves = glob NEWLINE
;

bond_bridge
:
  BRIDGE
  (
    bob_access
    | bob_learning
    | bob_pvid
    | bob_vids
  )
;

bob_access
:
  ACCESS vlan = vlan_id NEWLINE
;

bob_learning
:
  LEARNING OFF NEWLINE
;

bob_pvid
:
  PVID id = vlan_id NEWLINE
;

bob_vids
:
  VIDS vlans = vlan_range_set NEWLINE
;

bond_clag_id
:
  CLAG ID id = uint16 NEWLINE
;

bond_ip_address
:
  IP ADDRESS address = interface_address NEWLINE
;

bond_mtu
:
  MTU mtu = uint16 NEWLINE
;

bond_vrf
:
  VRF name = word NEWLINE
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
    | brbr_pvid
    | brbr_vids
    | brbr_vlan_aware
  )
;

brbr_ports
:
  PORTS ports = glob NEWLINE
;

brbr_pvid
:
  PVID pvid = vlan_id NEWLINE
;

brbr_vids
:
  VIDS ids = range_set NEWLINE
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
  IPV4 address = ip_address NEWLINE
;

dn6
:
  IPV6 address6 = ipv6_address NEWLINE
;

a_dot1x
:
  DOT1X null_rest_of_line
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
    | l_vxlan
    | NEWLINE
  )
;

l_clag
:
  CLAG lc_vxlan_anycast_ip
;

lc_vxlan_anycast_ip
:
  VXLAN_ANYCAST_IP ip = ip_address NEWLINE
;

l_ip_address
:
  IP ADDRESS address = interface_address NEWLINE
;

l_vxlan
:
   VXLAN
   (
     lv_local_tunnelip
   )
;

lv_local_tunnelip
:
   LOCAL_TUNNELIP ip = ip_address NEWLINE
;

a_ptp
:
  PTP null_rest_of_line
;

a_snmp_server
:
  SNMP_SERVER null_rest_of_line
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
  SERVER server = word IBURST? NEWLINE
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
  VLAN
  (
    (
      suffix = uint16 v_vlan_id
    )
    |
    (
      suffixes = range_set
      (
        v_alias
        | v_hw_address
        | v_ip_address
        | v_ip_address_virtual
        | v_vlan_raw_device
        | v_vrf
        | NEWLINE
      )
    )
  )
;

v_alias
:
  ALIAS alias = ALIAS_BODY NEWLINE
;

v_hw_address
:
  HWADDRESS mac = mac_address NEWLINE
;

v_ip_address
:
  IP ADDRESS address = interface_address NEWLINE
;

v_ip_address_virtual
:
  IP ADDRESS_VIRTUAL mac = mac_address address = interface_address NEWLINE
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
  VRF names = glob
  (
    vrf_ip_address
    | vrf_vni
    | vrf_vrf_table
    | NEWLINE
  )
;

vrf_ip_address
:
  IP ADDRESS address = interface_address NEWLINE
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
  VXLAN names = glob
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
  LOCAL_TUNNELIP ip = ip_address NEWLINE
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

