parser grammar NecIx_ip;

options {
  tokenVocab = NecIxLexer;
}

s_ip
:
  IP
  (
    ip_access_list
    | ip_null
    | ip_prefix_list
    | ip_route
  )
;

// --------------------------------------------------------------------------------- access-list

ip_access_list
:
  ACCESS_LIST name = variable
  (
    ipal_entry
    | ipal_sequence_mode
  )
;

ipal_sequence_mode
:
  SEQUENCE_MODE interval = uint? NEWLINE
;

ipal_entry
:
  (
    seq = uint
  )? action = ( PERMIT | DENY )
  (
    ipale_icmp
    | ipale_other
    | ipale_tcp
    | ipale_udp
  ) acl_option* NEWLINE
;

ipale_icmp
:
  ICMP
  (
    TYPE icmp_type = uint
    (
      CODE icmp_code = uint
    )?
    | icmp_message = WORD
  )? src = acl_src dst = acl_dst
;

// `ip` (all protocols) or a protocol number.
ipale_other
:
  (
    IP
    | protocol = uint
  ) src = acl_src dst = acl_dst
;

ipale_tcp
:
  TCP ( flags += tcp_flag )* src = acl_src
  (
    SPORT sport = port_spec
  )? dst = acl_dst
  (
    DPORT dport = port_spec
  )?
;

ipale_udp
:
  UDP src = acl_src
  (
    SPORT sport = port_spec
  )? dst = acl_dst
  (
    DPORT dport = port_spec
  )?
;

acl_src
:
  SRC address = acl_address
  | SRC_DOMAIN domain = variable
;

acl_dst
:
  DEST address = acl_address
  | DEST_DOMAIN domain = variable
;

acl_address
:
  ANY
  | prefix = IP_PREFIX
  | ip = IP_ADDRESS wildcard = IP_ADDRESS
;

port_spec
:
  ANY
  | op = ( EQ | NEQ | LT | GT ) port = uint
  | RANGE low = uint high = uint
;

tcp_flag
:
  ACK
  | ESTABLISHED
  | FIN
  | PSH
  | RST
  | SYN
  | URG
;

acl_option
:
  DSCP dscp = uint
  | FRAGMENTS
  | LINKMGR ( WORD uint )*
  | PRECEDENCE precedence = uint
  | TOS tos = uint
;

// --------------------------------------------------------------------------------- prefix-list

ip_prefix_list
:
  PREFIX_LIST name = variable seq = uint action = ( PERMIT | DENY )
  (
    ANY
    | prefix = IP_PREFIX
    (
      MIN min = uint
    )?
    (
      MAX max = uint
    )?
  ) NEWLINE
;

// --------------------------------------------------------------------------------- static route

ip_route
:
  ROUTE
  (
    VRF vrf = variable
  )?
  (
    prefix = IP_PREFIX
    | DEFAULT
  ) ipr_next_hop ipr_option* NEWLINE
;

ipr_next_hop
:
  next_hop_ip = IP_ADDRESS
  (
    iface = WORD
  )?
  | iface = WORD
  (
    next_hop_ip = IP_ADDRESS
    | DHCP
  )?
;

ipr_option
:
  CONNECTED
  | DISTANCE distance = uint
  | METRIC metric = uint
  | TAG tag = uint
;

// -------------------------------------------------------------------------------------- others

ip_null
:
  (
    DHCP // DHCP server profiles
    | MULTIPATH
    | UFS_CACHE
  ) null_rest_of_line null_block_line*
;
