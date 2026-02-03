parser grammar PaloAlto_vlan;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sn_vlan
:
    VLAN vlan_id = variable?
    (
        sn_vlan_interface
        | sn_vlan_virtual_interface
    )*
;

sn_vlan_interface
:
    INTERFACE iface = variable
;

sn_vlan_virtual_interface
:
    VIRTUAL_INTERFACE INTERFACE iface = variable
;
