parser grammar FlatJuniper_protocols;

import
FlatJuniper_common, FlatJuniper_bgp, FlatJuniper_evpn, FlatJuniper_isis, FlatJuniper_mpls, FlatJuniper_ospf;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols
:
   PROTOCOLS
   (
      p_bgp
      | p_connections
      | p_evpn
      | p_isis
      | p_mpls
      | p_null
      | p_ospf
      | p_ospf3
   )
;

 p_null
:
   (
      BFD
      | DCBX
      | ICCP
      | IGMP
      | IGMP_SNOOPING
      | L2_LEARNING
      | L2CIRCUIT
      | L2VPN
      | LACP
      | LAYER2_CONTROL
      | LDP
      | LLDP
      | LLDP_MED
      | MLD
      | MSDP
      | MSTP
      | MVPN
      | NEIGHBOR_DISCOVERY
      | PIM
      | ROUTER_ADVERTISEMENT
      | ROUTER_DISCOVERY
      | RSTP
      | RSVP
      | SFLOW
      | STP
      | UPLINK_FAILURE_DETECTION
      | VRRP
      | VSTP
   ) null_filler
;

