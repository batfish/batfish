parser grammar PaloAlto_ospf;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

snvrp_ospf
:
    OSPF
;