parser grammar PaloAlto_vlan;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_vlan
:
    VLAN
;
