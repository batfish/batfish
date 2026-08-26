parser grammar PaloAlto_network;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

cp_authentication
:
    AUTHENTICATION
    (
        MD5
        | NONE
        | SHA1
        | SHA256
        | SHA384
        | SHA512
    )
;

cp_dh_group
:
    DH_GROUP
    (
        NO_PFS
        |
        (
            OPEN_BRACKET?
            (
                group += cp_dh_group_value
            )+
            CLOSE_BRACKET?
        )
    )
;

cp_dh_group_value
:
    GROUP1
    | GROUP2
    | GROUP5
    | GROUP14
    | GROUP15
    | GROUP16
    | GROUP19
    | GROUP20
    | GROUP21
;

cp_encryption
:
    ENCRYPTION
    OPEN_BRACKET?
    (
        algo += cp_encryption_algo
    )+
    CLOSE_BRACKET?
;

cp_encryption_algo
:
    DES
    | THREE_DES
    | AES_128_CBC
    | AES_192_CBC
    | AES_256_CBC
    | AES_128_GCM
    | AES_256_GCM
    | NULL
;

cp_hash
:
    HASH
    (
        MD5
        | SHA1
        | SHA256
        | SHA384
        | SHA512
    )
;

cp_authentication_multiple_null
:
    AUTHENTICATION_MULTIPLE null_rest_of_line
;

cp_lifesize_null
:
    LIFESIZE null_rest_of_line
;

cp_lifetime
:
    LIFETIME
    (
        DAYS
        | HOURS
        | MINUTES
        | SECONDS
    )
    val = uint16
;

s_network
:
    NETWORK
    (
        sn_ike
        | sn_interface
        | sn_profiles
        | sn_qos
        | sn_shared_gateway
        | sn_tunnel
        | sn_virtual_router
        | sn_virtual_wire
        | sn_vlan
    )
;

sn_ike
:
    IKE
    (
        sn_ike_crypto_profiles
        | sn_ike_gateway
    )
;

sn_ike_crypto_profiles
:
    CRYPTO_PROFILES
    (
        snicp_global_protect
        | snicp_ike_crypto_profiles
        | snicp_ipsec_crypto_profiles
    )
;

sn_ike_gateway
:
    GATEWAY
    (
        name = variable
        (
            snikeg_authentication
            | snikeg_local_address
            | snikeg_peer_address
            | snikeg_protocol
            | snikeg_comment_null
            | snikeg_disabled_null
            | snikeg_ipv6_null
            | snikeg_local_id_null
            | snikeg_peer_id_null
            | snikeg_protocol_common_null
        )
    )?
;

snikeg_authentication
:
    AUTHENTICATION
    (
        snikega_certificate
        | snikega_pre_shared_key
    )
;

snikega_certificate
:
    CERTIFICATE credential = null_rest_of_line
;

snikega_pre_shared_key
:
    PRE_SHARED_KEY
    (
        snikegapsk_key
    )
;

snikegapsk_key
:
    KEY key = null_rest_of_line
;

snikeg_comment_null
:
    COMMENT null_rest_of_line
;

snikeg_disabled_null
:
    DISABLED null_rest_of_line
;

snikeg_ipv6_null
:
    IPV6 null_rest_of_line
;

snikeg_local_id_null
:
    LOCAL_ID null_rest_of_line
;

snikeg_peer_id_null
:
    PEER_ID null_rest_of_line
;

snikeg_protocol_common_null
:
    PROTOCOL_COMMON null_rest_of_line
;

snikeg_local_address
:
    LOCAL_ADDRESS
    (
        snikegla_floating_ip
        | snikegla_interface
        | snikegla_ip
    )
;

snikegla_floating_ip
:
    FLOATING_IP addr = interface_address_or_reference
;

snikegla_interface
:
    INTERFACE name = variable
;

snikegla_ip
:
    IP addr = interface_address_or_reference
;

snikeg_peer_address
:
    PEER_ADDRESS
    (
        snikegpa_dynamic
        | snikegpa_fqdn
        | snikegpa_ip
    )
;

snikegpa_dynamic
:
    DYNAMIC
;

snikegpa_fqdn
:
    FQDN name = variable
;

snikegpa_ip
:
    IP addr = interface_address_or_reference
;

snikeg_protocol
:
    PROTOCOL
    (
        snikegp_ikev1
        | snikegp_ikev2
        | snikegp_version
    )
;

snikegp_ikev1
:
    IKEV1
    (
        snikegpv1_ike_crypto_profile
        | snikegpv1_dpd_null
        | snikegpv1_exchange_mode_null
    )
;

snikegpv1_ike_crypto_profile
:
    IKE_CRYPTO_PROFILE name = variable
;

snikegpv1_dpd_null
:
    DPD null_rest_of_line
;

snikegpv1_exchange_mode_null
:
    EXCHANGE_MODE null_rest_of_line
;

snikegp_ikev2
:
    IKEV2
    (
        snikegpv2_ike_crypto_profile
        | snikegpv2_dpd_null
        | snikegpv2_pq_ppk_null
        | snikegpv2_require_cookie_null
    )
;

snikegpv2_ike_crypto_profile
:
    IKE_CRYPTO_PROFILE name = variable
;

snikegpv2_dpd_null
:
    DPD null_rest_of_line
;

snikegpv2_pq_ppk_null
:
    PQ_PPK null_rest_of_line
;

snikegpv2_require_cookie_null
:
    REQUIRE_COOKIE null_rest_of_line
;

snikegp_version
:
    VERSION
    (
        IKEV1
        | IKEV2
        | IKEV2_PREFERRED
    )
;

sn_tunnel
:
    TUNNEL
    (
        sntun_ipsec
        | sntun_global_protect_gateway_null
        | sntun_global_protect_site_to_site_null
        | sntun_gre_null
    )
;

sntun_global_protect_gateway_null
:
    GLOBAL_PROTECT_GATEWAY null_rest_of_line
;

sntun_global_protect_site_to_site_null
:
    GLOBAL_PROTECT_SITE_TO_SITE null_rest_of_line
;

sntun_gre_null
:
    GRE null_rest_of_line
;

sntun_ipsec
:
    IPSEC
    (
        name = variable
        (
            sntuni_auto_key
            | sntuni_disabled
            | sntuni_ipsec_mode
            | sntuni_tunnel_interface
            | sntuni_anti_replay_null
            | sntuni_anti_replay_window_null
            | sntuni_comment_null
            | sntuni_copy_flow_label_null
            | sntuni_copy_tos_null
            | sntuni_enable_gre_encapsulation_null
            | sntuni_global_protect_satellite_null
            | sntuni_ipv6_null
            | sntuni_manual_key_null
            | sntuni_tunnel_monitor_null
        )
    )?
;

sntuni_anti_replay_null
:
    ANTI_REPLAY null_rest_of_line
;

sntuni_anti_replay_window_null
:
    ANTI_REPLAY_WINDOW null_rest_of_line
;

sntuni_comment_null
:
    COMMENT null_rest_of_line
;

sntuni_copy_flow_label_null
:
    COPY_FLOW_LABEL null_rest_of_line
;

sntuni_copy_tos_null
:
    COPY_TOS null_rest_of_line
;

sntuni_enable_gre_encapsulation_null
:
    ENABLE_GRE_ENCAPSULATION null_rest_of_line
;

sntuni_global_protect_satellite_null
:
    GLOBAL_PROTECT_SATELLITE null_rest_of_line
;

sntuni_ipv6_null
:
    IPV6 null_rest_of_line
;

sntuni_manual_key_null
:
    MANUAL_KEY null_rest_of_line
;

sntuni_tunnel_monitor_null
:
    TUNNEL_MONITOR null_rest_of_line
;

sntuni_auto_key
:
    AUTO_KEY
    (
        sntunia_ike_gateway
        | sntunia_ipsec_crypto_profile
        | sntunia_proxy_id_null
        | sntunia_proxy_id_v6_null
    )
;

sntunia_proxy_id_null
:
    PROXY_ID null_rest_of_line
;

sntunia_proxy_id_v6_null
:
    PROXY_ID_V6 null_rest_of_line
;

sntunia_ike_gateway
:
    IKE_GATEWAY name = variable
;

sntunia_ipsec_crypto_profile
:
    IPSEC_CRYPTO_PROFILE name = variable
;

sntuni_disabled
:
    DISABLED yn = yes_or_no
;

sntuni_ipsec_mode
:
    IPSEC_MODE
    (
        TRANSPORT
        | TUNNEL
    )
;

sntuni_tunnel_interface
:
    TUNNEL_INTERFACE name = variable
;

sn_profiles
:
    PROFILES null_rest_of_line
;

sn_qos
:
    QOS null_rest_of_line
;

sn_shared_gateway
:
    SHARED_GATEWAY sn_shared_gateway_definition?
;

sn_shared_gateway_definition
:
    name = variable
    (
        snsg_display_name
        | snsg_import
        | snsg_zone
    )?
;

snsg_display_name
:
    DISPLAY_NAME name = variable
;

snsg_import
:
    IMPORT
    (
        snsgi_interface
    )?
;

snsgi_interface
:
    NETWORK INTERFACE variable_list
;

snsg_zone
:
    ZONE snsg_zone_definition
;

snsg_zone_definition
:
    name = variable
    (
        snsgz_network
    )?
;

snsgz_network
:
    NETWORK
    (
        snsgzn_layer3
    )?
;

snsgzn_layer3
:
    LAYER3 variable_list
;

snicp_global_protect
:
    GLOBAL_PROTECT_APP_CRYPTO_PROFILES name = variable
    (
        cp_encryption
        | cp_authentication
    )
;

snicp_ike_crypto_profiles
:
    IKE_CRYPTO_PROFILES name = variable
    (
        cp_authentication_multiple_null
        | cp_dh_group
        | cp_encryption
        | cp_hash
        | cp_lifetime
    )
;

snicp_ipsec_crypto_profiles
:
    IPSEC_CRYPTO_PROFILES name = variable
    (
        snicpi_esp
        | snicpi_ah
        | cp_dh_group
        | cp_lifesize_null
        | cp_lifetime
    )
;

snicpi_esp
:
    ESP
    (
        cp_authentication
        | cp_encryption
    )
;

snicpi_ah
:
    AH cp_authentication
;
