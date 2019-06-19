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
  PEER_IP peer_ip = ip_address NEWLINE
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

i_vrf
:
  VRF name = word NEWLINE
;
