parser grammar Legacy_crypto;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

cc_certificate_chain
:
   CERTIFICATE CHAIN name = variable NEWLINE cccc_certificate*
;

cc_lookup
:
   LOOKUP null_rest_of_line
;

cc_server
:
   SERVER NEWLINE
   (
      ccs_null
   )*
;

cc_trustpoint
:
   TRUSTPOINT name = variable NEWLINE
   (
      cctpoint_null
   )*
;

cc_trustpool
:
   TRUSTPOOL name = variable NEWLINE
   (
      cctpool_null
   )*
;

cccc_certificate
:
   CERTIFICATE
   (
      CA
      | SELF_SIGNED
   )? certificate QUIT NEWLINE
;

ccs_null
:
   NO?
   (
      CDP_URL
      | ISSUER_NAME
      | SHUTDOWN
      | SMTP
   ) null_rest_of_line
;

cctpoint_null
:
   NO?
   (
      CRL
      | ENROLLMENT
      | FQDN
      | KEYPAIR
      | SUBJECT_NAME
      | VALIDATION_USAGE
   ) null_rest_of_line
;

cctpool_null
:
   NO?
   (
      AUTO_IMPORT
   ) null_rest_of_line
;

cd_match_address
:
   MATCH ADDRESS name = variable NEWLINE
;

cd_null
:
   NO?
   (
      VERSION
   ) null_rest_of_line
;

cd_set
:
   SET
   (
      cd_set_isakmp_profile
      | cd_set_null
      | cd_set_peer
      | cd_set_pfs
      | cd_set_transform_set
   )
;

cd_set_isakmp_profile
:
    ISAKMP_PROFILE name = variable NEWLINE
;

cd_set_null
:
    (
       SECURITY_ASSOCIATION
    ) null_rest_of_line
;

cd_set_peer
:
    PEER address = IP_ADDRESS NEWLINE
;

cd_set_pfs
:
    PFS dh_group NEWLINE
;

cd_set_transform_set
:
   TRANSFORM_SET
   (
      transforms += variable
   )+ NEWLINE
;

certificate
:
   ~QUIT+
;

cg_identity_null
:
   IDENTITY null_rest_of_line
;
cg_server_null
:
   SERVER null_rest_of_line
;

ci1_am_disable_null
:
   AM_DISABLE null_rest_of_line
;
ci1_enable_null
:
   ENABLE null_rest_of_line
;
ci1_ipsec_over_tcp_null
:
   IPSEC_OVER_TCP null_rest_of_line
;

ci1_policy
:
   POLICY name = variable NEWLINE
   (
      ci1p_null
   )*
;

ci1p_null
:
   NO?
   (
      AUTHENTICATION
      | ENCRYPTION
      | GROUP
      | HASH
      | LIFETIME
   ) null_rest_of_line
;

ci2_keyring
:
   KEYRING name = variable NEWLINE
   (
      ci2k_peer
   )*
;

ci2_enable_null
:
   ENABLE null_rest_of_line
;
ci2_remote_access_null
:
   REMOTE_ACCESS null_rest_of_line
;

ci2_policy
:
   POLICY name = variable NEWLINE
   (
      ci2pol_null
   )*
;

ci2_profile
:
   PROFILE name = variable NEWLINE
   (
      ci2prf_null
   )*
;

ci2_proposal
:
   PROPOSAL name = variable NEWLINE
   (
      ci2prp_null
   )*
;

ci2k_peer
:
   PEER name = variable NEWLINE
   (
      ci2kp_null
   )*
;

ci2kp_null
:
   NO?
   (
      ADDRESS
      | PRE_SHARED_KEY
   ) null_rest_of_line
;

ci2pol_null
:
   NO?
   (
      ENCRYPTION
      | GROUP
      | INTEGRITY
      | LIFETIME
      | PRF
      | PROPOSAL
   ) null_rest_of_line
;

ci2prf_null
:
   NO?
   (
      AUTHENTICATION
      | KEYRING
      | MATCH
   ) null_rest_of_line
;

ci2prp_null
:
   NO?
   (
      ENCRYPTION
      | GROUP
      | INTEGRITY
   ) null_rest_of_line
;

cip_ikev2
:
   IKEV2 cipi2_ipsec_proposal
;

cip_df_bit_null
:
   DF_BIT null_rest_of_line
;
cip_fragmentation_null
:
   FRAGMENTATION null_rest_of_line
;
cip_ikev1_null
:
   IKEV1 null_rest_of_line
;
cip_nat_transparency_null
:
   NAT_TRANSPARENCY null_rest_of_line
;
cip_security_association_null
:
   SECURITY_ASSOCIATION null_rest_of_line
;

cip_profile
:
   PROFILE name = variable_permissive NEWLINE
   (
      cipprf_set
   )*
;

cip_transform_set
:
   TRANSFORM_SET name = variable
   (
      ipsec_encryption
      | ipsec_encryption_aruba
   )
      ipsec_authentication? NEWLINE
   (
      cipt_mode
   )*
;

cipi2_ipsec_proposal
:
   IPSEC_PROPOSAL name = variable_permissive NEWLINE
   (
      cipi2ip_null
   )*
;

cipi2ip_null
:
   NO?
   (
      PROTOCOL
   ) null_rest_of_line
;

cipprf_set
:
   SET
   (
      cipprf_set_ikev2_profile_null
      | cipprf_set_isakmp_profile
      | cipprf_set_pfs
      | cipprf_set_security_association_null
      | cipprf_set_transform_set
   )
;

cipprf_set_isakmp_profile
:
    ISAKMP_PROFILE name = variable NEWLINE
;

cipprf_set_ikev2_profile_null
:
   IKEV2_PROFILE null_rest_of_line
;
cipprf_set_security_association_null
:
   SECURITY_ASSOCIATION null_rest_of_line
;

cipprf_set_pfs
:
   PFS dh_group NEWLINE
;

cipprf_set_transform_set
:
   TRANSFORM_SET
      (
          transforms += variable
      )+ NEWLINE
;

cipt_mode
:
   MODE
   (
     TRANSPORT
     | TUNNEL
   ) NEWLINE
;

cis_key
:
   KEY dec? key = VARIABLE ADDRESS ip_address = IP_ADDRESS (wildcard_mask = IP_ADDRESS)? NEWLINE
;

cis_eap_passthrough_null
:
   EAP_PASSTHROUGH null_rest_of_line
;
cis_enable_null
:
   ENABLE null_rest_of_line
;
cis_identity_null
:
   IDENTITY null_rest_of_line
;
cis_invalid_spi_recovery_null
:
   INVALID_SPI_RECOVERY null_rest_of_line
;
cis_keepalive_null
:
   KEEPALIVE null_rest_of_line
;
cis_nat_null
:
   NAT null_rest_of_line
;
cis_nat_traversal_null
:
   NAT_TRAVERSAL null_rest_of_line
;

cis_policy
:
   POLICY priority = dec NEWLINE
   (
      cispol_authentication
      | cispol_encryption        //cisco
      | cispol_encryption_aruba  //aruba
      | cispol_group
      | cispol_hash
      | cispol_lifetime
      | cispol_null
   )*
;

cis_profile
:
   PROFILE name = variable NEWLINE
   (
      cisprf_keyring
      | cisprf_local_address
      | cisprf_match
      | cisprf_null
      | cisprf_self_identity
   )*
;

cispol_authentication
:
   AUTHENTICATION
   (
      PRE_SHARE
      | RSA_ENCR
      | RSA_SIG
   ) NEWLINE
;

cispol_encryption
:
   (
      ENCR
      | ENCRYPTION
    ) ike_encryption NEWLINE
;

cispol_encryption_aruba
:
   ENCRYPTION ike_encryption_aruba NEWLINE
;

cispol_group
:
   GROUP dec NEWLINE
;

cispol_hash
:
   HASH
   (
      MD5
      | SHA
      | SHA2_256_128
   ) NEWLINE
;

cispol_lifetime
:
    LIFETIME dec NEWLINE
;

cispol_null
:
   NO?
   (
      PRF
      | VERSION
   ) null_rest_of_line
;

cisprf_keyring
:
   KEYRING name = variable_permissive NEWLINE
;

cisprf_local_address
:
    LOCAL_ADDRESS
    (
       IP_ADDRESS
       | iname = interface_name_unstructured
    ) NEWLINE
;

cisprf_match
:
   MATCH IDENTITY ADDRESS address = IP_ADDRESS mask = IP_ADDRESS? NEWLINE
;

cisprf_null
:
   NO?
   (
      KEEPALIVE
      | REVERSE_ROUTE
   ) null_rest_of_line
;

cisprf_self_identity
:
   SELF_IDENTITY IP_ADDRESS NEWLINE
;


ck_generate_null
:
   GENERATE null_rest_of_line
;
ck_param_null
:
   PARAM null_rest_of_line
;

ck_pubkey_chain
:
   PUBKEY_CHAIN null_rest_of_line ckp_named_key*
;

ckp_named_key
:
   NAMED_KEY name = variable_permissive NEWLINE
   (
      ckpn_address
      | ckpn_key_string
   )*
;

ckpn_address
:
   NO? ADDRESS ip_address = IP_ADDRESS NEWLINE
;

ckpn_key_string
:
   NO? KEY_STRING certificate QUIT NEWLINE
;

ckr_local_address
:
   LOCAL_ADDRESS
   (
      IP_ADDRESS
      | iname = interface_name_unstructured
   ) NEWLINE
;

ckr_psk
:
   PRE_SHARED_KEY ADDRESS ip_address = IP_ADDRESS (wildcard_mask = IP_ADDRESS)? KEY variable_permissive NEWLINE
;

cpki_certificate_chain
:
   CERTIFICATE CHAIN name = variable_permissive NEWLINE
   (
      cpkicc_certificate
   )*
;

cpki_null
:
   (
      TOKEN
   ) null_rest_of_line
;

cpki_server
:
   SERVER name = variable_permissive NEWLINE
   (
      cpkis_null
   )*
;

cpki_trustpoint
:
   TRUSTPOINT name = variable_permissive NEWLINE
   (
      cpkit_null
   )*
;

cpkicc_certificate
:
   CERTIFICATE
   (
      SELF_SIGNED
   )? certificate QUIT NEWLINE
;

cpkis_null
:
   NO?
   (
      CDP_URL
      | DATABASE
      | GRANT
      | ISSUER_NAME
   ) null_rest_of_line
;

cpkit_null
:
   NO?
   (
      ENROLLMENT
      | REVOCATION_CHECK
      | RSAKEYPAIR
      | SERIAL_NUMBER
      | SUBJECT_NAME
      | VALIDATION_USAGE
   ) null_rest_of_line
;

crypto_ca
:
   CA
   (
      cc_certificate_chain
      | cc_lookup
      | cc_server
      | cc_trustpoint
      | cc_trustpool
   )
;

crypto_csr_params
:
   CSR_PARAMS name = variable_permissive NEWLINE
   (
      (
         COMMON_NAME
         | COUNTRY
         | EMAIL
         | LOCALITY
         | ORGANIZATION_NAME
         | ORGANIZATION_UNIT
         | SERIAL_NUMBER
         | STATE
      ) null_rest_of_line
   )*
;

crypto_dynamic_map
:
   DYNAMIC_MAP name = variable seq_num = dec crypto_dynamic_map_null?
   (
     NEWLINE
       (
          cd_match_address
          | cd_null
          | cd_set
       )*
   )
;

crypto_dynamic_map_null
:
   (
     MATCH
     | SET
   ) ~NEWLINE*
;

crypto_engine
:
   ENGINE null_rest_of_line
;

crypto_gdoi
:
   GDOI null_rest_of_line
   (
      ( cg_identity_null | cg_server_null )
   )*
;

crypto_ikev1
:
   IKEV1
   (
      ci1_am_disable_null
      | ci1_enable_null
      | ci1_ipsec_over_tcp_null
      | ci1_policy
   )
;

crypto_ikev2
:
   IKEV2
   (
      ci2_enable_null
      | ci2_keyring
      | ci2_policy
      | ci2_profile
      | ci2_proposal
      | ci2_remote_access_null
   )
;

crypto_ipsec
:
   IPSEC
   (
      cip_df_bit_null
      | cip_fragmentation_null
      | cip_ikev1_null
      | cip_ikev2
      | cip_nat_transparency_null
      | cip_profile
      | cip_security_association_null
      | cip_transform_set
   )
;

crypto_isakmp
:
   ISAKMP
   (
      cis_eap_passthrough_null
      | cis_enable_null
      | cis_identity_null
      | cis_invalid_spi_recovery_null
      | cis_keepalive_null
      | cis_key
      | cis_nat_null
      | cis_nat_traversal_null
      | cis_policy
      | cis_profile
   )
;

crypto_key
:
   KEY
   (
      ck_generate_null
      | ck_param_null
      | ck_pubkey_chain
   )
;

crypto_keyring
:
   KEYRING name = variable_permissive NEWLINE
   (
      ckr_local_address
      | ckr_psk
   )*
;

crypto_map
:
   MAP name = variable
   (
      ( crypto_map_interface_null | crypto_map_local_address_null | crypto_map_redundancy_null )
      | seq_num = dec crypto_map_tail
   )
;

crypto_map_interface_null
:
   INTERFACE null_rest_of_line
;
crypto_map_local_address_null
:
   LOCAL_ADDRESS null_rest_of_line
;
crypto_map_redundancy_null
:
   REDUNDANCY null_rest_of_line
;

crypto_map_tail
:
   crypto_map_t_gdoi
   | crypto_map_t_ipsec_isakmp
   | crypto_map_t_ipsec_manual_null
   | crypto_map_t_match_null
   | crypto_map_t_set_null
;

crypto_map_t_g_null
:
   (
      SET
   ) null_rest_of_line
;

crypto_map_t_gdoi
:
   GDOI NEWLINE
   (
      crypto_map_t_g_null
   )*
;

crypto_map_t_ii_match_address
:
   MATCH ADDRESS name = variable NEWLINE
;

crypto_map_t_ii_null
:
   NO?
   (
      DESCRIPTION
      | REVERSE_ROUTE
   ) null_rest_of_line
;

crypto_map_t_ii_set
:
    SET
    (
       crypto_map_t_ii_set_isakmp_profile
       | crypto_map_t_ii_set_null
       | crypto_map_t_ii_set_peer
       | crypto_map_t_ii_set_pfs
       | crypto_map_t_ii_set_transform_set

    )
;

crypto_map_t_ii_set_isakmp_profile
:
    ISAKMP_PROFILE name = variable NEWLINE
;

crypto_map_t_ii_set_null
:
    (
       SECURITY_ASSOCIATION
    ) null_rest_of_line
;

crypto_map_t_ii_set_peer
:
    PEER address = IP_ADDRESS NEWLINE
;

crypto_map_t_ii_set_pfs
:
    PFS dh_group NEWLINE
;

crypto_map_t_ii_set_transform_set
:
   TRANSFORM_SET
   (
      transforms += variable
   )+ NEWLINE
;

crypto_map_t_ipsec_isakmp
:
   IPSEC_ISAKMP
   (
      DYNAMIC crypto_dynamic_map_name = variable
   )?
   NEWLINE
   (
      crypto_map_t_ii_match_address
      | crypto_map_t_ii_null
      | crypto_map_t_ii_set
   )*
;

crypto_map_t_ipsec_manual_null
:
   IPSEC_MANUAL null_rest_of_line
;
crypto_map_t_match_null
:
   MATCH null_rest_of_line
;
crypto_map_t_set_null
:
   SET null_rest_of_line
;

crypto_pki
:
   PKI
   (
      cpki_certificate_chain
      | cpki_null
      | cpki_server
      | cpki_trustpoint
   )
;

dh_group
:
   GROUP1
   | GROUP14
   | GROUP15
   | GROUP16
   | GROUP19
   | GROUP2
   | GROUP20
   | GROUP21
   | GROUP24
   | GROUP5
;

key_key
:
   KEY name = variable NEWLINE
   (
      kk_null
   )*
;

kk_null
:
   NO?
   (
      ACCEPT_LIFETIME
      | CRYPTOGRAPHIC_ALGORITHM
      | KEY_STRING
      | SEND_LIFETIME
   ) null_rest_of_line
;

ike_encryption
:
   (
      AES strength = dec?
   )
   | DES
   | THREE_DES
;

ike_encryption_aruba
:
   AES128
   | AES192
   | AES256
   | DES
   | THREE_DES
;

ipsec_authentication
:
   AH_MD5_HMAC
   | AH_SHA_HMAC
   | ESP_MD5_HMAC
   | ESP_SHA_HMAC
   | ESP_SHA256_HMAC
   | ESP_SHA512_HMAC
;

ipsec_encryption
:
   (
      ESP_AES strength = dec?
   )
   | ESP_DES
   | ESP_3DES
   |
   (
      ESP_GCM strength = dec?
   )
   |
   (
      ESP_GMAC strength = dec?
   )
   | ESP_NULL
   | ESP_SEAL
;

ipsec_encryption_aruba
:
   ESP_AES128
   | ESP_AES192
   | ESP_AES256
   | ESP_AES128_GCM
   | ESP_AES256_GCM
   | ESP_DES
   | ESP_3DES
;

s_crypto
:
   NO? CRYPTO
   (
      crypto_ca
      | crypto_csr_params
      | crypto_dynamic_map
      | crypto_engine
      | crypto_gdoi
      | crypto_ikev1
      | crypto_ikev2
      | crypto_ipsec
      | crypto_isakmp
      | crypto_key
      | crypto_keyring
      | crypto_map
      | crypto_pki
   )
;

s_key
:
   KEY CHAIN name = variable_permissive NEWLINE
   (
      key_key
   )*
;
