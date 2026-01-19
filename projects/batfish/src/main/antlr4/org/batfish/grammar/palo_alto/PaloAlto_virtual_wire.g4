parser grammar PaloAlto_virtual_wire;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_virtual_wire
:
    VIRTUAL_WIRE
    name=variable
    (
        snvw_interface1
        | snvw_interface2
    )?
;

snvw_interface1
:
    INTERFACE1 variable
;

snvw_interface2
:
    INTERFACE2 variable
;
