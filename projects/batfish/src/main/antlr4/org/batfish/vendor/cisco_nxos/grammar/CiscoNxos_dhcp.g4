parser grammar CiscoNxos_dhcp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

// TODO: flesh out.

ip_dhcp: DHCP RELAY NEWLINE;

ipv6_dhcp: DHCP RELAY NEWLINE;