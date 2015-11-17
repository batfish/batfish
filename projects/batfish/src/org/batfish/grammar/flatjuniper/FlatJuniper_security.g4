parser grammar FlatJuniper_security;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

certt_local
:
   LOCAL name = variable cert = DOUBLE_QUOTED_STRING
;

dh_group
:
   GROUP1
   | GROUP14
   | GROUP2
   | GROUP5
;

encryption_algorithm
:
   THREEDES_CBC
   | AES_128_CBC
   | AES_192_CBC
   | AES_256_CBC
   | DES_CBC
;

ikegt_address
:
   ADDRESS IP_ADDRESS
;

ikegt_dead_peer_detection
:
   DEAD_PEER_DETECTION ALWAYS_SEND?
;

ikegt_external_interface
:
   EXTERNAL_INTERFACE interface_id
;

ikegt_ike_policy
:
   IKE_POLICY name = variable
;

ikegt_local_address
:
   LOCAL_ADDRESS IP_ADDRESS
;

ikegt_no_nat_traversal
:
   NO_NAT_TRAVERSAL
;

ikegt_version
:
   VERSION V1_ONLY
;

ikeprt_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM
   (
      MD5
      | SHA_256
      | SHA1
   )
;

ikeprt_authentication_method
:
   AUTHENTICATION_METHOD
   (
      DSA_SIGNATURES
      | PRE_SHARED_KEYS
      | RSA_SIGNATURES
   )
;

ikeprt_description
:
   DESCRIPTION s_null_filler
;

ikeprt_dh_group
:
   DH_GROUP dh_group
;

ikeprt_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

ikeprt_lifetime_seconds
:
   LIFETIME_SECONDS seconds = DEC
;

ikept_mode
:
   MODE
   (
      AGGRESSIVE
      | MAIN
   )
;

ikept_pre_shared_key
:
   PRE_SHARED_KEY ASCII_TEXT key = DOUBLE_QUOTED_STRING
;

ikept_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

ikept_proposals
:
   PROPOSALS name = variable
;

iket_gateway
:
   GATEWAY iket_gateway_named
;

iket_gateway_named
:
   name = variable iket_gateway_tail
;

iket_gateway_tail
:
   ikegt_address
   | ikegt_dead_peer_detection
   | ikegt_external_interface
   | ikegt_ike_policy
   | ikegt_local_address
   | ikegt_no_nat_traversal
   | ikegt_version
;

iket_policy
:
   POLICY iket_policy_named
;

iket_policy_named
:
   name = variable iket_policy_tail
;

iket_policy_tail
:
   ikept_mode
   | ikept_pre_shared_key
   | ikept_proposal_set
   | ikept_proposals
;

iket_proposal
:
   PROPOSAL iket_proposal_named
;

iket_proposal_named
:
   name = variable iket_proposal_tail
;

iket_proposal_tail
:
   ikeprt_authentication_algorithm
   | ikeprt_authentication_method
   | ikeprt_description
   | ikeprt_dh_group
   | ikeprt_encryption_algorithm
   | ikeprt_lifetime_seconds
;

ipsecprt_authentication_algorithm
:
   AUTHENTICATION_ALGORITHM
   (
      HMAC_MD5_96
      | HMAC_SHA1_96
   )
;

ipsecprt_encryption_algorithm
:
   ENCRYPTION_ALGORITHM encryption_algorithm
;

ipsecprt_lifetime_seconds
:
   LIFETIME_SECONDS seconds = DEC
;

ipsecprt_protocol
:
   PROTOCOL
   (
      AH
      | BUNDLE
      | ESP
   )
;

ipsecpt_perfect_forward_secrecy
:
   PERFECT_FORWARD_SECRECY KEYS dh_group
;

ipsecpt_proposal_set
:
   PROPOSAL_SET proposal_set_type
;

ipsecpt_proposals
:
   PROPOSALS name = variable
;

ipsect_policy
:
   POLICY ipsect_policy_named
;

ipsect_policy_named
:
   name = variable ipsect_policy_tail
;

ipsect_policy_tail
:
   ipsecpt_perfect_forward_secrecy
   | ipsecpt_proposal_set
   | ipsecpt_proposals
;

ipsect_proposal
:
   PROPOSAL ipsect_proposal_named
;

ipsect_proposal_named
:
   name = variable ipsect_proposal_tail
;

ipsect_proposal_tail
:
   ipsecprt_authentication_algorithm
   | ipsecprt_encryption_algorithm
   | ipsecprt_lifetime_seconds
   | ipsecprt_protocol
;

ipsect_vpn
:
   VPN ipsect_vpn_named
;

ipsect_vpn_named
:
   name = variable ipsect_vpn_tail
;

ipsect_vpn_tail
:
   ipsecvt_bind_interface
   | ipsecvt_df_bit
   | ipsecvt_establish_tunnels
   | ipsecvt_ike
   | ipsecvt_vpn_monitor
;

ipsecvit_gateway
:
   GATEWAY name = variable
;

ipsecvit_ipsec_policy
:
   IPSEC_POLICY name = variable
;

ipsecvit_proxy_identity
:
   PROXY_IDENTITY
   (
      LOCAL
      | REMOTE
   ) IP_PREFIX
;

ipsecvmt_destination_ip
:
   DESTINATION_IP IP_ADDRESS
;

ipsecvmt_source_interface
:
   SOURCE_INTERFACE interface_id
;

ipsecvt_bind_interface
:
   BIND_INTERFACE interface_id
;

ipsecvt_df_bit
:
   DF_BIT CLEAR
;

ipsecvt_establish_tunnels
:
   ESTABLISH_TUNNELS IMMEDIATELY
;

ipsecvt_ike
:
   IKE ipsecvt_ike_tail
;

ipsecvt_ike_tail
:
   ipsecvit_gateway
   | ipsecvit_ipsec_policy
   | ipsecvit_proxy_identity
;

ipsecvt_vpn_monitor
:
   VPN_MONITOR ipsecvt_vpn_monitor_tail
;

ipsecvt_vpn_monitor_tail
:
// intentional blank

   | ipsecvmt_destination_ip
   | ipsecvmt_source_interface
;

nat_rule_set
:
   RULE_SET nat_rule_set_named
;

nat_rule_set_named
:
   name = variable nat_rule_set_tail
;

nat_rule_set_tail
:
   natrst_from
   | natrst_rule
   | natrst_to
;

natrsrmt_destination_address
:
   DESTINATION_ADDRESS IP_PREFIX
;

natrsrmt_source_address
:
   SOURCE_ADDRESS IP_PREFIX
;

natrsrt_match
:
   MATCH natrsrt_match_tail
;

natrsrt_match_tail
:
   natrsrmt_destination_address
   | natrsrmt_source_address
;

natrsrt_then
:
   THEN natrsrt_then_tail
;

natrsrt_then_tail
:
   natrsrtt_source_nat
   | natrsrtt_static_nat
;

natrsrtt_source_nat
:
   SOURCE_NAT INTERFACE
;

natrsrtt_static_nat
:
   STATIC_NAT PREFIX IP_PREFIX
;

natrst_from
:
   FROM natrst_from_tail
;

natrst_from_tail
:
   natrsfromt_zone
;

natrst_rule
:
   RULE natrst_rule_named
;

natrst_rule_named
:
   name = variable natrst_rule_tail
;

natrst_rule_tail
:
   natrsrt_match
   | natrsrt_then
;

natrst_to
:
   TO natrst_from_tail
;

natrst_to_tail
:
   natrstot_zone
;

natrsfromt_zone
:
   ZONE name = variable
;

natrstot_zone
:
   ZONE name = variable
;

natsot_rule_set
:
   nat_rule_set
;

natstt_rule_set
:
   nat_rule_set
;

natt_source
:
   SOURCE natt_source_tail
;

natt_source_tail
:
   natsot_rule_set
;

natt_static
:
   STATIC natt_static_tail
;

natt_static_tail
:
   natstt_rule_set
;

proposal_set_type
:
   BASIC
   | COMPATIBLE
   | STANDARD
;

s_security
:
   SECURITY s_security_tail
;

s_security_tail
:
   sect_certificates
   | sect_ike
   | sect_ipsec
   | sect_nat
   | sect_null
   | sect_zones
;

sect_certificates
:
   CERTIFICATES sect_certificates_tail
;

sect_certificates_tail
:
   certt_local
;

sect_ike
:
   IKE sect_ike_tail
;

sect_ike_tail
:
   iket_gateway
   | iket_policy
   | iket_proposal
;

sect_ipsec
:
   IPSEC sect_ipsec_tail
;

sect_ipsec_tail
:
   ipsect_policy
   | ipsect_proposal
   | ipsect_vpn
;

sect_nat
:
   NAT sect_nat_tail
;

sect_nat_tail
:
   natt_source
   | natt_static
;

sect_null
:
   (
      ALG
      | FLOW
      | LOG
      | SCREEN
   ) s_null_filler
;

sect_zones
:
   ZONES
;