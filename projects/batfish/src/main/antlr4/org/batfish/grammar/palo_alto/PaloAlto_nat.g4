parser grammar PaloAlto_nat;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

sr_nat
:
    NAT sr_nat_rules?
;

sr_nat_rules
:
    RULES srn_definition?
;

srn_definition
:
    name = variable
    srn_uuid ?
    (
        srn_active_active_device_binding
        | srn_description
        | srn_destination_translation
        | srn_source_translation
        | srn_to
        | srn_from
        | srn_to_interface
        | srn_source
        | srn_destination
        | srn_service
        | srn_disabled
    )?
;

srn_uuid
: UUID
 | ~(
    ACTIVE_ACTIVE_DEVICE_BINDING
    | DESCRIPTION
    | DESTINATION_TRANSLATION
    | SOURCE_TRANSLATION
    | TO
    | FROM
    | SOURCE
    | DESTINATION
    | SERVICE
    | DISABLED
    | NEWLINE
 )
;

active_active_device_binding_val
:
    uint8
    | BOTH
    | PRIMARY
;

srn_active_active_device_binding
:
    ACTIVE_ACTIVE_DEVICE_BINDING bind = active_active_device_binding_val
;

srn_destination_translation
:
    DESTINATION_TRANSLATION (
        srndt_translated_address
        | srndt_translated_port
    )
;

srn_source_translation
:
    SOURCE_TRANSLATION (
        srnst_dynamic_ip_and_port
        | srnst_static_ip
    )
;

srn_from
:
    FROM variable_list
;

srn_to
:
    TO zone = variable
;

srn_to_interface
:
    TO_INTERFACE interface = dotted_variable
;


srn_destination
:
    DESTINATION src_or_dst_list
;

srn_source
:
    SOURCE src_or_dst_list
;

srn_service
:
    SERVICE service = variable
;

srn_disabled
:
    DISABLED yn = yes_or_no
;

srn_description
:
    DESCRIPTION description = value
;

srnst_dynamic_ip_and_port
:
    DYNAMIC_IP_AND_PORT srnst_translated_address
;

srndt_translated_address
:
    TRANSLATED_ADDRESS translated_address_list_item
;

srndt_translated_port
:
    TRANSLATED_PORT port = port_number
;

srnst_translated_address
:
    TRANSLATED_ADDRESS translated_address_list
;

translated_address_list
:
    (
        translated_address_list_item
        |
        (
            OPEN_BRACKET
            (
                translated_address_list_item
            )*
            CLOSE_BRACKET
        )
    )
;

translated_address_list_item
:
    (
        address = ip_address
        | prefix = ip_prefix
        | range = ip_range
        | name = variable
    )
;

srnst_static_ip
:
    STATIC_IP (
        srnsti_bi_directional
        | srnsti_translated_address
    )
;

srnsti_bi_directional
:
    BI_DIRECTIONAL yes_or_no
;

srnsti_translated_address
:
    TRANSLATED_ADDRESS (
        address = ip_address
        | name = variable
    )
;
