parser grammar PaloAlto_virtual_wire;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_virtual_wire
:
    VIRTUAL_WIRE
;
