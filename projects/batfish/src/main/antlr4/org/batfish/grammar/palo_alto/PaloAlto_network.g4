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
            | null_rest_of_line
        )
    )?
;

snikeg_authentication
:
    AUTHENTICATION
    (
        snikega_certificate
        | snikega_pre_shared_key
        | null_rest_of_line
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
        | null_rest_of_line
    )
;

snikegapsk_key
:
    KEY key = null_rest_of_line
;

snikeg_local_address
:
    LOCAL_ADDRESS
    (
        snikeg_la_floating_ip
        | snikeg_la_interface
        | snikeg_la_ip
        | null_rest_of_line
    )
;

snikeg_la_floating_ip
:
    FLOATING_IP addr = interface_address_or_reference
;

snikeg_la_interface
:
    INTERFACE name = variable
;

snikeg_la_ip
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
        | null_rest_of_line
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
        | null_rest_of_line
    )
;

snikegp_ikev1
:
    IKEV1
    (
        snikegpv1_ike_crypto_profile
        | null_rest_of_line
    )
;

snikegpv1_ike_crypto_profile
:
    IKE_CRYPTO_PROFILE name = variable
;

snikegp_ikev2
:
    IKEV2
    (
        snikegpv2_ike_crypto_profile
        | null_rest_of_line
    )
;

snikegpv2_ike_crypto_profile
:
    IKE_CRYPTO_PROFILE name = variable
;

snikegp_version
:
    VERSION version = variable
;

sn_tunnel
:
    TUNNEL
    (
        sntun_ipsec
        | null_rest_of_line
    )
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
            | null_rest_of_line
        )
    )?
;

sntuni_auto_key
:
    AUTO_KEY
    (
        sntunia_ike_gateway
        | sntunia_ipsec_crypto_profile
        | null_rest_of_line
    )
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
        cp_dh_group
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
        | cp_lifetime
    )
;

snicpi_esp
:
    ESP
    (
        cp_authentication
        | cp_encryption
        | null_rest_of_line
    )
;

snicpi_ah
:
    AH cp_authentication
;
