parser grammar Mrv_interface;

import Mrv_common;

options {
   tokenVocab = MrvLexer;
}

a_interface
:
   INTERFACE PERIOD
   (
      a_interface_authtype
      | a_interface_banner
      | a_interface_bonddevs
      | a_interface_bondmiimon
      | a_interface_bondmode
      | a_interface_dhcp
      | a_interface_ifname
      | a_interface_ipaddress
      | a_interface_ipbroadcast
      | a_interface_ipmask
      | a_interface_sshportlist
      | a_interface_stat
   )
;

a_interface_authtype
:
   AUTHTYPE nbdecl
;

a_interface_banner
:
   BANNER nsdecl
;

a_interface_bonddevs
:
   BONDDEVS nosdecl
;

a_interface_bondmiimon
:
   BONDMIIMON nshdecl
;

a_interface_bondmode
:
   BONDMODE nshdecl
;

a_interface_dhcp
:
   DHCP nbdecl
;

a_interface_ifname
:
   IFNAME nsdecl
;

a_interface_ipaddress
:
   IPADDRESS nipdecl
;

a_interface_ipbroadcast
:
   IPBROADCAST nipdecl
;

a_interface_ipmask
:
   IPMASK nipdecl
;

a_interface_sshportlist
:
   SSHPORTLIST nssdecl
;

a_interface_stat
:
   STAT nbdecl
;
