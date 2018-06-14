parser grammar PaloAlto_network;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_network
:
    NETWORK
    (
        sn_interface
    )
;

sn_interface
:
    INTERFACE
    (
        sni_ethernet
    )
;

sni_ethernet
:
    ETHERNET name = variable
    (
        snie_comment
        | snie_layer3
        | snie_link_status
    )
;

snie_comment
:
    COMMENT text = variable
;

snie_layer3
:
    LAYER3
    (
        sniel3_ip
        | sniel3_mtu
    )
;

snie_link_status
:
    LINK_STATUS
    (
        AUTO
        | DOWN
        | UP
    )
;

sniel3_ip
:
    IP address =
    (
        IP_PREFIX
        | IP_ADDRESS
    )
;

sniel3_mtu
:
    MTU mtu = DEC
;
