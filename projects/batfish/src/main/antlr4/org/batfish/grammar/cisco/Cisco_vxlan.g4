parser grammar Cisco_vxlan;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_interface_nve1_null: NVE1 NEWLINE;