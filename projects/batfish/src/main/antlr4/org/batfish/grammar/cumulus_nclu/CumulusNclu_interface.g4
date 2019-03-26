parser grammar CumulusNclu_interface;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

a_interface
:
  INTERFACE interfaces = glob
  (
    i_bridge
    | i_clag
    | i_ip_address
    | i_vrf
    | NEWLINE
  )
;

i_bridge
:
  BRIDGE ib_access
;

ib_access
:
  ACCESS vlan = vlan_id NEWLINE
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
  BACKUP_IP ip = IP_ADDRESS
  (
    VRF vrf = word
  )? NEWLINE
;

ic_peer_ip
:
  PEER_IP ip = IP_ADDRESS NEWLINE
;

ic_priority
:
  PRIORITY priority = uint16 NEWLINE
;

ic_sys_mac
:
  SYS_MAC mac = MAC_ADDRESS NEWLINE
;

i_ip_address
:
  IP ADDRESS address = IP_PREFIX NEWLINE
;

i_vrf
:
  VRF name = word NEWLINE
;
