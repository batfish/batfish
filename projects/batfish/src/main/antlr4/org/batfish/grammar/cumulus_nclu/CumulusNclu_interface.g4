parser grammar CumulusNclu_interface;

import CumulusNclu_common, CumulusNclu_stp;

options {
  tokenVocab = CumulusNcluLexer;
}

a_interface
:
  INTERFACE interfaces = glob
  (
    i_alias
    | i_bridge
    | i_clag
    | i_ip_address
    | i_ipv6
    | i_link_speed
    | i_mtu
    | i_vrf
    | stp_common
    | NEWLINE
  )
;

i_alias
:
  ALIAS ALIAS_BODY NEWLINE
;

i_bridge
:
  BRIDGE
  (
    ib_access
    | ib_pvid
    | ib_vids
  )
;

ib_access
:
  ACCESS vlan = vlan_id NEWLINE
;

ib_pvid
:
  PVID id = vlan_id NEWLINE
;

ib_vids
:
  VIDS vlans = vlan_range_set NEWLINE
;

i_clag
:
  CLAG
  (
    ic_backup_ip
    | ic_peer_ip
    | ic_priority
    | ic_sys_mac
  )
;

ic_backup_ip
:
  BACKUP_IP backup_ip = ip_address
  (
    VRF vrf = word
  )? NEWLINE
;

ic_peer_ip
:
  PEER_IP (peer_ip = ip_address | LINKLOCAL) NEWLINE
;

ic_priority
:
  PRIORITY priority = uint16 NEWLINE
;

ic_sys_mac
:
  SYS_MAC mac = mac_address NEWLINE
;

i_ip_address
:
  IP ADDRESS address = interface_address NEWLINE
;

i_ipv6
:
  IPV6
  (
    iv6_address
    | iv6_address_virtual
    | iv6_forward
    | iv6_gateway
    | iv6_nd
  )
;

i_link_speed
:
  LINK SPEED speed = uint32 NEWLINE
;

i_mtu
:
  MTU mtu = uint16 NEWLINE
;

iv6_address
:
  ADDRESS
  (
    IPV6_PREFIX
    | DHCP
  ) NEWLINE
;

iv6_address_virtual
:
  ADDRESS_VIRTUAL mac = MAC_ADDRESS NEWLINE
;

iv6_forward
:
  FORWARD (zero | NO | OFF) NEWLINE
;

iv6_gateway
:
  GATEWAY IPV6_ADDRESS NEWLINE
;

iv6_nd
:
  ND
  (
    IPV6_PREFIX NEWLINE
    | iv6_ra_interval
    | iv6_ra_lifetime
    | iv6_suppress_ra
  )
;

iv6_ra_interval
:
  RA_INTERVAL
  (
    interval = uint16
    | MSEC msecs = uint32
  )
  NEWLINE
;

iv6_ra_lifetime
:
  RA_LIFETIME val = uint16 NEWLINE
;

iv6_suppress_ra
:
  SUPPRESS_RA NEWLINE
;

i_vrf
:
  VRF name = word NEWLINE
;
