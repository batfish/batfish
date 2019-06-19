parser grammar F5BigipImishParser;

/* This is only needed if parser grammar is spread across files */
import
F5BigipImish_common, F5BigipImish_access_list, F5BigipImish_bgp, F5BigipImish_route_map;

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

s_ip_prefix_list
:
  IP PREFIX_LIST name = word SEQ num = uint32 action = line_action prefix =
  ip_prefix
  (
    LE le = ip_prefix_length
    | GE ge = ip_prefix_length
  )* NEWLINE
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
    | INTERFACE
    | SERVICE
  ) null_rest_of_line
;

statement
:
  s_access_list
  | s_line
  | s_null
  | s_ip_prefix_list
  | s_route_map
  | s_router_bgp
  | s_end
;
