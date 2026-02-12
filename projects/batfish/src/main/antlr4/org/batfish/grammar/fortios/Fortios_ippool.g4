parser grammar Fortios_ippool;

options {
  tokenVocab = FortiosLexer;
}

cf_ippool: IPPOOL newline cfip*;

cfip: cfip_delete | cfip_edit;

cfip_delete: DELETE ippool_name newline;

cfip_edit: EDIT ippool_name newline cfipe* NEXT newline;

cfipe: cfip_set | cfip_unset;

cfip_set: SET (cfip_set_prefix | cfip_set_ge | cfip_set_le | cfip_set_startip | cfip_set_endip | cfip_set_type | cfip_set_comments | cfip_set_associated_interface);

cfip_unset: UNSET (cfip_unset_ge | cfip_unset_le);

cfip_set_prefix: PREFIX prefix_ip = ip_address netmask_value = ip_address newline;

cfip_set_ge: GE ge_port = uint16 newline;

cfip_set_le: LE le_port = uint16 newline;

cfip_unset_ge: GE newline;

cfip_unset_le: LE newline;

cfip_set_startip: STARTIP ip = ip_address newline;

cfip_set_endip: ENDIP ip = ip_address newline;

cfip_set_type: TYPE ippool_type newline;

cfip_set_comments: COMMENTS comments = str newline;

cfip_set_associated_interface: ASSOCIATED_INTERFACE name = interface_or_zone_name newline;

ippool_type:
    OVERLOAD
    | ONE_TO_ONE
    | FIXED_PORT_RANGE
    | PORT_BLOCK_ALLOCATION;
