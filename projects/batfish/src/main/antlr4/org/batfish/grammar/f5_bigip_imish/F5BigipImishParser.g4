parser grammar F5BigipImishParser;

/* This is only needed if parser grammar is spread across files */
import
  F5BigipImish_common,
  F5BigipImish_access_list,
  F5BigipImish_bgp,
  F5BigipImish_interface,
  F5BigipImish_ospf,
  F5BigipImish_route_map;

options {
  superClass =
  'org.batfish.grammar.f5_bigip_imish.parsing.F5BigipImishBaseParser';
  tokenVocab = F5BigipImishLexer;
}

// goal rule

f5_bigip_imish_configuration
:
  NEWLINE? statement+ NEWLINE? EOF
;

// other rules

s_end
:
  END NEWLINE
;

s_ip
:
  IP
  (
    s_ip_as_path
    | s_ip_prefix_list
    | s_ip_route
  )
;

s_ip_as_path
:
  AS_PATH ACCESS_LIST name = word (PERMIT | DENY) value = word NEWLINE
;

s_ip_prefix_list
:
  PREFIX_LIST name = word SEQ num = uint32 action = line_action prefix =
  ip_prefix
  (
    LE le = ip_prefix_length
    | GE ge = ip_prefix_length
  )* NEWLINE
;

s_ip_route
:
  ROUTE prefix = ip_prefix nhip = ip_address NEWLINE
;

s_line
:
  LINE
  (
    l_con
    | l_vty
  )
;

l_con
:
  CON num = uint32 NEWLINE l_login*
;

l_login
:
  LOGIN NEWLINE
;

l_vty
:
  VTY low = uint32 high = uint32 NEWLINE l_login*
;

s_null
:
  NO?
  (
    BFD
    | SERVICE
  ) null_rest_of_line
;

statement
:
  s_access_list
  | s_interface
  | s_line
  | s_null
  | s_ip
  | s_route_map
  | s_router
  | s_end
;

s_router
:
  ROUTER
  (
    router_bgp
    | router_ospf
  )
;
