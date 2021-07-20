parser grammar CheckPointGateway_bonding;

options {
    tokenVocab = CheckPointGatewayLexer;
}

a_bonding_group: BONDING GROUP bonding_group_number abg?;

abg: abg_interface;

abg_interface: INTERFACE bonding_group_member_interface_name;

s_bonding_group: BONDING GROUP bonding_group_number sbg;

sbg: sbg_lacp_rate | sbg_mode | sbg_xmit_hash_policy;

sbg_lacp_rate: LACP_RATE lacp_rate;

lacp_rate: FAST | SLOW;

sbg_mode: MODE bonding_group_mode;

bonding_group_mode: ACTIVE_BACKUP | EIGHT_ZERO_TWO_THREE_AD | ROUND_ROBIN | XOR;

sbg_xmit_hash_policy: XMIT_HASH_POLICY xmit_hash_policy;

xmit_hash_policy: LAYER2 | LAYER3_4;

bonding_group_member_interface_name: interface_name;
