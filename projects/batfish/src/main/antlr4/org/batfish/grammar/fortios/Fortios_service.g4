parser grammar Fortios_service;

options {
  tokenVocab = FortiosLexer;
}

cf_service: SERVICE cfs_custom;

cfs_custom: CUSTOM newline cfsc*;

cfsc: cfsc_edit | cfsc_rename;

cfsc_rename: RENAME current_name = service_name TO new_name = service_name newline;

cfsc_edit: EDIT service_name newline cfsce* NEXT newline;

cfsce: SET cfsc_set_singletons;

cfsc_set_singletons:
    cfsc_set_comment
    | cfsc_set_icmpcode
    | cfsc_set_icmptype
    | cfsc_set_protocol
    | cfsc_set_protocol_number
    | cfsc_set_sctp_portrange
    | cfsc_set_tcp_portrange
    | cfsc_set_udp_portrange
;

cfsc_set_comment: COMMENT comment = str newline;

cfsc_set_icmpcode: ICMPCODE code = uint8 newline;

cfsc_set_icmptype: ICMPTYPE type = uint8 newline;

cfsc_set_protocol: PROTOCOL protocol = service_protocol newline;

cfsc_set_protocol_number: PROTOCOL_NUMBER number = ip_protocol_number newline;

cfsc_set_sctp_portrange: SCTP_PORTRANGE value = service_port_ranges newline;

cfsc_set_tcp_portrange: TCP_PORTRANGE value = service_port_ranges newline;

cfsc_set_udp_portrange: UDP_PORTRANGE value = service_port_ranges newline;

// Up to 79 characters
service_name: str;

service_protocol
:
    ICMP
    | ICMP6
    | IP_UPPER
    | TCP_UDP_SCTP
;

service_port_ranges: service_port_range+;

// TODO: get from a portange lexing mode
service_port_range: dst_ports = port_range (COLON src_ports = port_range)?;

// 0-254
ip_protocol_number: uint8;
