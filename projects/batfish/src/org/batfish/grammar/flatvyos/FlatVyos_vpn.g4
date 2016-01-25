parser grammar FlatVyos_vpn;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

esppt_encryption
:
   ENCRYPTION
   (
      AES128
      | AES256
      | THREEDES
   )
;

esppt_hash
:
   HASH hash_algorithm
;

espt_compression
:
   COMPRESSION
   (
      DISABLE
      | ENABLE
   )
;

espt_lifetime
:
   LIFETIME seconds = DEC
;

espt_mode
:
   MODE
   (
      TRANSPORT
      | TUNNEL
   )
;

espt_pfs
:
   PFS
   (
      DH_GROUP2
      | DH_GROUP5
      | DH_GROUP14
      | DH_GROUP15
      | DH_GROUP16
      | DH_GROUP17
      | DH_GROUP18
      | DH_GROUP19
      | DH_GROUP20
      | DH_GROUP21
      | DH_GROUP22
      | DH_GROUP23
      | DH_GROUP24
      | DH_GROUP25
      | DH_GROUP26
      | DISABLE
      | ENABLE
   )
;

espt_proposal
:
   PROPOSAL num = DEC espt_proposal_tail
;

espt_proposal_tail
:
   esppt_encryption
   | esppt_hash
;

hash_algorithm
:
   MD5
   | SHA1
   | SHA256
   | SHA384
   | SHA512
;

ikept_dh_group
:
   DH_GROUP num = DEC
;

ikept_encryption
:
   ENCRYPTION
   (
      AES128
      | AES256
      | THREEDES
   )
;

ikept_hash
:
   HASH hash_algorithm
;

iket_key_exchange
:
   KEY_EXCHANGE
   (
      IKEV1
      | IKEV2
   )
;

iket_lifetime
:
   LIFETIME seconds = DEC
;

iket_null
:
   (
      DEAD_PEER_DETECTION
      | IKEV2_REAUTH
   ) null_filler
;

iket_proposal
:
   PROPOSAL num = DEC iket_proposal_tail
;

iket_proposal_tail
:
   ikept_dh_group
   | ikept_encryption
   | ikept_hash
;

ivt_esp_group
:
   ESP_GROUP name = variable ivt_esp_group_tail
;

ivt_esp_group_tail
:
   espt_compression
   | espt_lifetime
   | espt_mode
   | espt_pfs
   | espt_proposal
;

ivt_ike_group
:
   IKE_GROUP name = variable ivt_ike_group_tail
;

ivt_ike_group_tail
:
   iket_key_exchange
   | iket_lifetime
   | iket_null
   | iket_proposal
;

ivt_ipsec_interfaces
:
   IPSEC_INTERFACES INTERFACE name = variable
;

ivt_null
:
   (
      AUTO_UPDATE
   ) null_filler
;

ivt_site_to_site
:
   SITE_TO_SITE PEER peer = IP_ADDRESS ivt_site_to_site_tail
;

ivt_site_to_site_tail
:
   s2st_authentication
   | s2st_connection_type
   | s2st_description
   | s2st_ike_group
   | s2st_local_address
   | s2st_null
   | s2st_vti
;

s_vpn
:
   VPN s_vpn_tail
;

s_vpn_tail
:
   vpnt_ipsec
;

s2sat_id
:
   ID name = variable
;

s2sat_mode
:
   MODE
   (
      PRE_SHARED_SECRET
      | RSA
      | X509
   )
;

s2sat_pre_shared_secret
:
   PRE_SHARED_SECRET secret = variable
;

s2sat_remote_id
:
   REMOTE_ID name = variable
;

s2svt_bind
:
   BIND name = variable
;

s2svt_esp_group
:
   ESP_GROUP name = variable
;

s2st_authentication
:
   AUTHENTICATION s2st_authentication_tail
;

s2st_authentication_tail
:
   s2sat_id
   | s2sat_mode
   | s2sat_pre_shared_secret
   | s2sat_remote_id
;

s2st_connection_type
:
   CONNECTION_TYPE
   (
      INITIATE
      | RESPOND
   )
;

s2st_description
:
   description
;

s2st_ike_group
:
   IKE_GROUP name = variable
;

s2st_local_address
:
   LOCAL_ADDRESS
   (
      ANY
      | ip = IP_ADDRESS
   )
;

s2st_null
:
   (
      IKEV2_REAUTH
   ) null_filler
;

s2st_vti
:
   VTI s2st_vti_tail
;

s2st_vti_tail
:
   s2svt_bind
   | s2svt_esp_group
;

vpnt_ipsec
:
   IPSEC vpnt_ipsec_tail
;

vpnt_ipsec_tail
:
   ivt_esp_group
   | ivt_ike_group
   | ivt_ipsec_interfaces
   | ivt_null
   | ivt_site_to_site
;