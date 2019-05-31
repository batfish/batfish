parser grammar PaloAlto_zone;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_zone
:
    ZONE name = variable
    (
        sz_network
    )
;

sz_network
:
    NETWORK
    (
        szn_external
        | szn_layer3
    )?
;

szn_external
:
    EXTERNAL variable_list
;

szn_layer3
:
    LAYER3 variable_list?
;
