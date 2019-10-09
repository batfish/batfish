parser grammar PaloAlto_ospf;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

vrp_ospf
:
    OSPF
;