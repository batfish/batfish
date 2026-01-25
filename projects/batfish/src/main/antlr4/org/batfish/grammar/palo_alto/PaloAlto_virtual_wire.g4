parser grammar PaloAlto_virtual_wire;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_virtual_wire
:
    VIRTUAL_WIRE
    (
        name=variable
        (
            snvw_interface1
            | snvw_interface2
            | snvw_link_state_pass_through
            | snvw_multicast_firewalling
            | snvw_tag
        )*
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

snvw_link_state_pass_through
:
    LINK_STATE_PASS_THROUGH ( yes_or_no | ENABLE | DISABLE )
;

snvw_multicast_firewalling
:
    MULTICAST_FIREWALLING ( yes_or_no | ENABLE | DISABLE )
;

snvw_tag
:
    TAG variable_list
;

