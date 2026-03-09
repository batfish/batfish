parser grammar Ftd_crypto;

options {
   tokenVocab = FtdLexer;
}

crypto_stanza
:
   CRYPTO
   (
      crypto_ca
      | crypto_ipsec
      | crypto_ikev2
      | crypto_map
      | crypto_map_interface
      | null_rest_of_line
   )
;

crypto_ca
:
   CA
   (
      TRUSTPOOL POLICY NEWLINE
      | null_rest_of_line
   )
;

crypto_ipsec
:
   IPSEC
   (
      crypto_ipsec_security_association
      | crypto_ipsec_transform_set
      | crypto_ipsec_profile
      | null_rest_of_line
   )
;

crypto_ipsec_security_association
:
   SECURITY_ASSOCIATION
   (
      PMTU_AGING (INFINITE | timeout = dec) NEWLINE
      | null_rest_of_line
   )
;

crypto_ipsec_transform_set
:
   TRANSFORM_SET (name_parts += ~(NEWLINE | ESP_AES | ESP_3DES | ESP_DES | ESP_SHA_HMAC | ESP_MD5_HMAC | MODE | TRANSPORT | TUNNEL))+ (algs += ~NEWLINE)* NEWLINE
;

crypto_ikev2
:
   IKEV2
   (
      crypto_ikev2_enable_null
      | crypto_ikev2_policy
      | null_rest_of_line
   )
;

crypto_ikev2_enable_null
:
   ENABLE (name_parts += ~NEWLINE)+ NEWLINE
;

crypto_ikev2_policy
:
   POLICY priority = dec NEWLINE
   (
      ikev2_policy_attr
      | stanza_unrecognized_line
   )*
;

ikev2_policy_attr
:
   (
      ENCRYPTION (enc_algs += ~NEWLINE)+
      | INTEGRITY (int_algs += ~NEWLINE)+
      | PRF (prf_algs += ~NEWLINE)+
      | GROUP (dh_groups += ~NEWLINE)+
      | LIFETIME SECONDS dec
   ) NEWLINE
;

crypto_ipsec_profile
:
   PROFILE (name_parts += ~NEWLINE)+ NEWLINE
   (
      cip_set
      | stanza_unrecognized_line
   )*
;

cip_set
:
   SET
   (
      TRANSFORM_SET (transform_names += ~NEWLINE)+ NEWLINE
      | PFS GROUP dec NEWLINE
      | null_rest_of_line
   )
;

crypto_map
:
   MAP (name_parts += ~(NEWLINE | INTERFACE | DEC))+ seq = dec
   (
      cm_match_address
      | cm_set
   )
;

crypto_map_interface
:
   MAP (name_parts += ~(NEWLINE | INTERFACE))+ INTERFACE iface_name = ~NEWLINE+ NEWLINE
;

cm_match_address
:
   MATCH ADDRESS (acl_parts += ~NEWLINE)+ NEWLINE
;

cm_set
:
   SET
   (
      PEER ip_address NEWLINE
      | TRANSFORM_SET (transform_names += ~NEWLINE)+ NEWLINE
      | PFS GROUP dec NEWLINE
      | null_rest_of_line
   )
;
