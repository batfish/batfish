parser grammar A10_floating_ip;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_floating_ip: FLOATING_IP ip = ip_address fip_option* NEWLINE;

fip_option: fipo_ha_group;

fipo_ha_group: HA_GROUP id = ha_group_id;
