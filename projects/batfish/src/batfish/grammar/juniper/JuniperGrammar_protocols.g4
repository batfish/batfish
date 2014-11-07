parser grammar JuniperGrammar_protocols;

import JuniperGrammarCommonParser, JuniperGrammar_bgp, JuniperGrammar_ospf;

options {
   tokenVocab = JuniperGrammarLexer;
}

protocols_stanza
:
   PROTOCOLS OPEN_BRACE = p_stanza+ CLOSE_BRACE
;

p_stanza
:
   bgp_p_stanza
   | ospf_p_stanza
   | null_p_stanza
;

null_p_stanza
:
   bfd_p_stanza
   | connections_p_stanza
   | igmp_p_stanza
   | igmp_snooping_p_stanza
   | isis_p_stanza
   | l2_circuit_p_stanza
   | ldp_p_stanza
   | lldp_p_stanza
   | lldp_med_p_stanza
   | mld_p_stanza
   | mpls_p_stanza
   | msdp_p_stanza
   | ospf3_p_stanza
   | pim_p_stanza
   | router_advertisement_p_stanza
   | rstp_p_stanza
   | rsvp_p_stanza
   | vstp_p_stanza
;

bfd_p_stanza
:
   BFD ignored_substanza
;

connections_p_stanza
:
   CONNECTIONS ignored_substanza
;

igmp_p_stanza
:
   IGMP ignored_substanza
;

igmp_snooping_p_stanza
:
   IGMP_SNOOPING ignored_substanza
;

isis_p_stanza
:
   ISIS ignored_substanza
;

l2_circuit_p_stanza
:
   L2_CIRCUIT ignored_substanza
;

ldp_p_stanza
:
   LDP ignored_substanza
;

lldp_p_stanza
:
   LLDP ignored_substanza
;

lldp_med_p_stanza
:
   LLDP_MED ignored_substanza
;

mld_p_stanza
:
   MLD ignored_substanza
;

mpls_p_stanza
:
   MPLS ignored_substanza
;

msdp_p_stanza
:
   MSDP ignored_substanza
;

ospf3_p_stanza
:
   OSPF3 ignored_substanza
;

pim_p_stanza
:
   PIM ignored_substanza
;

router_advertisement_p_stanza
:
   ROUTER_ADVERTISEMENT ignored_substanza
;

rstp_p_stanza
:
   RSTP ignored_substanza
;

rsvp_p_stanza
:
   RSVP ignored_substanza
;

vstp_p_stanza
:
   VSTP ignored_substanza
;
