parser grammar Arista_mlag;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}


mlag_domain
:
   DOMAIN_ID id = variable NEWLINE
;

mlag_local_interface
:
   LOCAL_INTERFACE iface = variable NEWLINE
;

mlag_peer_address
:
   PEER_ADDRESS ip = IP_ADDRESS NEWLINE
;

mlag_peer_link
:
   PEER_LINK iface = variable NEWLINE
;

mlag_reload_delay
:
   RELOAD_DELAY (MLAG | NON_MLAG)? period = (INFINITY | DEC) NEWLINE
;

mlag_reload_mode
:
   MODE LACP STANDBY NEWLINE
;

s_eos_mlag
:
   MLAG CONFIGURATION NEWLINE
   mlag_domain
   | mlag_local_interface
   | mlag_peer_address
   | mlag_peer_link
   | mlag_reload_delay
   | SHUTDOWN NEWLINE
;

