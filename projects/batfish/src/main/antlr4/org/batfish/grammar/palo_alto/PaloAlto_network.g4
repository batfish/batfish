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
        GROUP1
        | GROUP2
        | GROUP5
        | GROUP14
        | GROUP19
        | GROUP20
    )
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
    val = DEC
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
        | sn_virtual_router
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
    GATEWAY null_rest_of_line
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

sn_virtual_router
:
    VIRTUAL_ROUTER sn_virtual_router_definition?
;

sn_virtual_router_definition
:
    name = variable
    (
        snvr_interface
        | snvr_protocol
        | snvr_routing_table
    )?
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
        (
            ESP
            (
                cp_authentication
                | cp_encryption
            )
        )
        | cp_dh_group
        | cp_lifetime
    )
;

snvr_interface
:
    INTERFACE variable_list
;

snvr_protocol
:
    PROTOCOL
    snvrp_bgp
;

snvr_routing_table
:
    ROUTING_TABLE IP STATIC_ROUTE name = variable
    (
        snvrrt_admin_dist
        | snvrrt_destination
        | snvrrt_interface
        | snvrrt_metric
        | snvrrt_nexthop
    )
;

snvrp_bgp
:
    BGP
    (
        snvrp_bgp_enable
        | snvrp_bgp_null
    )
;

snvrp_bgp_enable
:
    ENABLE NO  // parse and ignore NO
;


snvrp_bgp_null
:
    DAMPENING_PROFILE
    null_rest_of_line
;

snvrrt_admin_dist
:
    ADMIN_DIST distance = DEC
;

snvrrt_destination
:
    DESTINATION destination = IP_PREFIX
;

snvrrt_interface
:
    INTERFACE iface = variable
;

snvrrt_metric
:
    METRIC metric = DEC
;

snvrrt_nexthop
:
    NEXTHOP IP_ADDRESS_LITERAL address = IP_ADDRESS
;
