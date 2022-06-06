parser grammar FlatJuniper_evpn;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

e_default_gateway
:
    DEFAULT_GATEWAY NO_GATEWAY_COMMUNITY
;

e_encapsulation
:
    ENCAPSULATION (
      VXLAN
      | MPLS
    )
;

e_extended_vni_list
:
    EXTENDED_VNI_LIST (
        OPEN_BRACKET vni_range+ CLOSE_BRACKET
        | ALL
        | vni_range
    )
;

e_duplicate_mac_detection_null
:
    DUPLICATE_MAC_DETECTION null_filler
;

e_multicast_mode
:
    MULTICAST_MODE (
        CLIENT
        | INGRESS_REPLICATION
    )
;

e_vni_options
:
    VNI_OPTIONS VNI id = dec (
        evo_designated_forwarder_election_hold_time
        | evo_vrf_target
    )+
;

evo_designated_forwarder_election_hold_time
:
    DESIGNATED_FORWARDER_ELECTION_HOLD_TIME secs = dec
;

evo_vrf_target
:
    VRF_TARGET (
        evovt_auto
        | evovt_community
        | evovt_export
        | evovt_import
    )
;

evovt_auto
:
    AUTO
;

evovt_community
:
    comm = vt_community
;

evovt_export
:
    EXPORT vt_community
;

evovt_import
:
    IMPORT vt_community
;

p_evpn
:
    EVPN (
        e_default_gateway
        | e_encapsulation
        | e_extended_vni_list
        | e_multicast_mode
        | e_vni_options
        | e_duplicate_mac_detection_null
    )
;

vt_community
:
    TARGET COLON x = dec COLON y = dec
;