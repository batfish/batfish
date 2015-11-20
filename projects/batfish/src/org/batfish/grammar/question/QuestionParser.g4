parser grammar QuestionParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = QuestionLexer;
}

@header {
package org.batfish.grammar.question;
}

@members {
   private java.util.Map<String, VariableType> _typeBindings = new java.util.HashMap<String, VariableType>(); 
   
}

action
:
   ACCEPT
   | DROP
;

action_constraint
:
   action
   | VARIABLE
;

and_expr
:
   AND OPEN_BRACE conjuncts += boolean_expr
   (
      COMMA conjuncts += boolean_expr
   )* CLOSE_BRACE
;

assertion [String scope]
:
   ASSERT OPEN_BRACE boolean_expr CLOSE_BRACE
   (
      ONFAILURE OPEN_BRACE
      (
         statements += statement [scope]
      )* CLOSE_BRACE
   )?
;

assignment
:
   int_assignment
;

bgp_neighbor_boolean_expr
:
   BGP_NEIGHBOR PERIOD bgp_neighbor_has_generated_route_boolean_expr
;

bgp_neighbor_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

bgp_neighbor_int_expr
:
   BGP_NEIGHBOR PERIOD
   (
      bgp_neighbor_local_as_int_expr
      | bgp_neighbor_remote_as_int_expr
   )
;

bgp_neighbor_ip_expr
:
   BGP_NEIGHBOR PERIOD
   (
      bgp_neighbor_local_ip_ip_expr
      | bgp_neighbor_remote_ip_ip_expr
   )
;

bgp_neighbor_local_as_int_expr
:
   LOCAL_AS
;

bgp_neighbor_local_ip_ip_expr
:
   LOCAL_IP
;

bgp_neighbor_remote_as_int_expr
:
   REMOTE_AS
;

bgp_neighbor_remote_ip_ip_expr
:
   REMOTE_IP
;

boolean_expr
:
   and_expr
   | set_contains_expr
   | eq_expr
   | gt_expr
   | if_expr
   | false_expr
   | neq_expr
   | not_expr
   | or_expr
   | property_boolean_expr
   | true_expr
;

default_binding
:
   VARIABLE EQUALS
   (
      action
      | integer_literal
      | IP_ADDRESS
      | IP_PREFIX
      | ip_constraint_complex
      | range
      | REGEX
      | STRING_LITERAL
   ) SEMICOLON
;

defaults
:
   DEFAULTS OPEN_BRACE default_binding+ CLOSE_BRACE
;

eq_expr
:
   (
      lhs_int = int_expr DOUBLE_EQUALS rhs_int = int_expr
   )
   |
   (
      lhs_string = string_expr DOUBLE_EQUALS rhs_string = string_expr
   )
;

explicit_flow
:
   FLOW OPEN_PAREN
   (
      flow_constraint
      (
         COMMA flow_constraint
      )*
   )? CLOSE_PAREN
;

expr
:
   boolean_expr
   | int_expr
   | ip_expr
   | ipsec_vpn_expr
   | prefix_expr
   | route_filter_line_expr
   | string_expr
;

failure_question
:
   FAILURE
;

false_expr
:
   FALSE
;

flow_constraint
:
   flow_constraint_ingress_node
   | flow_constraint_ip_protocol
   | flow_constraint_dst_ip
   | flow_constraint_dst_port
   | flow_constraint_src_ip
   | flow_constraint_src_port
;

flow_constraint_ingress_node
:
   INGRESS_NODE EQUALS
   (
      ingress_node = STRING_LITERAL
      | ingress_node = VARIABLE
   )
;

flow_constraint_ip_protocol
:
   IP_PROTOCOL EQUALS
   (
      ip_protocol = DEC
      | ip_protocol = VARIABLE
   )
;

flow_constraint_dst_ip
:
   DST_IP EQUALS
   (
      dst_ip = IP_ADDRESS
      | dst_ip = VARIABLE
   )
;

flow_constraint_dst_port
:
   DST_PORT EQUALS
   (
      dst_port = DEC
      | dst_port = VARIABLE
   )
;

flow_constraint_src_ip
:
   SRC_IP EQUALS
   (
      src_ip = IP_ADDRESS
      | src_ip = VARIABLE
   )
;

flow_constraint_src_port
:
   SRC_PORT EQUALS
   (
      src_port = DEC
      | src_port = VARIABLE
   )
;

foreach_bgp_neighbor_statement
:
   FOREACH BGP_NEIGHBOR OPEN_BRACE statement ["bgp_neighbor"]+ CLOSE_BRACE
;

foreach_clause_statement
:
   FOREACH CLAUSE OPEN_BRACE statement ["clause"]+ CLOSE_BRACE
;

foreach_generated_route_statement
:
   FOREACH GENERATED_ROUTE OPEN_BRACE statement ["generated_route"]+
   CLOSE_BRACE
;

foreach_interface_statement
:
   FOREACH INTERFACE OPEN_BRACE statement ["interface"]+ CLOSE_BRACE
;

foreach_ipsec_vpn_statement
:
   FOREACH IPSEC_VPN OPEN_BRACE statement ["ipsec_vpn"]+ CLOSE_BRACE
;

foreach_line_statement
:
   FOREACH LINE OPEN_BRACE statement ["route_filter_line"]+ CLOSE_BRACE
;

foreach_match_protocol_statement
:
   FOREACH MATCH_PROTOCOL OPEN_BRACE statement ["match_protocol"]+ CLOSE_BRACE
;

foreach_match_route_filter_statement
:
   FOREACH MATCH_ROUTE_FILTER OPEN_BRACE statement ["match_route_filter"]+
   CLOSE_BRACE
;

foreach_node_bgp_generated_route_statement
:
   FOREACH NODE PERIOD BGP PERIOD GENERATED_ROUTE OPEN_BRACE statement
   ["generated_route"]+ CLOSE_BRACE
;

foreach_node_statement
:
   FOREACH NODE OPEN_BRACE statement ["node"]+ CLOSE_BRACE
;

foreach_ospf_outbound_policy_statement
:
   FOREACH OSPF_OUTBOUND_POLICY OPEN_BRACE statement ["policy"]+ CLOSE_BRACE
;

foreach_protocol_statement
:
   FOREACH PROTOCOL OPEN_BRACE statement ["protocol"]+ CLOSE_BRACE
;

foreach_route_filter_statement
:
   FOREACH ROUTE_FILTER OPEN_BRACE statement ["route_filter"]+ CLOSE_BRACE
;

foreach_route_filter_in_set_statement
:
   FOREACH ROUTE_FILTER COLON set = VARIABLE
   {_typeBindings.get($set.getText()) == VariableType.SET_ROUTE_FILTER}?

   OPEN_BRACE statement ["route_filter"]+ CLOSE_BRACE
;

foreach_static_route_statement
:
   FOREACH STATIC_ROUTE OPEN_BRACE statement ["static_route"]+ CLOSE_BRACE
;

generated_route_prefix_expr
:
   GENERATED_ROUTE PERIOD generated_route_prefix_prefix_expr
;

generated_route_prefix_prefix_expr
:
   PREFIX
;

gt_expr
:
   lhs = int_expr GT rhs = int_expr
;

if_expr
:
   IF OPEN_PAREN antecedent = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   consequent = boolean_expr CLOSE_BRACE
;

if_statement [String scope]
:
   IF OPEN_PAREN guard = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   (
      true_statements += statement [scope]
   )* CLOSE_BRACE
   (
      ELSE OPEN_BRACE
      (
         false_statements += statement [scope]
      )* CLOSE_BRACE
   )?
;

ingress_path_question
:
   INGRESS_PATH
;

int_assignment
:
   VARIABLE COLON_EQUALS int_expr SEMICOLON
;

int_expr
:
   multiplicand1 = int_expr ASTERISK multiplicand2 = int_expr
   | dividend = int_expr FORWARD_SLASH divisor = int_expr
   | addend1 = int_expr PLUS addend2 = int_expr
   | subtrahend = int_expr MINUS minuend = int_expr
   | OPEN_PAREN parenthesized = int_expr CLOSE_PAREN
   | val_int_expr
;

integer_literal
:
   MINUS? DEC
;

interface_boolean_expr
:
   INTERFACE PERIOD
   (
      interface_enabled_boolean_expr
      | interface_has_ip_boolean_expr
      | interface_isis_boolean_expr
      | interface_isloopback_boolean_expr
      | interface_ospf_boolean_expr
   )
;

interface_enabled_boolean_expr
:
   ENABLED
;

interface_has_ip_boolean_expr
:
   HAS_IP
;

interface_ip_expr
:
   INTERFACE PERIOD interface_ip_ip_expr
;

interface_ip_ip_expr
:
   IP
;

interface_isis_l1_active_boolean_expr
:
   L1_ACTIVE
;

interface_isis_l1_passive_boolean_expr
:
   L1_PASSIVE
;

interface_isis_l2_active_boolean_expr
:
   L2_ACTIVE
;

interface_isis_l2_passive_boolean_expr
:
   L2_PASSIVE
;

interface_isis_boolean_expr
:
   ISIS PERIOD
   (
      interface_isis_l1_active_boolean_expr
      | interface_isis_l1_passive_boolean_expr
      | interface_isis_l2_active_boolean_expr
      | interface_isis_l2_passive_boolean_expr
   )
;

interface_isloopback_boolean_expr
:
   IS_LOOPBACK
;

interface_name_string_expr
:
   NAME
;

interface_ospf_active_boolean_expr
:
   ACTIVE
;

interface_ospf_passive_boolean_expr
:
   PASSIVE
;

interface_ospf_boolean_expr
:
   OSPF PERIOD
   (
      interface_ospf_active_boolean_expr
      | interface_ospf_passive_boolean_expr
   )
;

interface_prefix_expr
:
   INTERFACE PERIOD interface_prefix_prefix_expr
;

interface_prefix_prefix_expr
:
   PREFIX
;

interface_string_expr
:
   INTERFACE PERIOD interface_name_string_expr
;

ip_constraint
:
   ip_constraint_complex
   | ip_constraint_simple
   | VARIABLE
;

ip_constraint_complex
:
   OPEN_BRACE ip_constraint_simple
   (
      COMMA ip_constraint_simple
   )* CLOSE_BRACE
;

ip_constraint_simple
:
   IP_ADDRESS
   | IP_PREFIX
;

ip_expr
:
   bgp_neighbor_ip_expr
   | interface_ip_expr
   | static_route_ip_expr
;

ipsec_vpn_boolean_expr
:
   ipsec_vpn_expr PERIOD
   (
      ipsec_vpn_compatible_ike_proposals_boolean_expr
      | ipsec_vpn_compatible_ipsec_proposals_boolean_expr
      | ipsec_vpn_has_remote_ipsec_vpn_boolean_expr
   )
;

ipsec_vpn_compatible_ike_proposals_boolean_expr
:
   COMPATIBLE_IKE_PROPOSALS
;

ipsec_vpn_compatible_ipsec_proposals_boolean_expr
:
   COMPATIBLE_IPSEC_PROPOSALS
;

ipsec_vpn_expr
:
   IPSEC_VPN
   | ipsec_vpn_ipsec_vpn_expr
;

ipsec_vpn_has_remote_ipsec_vpn_boolean_expr
:
   HAS_REMOTE_IPSEC_VPN
;

ipsec_vpn_ike_gateway_name_string_expr
:
   IKE_GATEWAY_NAME
;

ipsec_vpn_ike_policy_name_string_expr
:
   IKE_POLICY_NAME
;

ipsec_vpn_ipsec_policy_name_string_expr
:
   IPSEC_POLICY_NAME
;

ipsec_vpn_ipsec_vpn_expr
:
   IPSEC_VPN PERIOD
   (
      ipsec_vpn_remote_ipsec_vpn_ipsec_vpn_expr
   )
;

ipsec_vpn_name_string_expr
:
   NAME
;

ipsec_vpn_owner_name_string_expr
:
   OWNER_NAME
;

ipsec_vpn_pre_shared_key_string_expr
:
   PRE_SHARED_KEY
;

ipsec_vpn_remote_ipsec_vpn_ipsec_vpn_expr
:
   REMOTE_IPSEC_VPN
;

ipsec_vpn_string_expr
:
   ipsec_vpn_expr PERIOD
   (
      ipsec_vpn_ike_gateway_name_string_expr
      | ipsec_vpn_ike_policy_name_string_expr
      | ipsec_vpn_ipsec_policy_name_string_expr
      | ipsec_vpn_name_string_expr
      | ipsec_vpn_owner_name_string_expr
      | ipsec_vpn_pre_shared_key_string_expr
   )
;

literal_int_expr
:
   DEC
;

local_path_question
:
   LOCAL_PATH
;

method [String scope]
:
   caller = VARIABLE PERIOD typed_method [scope, $caller.getText()] SEMICOLON
;

multipath_question
:
   MULTIPATH
;

neq_expr
:
   lhs = int_expr NOT_EQUALS rhs = int_expr
;

node_bgp_boolean_expr
:
   BGP PERIOD
   (
      node_bgp_configured_boolean_expr
      | node_bgp_has_generated_route_boolean_expr
   )
;

node_bgp_configured_boolean_expr
:
   CONFIGURED
;

node_bgp_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

node_boolean_expr
:
   NODE PERIOD
   (
      node_bgp_boolean_expr
      | node_has_generated_route_boolean_expr
      | node_isis_boolean_expr
      | node_ospf_boolean_expr
      | node_static_boolean_expr
   )
;

node_constraint
:
   REGEX
   | STRING_LITERAL
   | VARIABLE
;

node_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

node_isis_boolean_expr
:
   ISIS PERIOD node_isis_configured_boolean_expr
;

node_isis_configured_boolean_expr
:
   CONFIGURED
;

node_name_string_expr
:
   NAME
;

node_ospf_boolean_expr
:
   OSPF PERIOD node_ospf_configured_boolean_expr
;

node_ospf_configured_boolean_expr
:
   CONFIGURED
;

node_static_boolean_expr
:
   STATIC PERIOD node_static_configured_boolean_expr
;

node_static_configured_boolean_expr
:
   CONFIGURED
;

node_string_expr
:
   NODE PERIOD node_name_string_expr
;

not_expr
:
   NOT OPEN_BRACE boolean_expr CLOSE_BRACE
;

or_expr
:
   OR OPEN_BRACE disjuncts += boolean_expr
   (
      COMMA disjuncts += boolean_expr
   )* CLOSE_BRACE
;

prefix_expr
:
   generated_route_prefix_expr
   | interface_prefix_expr
   | static_route_prefix_expr
;

printf_statement
:
   PRINTF OPEN_PAREN format_string = string_expr
   (
      COMMA replacements += expr
   )* CLOSE_PAREN SEMICOLON
;

property_boolean_expr
:
   bgp_neighbor_boolean_expr
   | interface_boolean_expr
   | ipsec_vpn_boolean_expr
   | node_boolean_expr
   | static_route_boolean_expr
;

protocol_name_string_expr
:
   NAME
;

protocol_string_expr
:
   PROTOCOL PERIOD protocol_name_string_expr
;

question
:
   defaults?
   (
      failure_question
      | ingress_path_question
      | local_path_question
      | multipath_question
      | reachability_question
      | traceroute_question
      | verify_question
   )
;

range
:
   range_list += subrange
   (
      COMMA range_list += subrange
   )*
;

range_constraint
:
   (
      OPEN_BRACE range CLOSE_BRACE
   )
   | subrange
   | VARIABLE
;

reachability_constraint
:
   reachability_constraint_action
   | reachability_constraint_dst_ip
   | reachability_constraint_dst_port
   | reachability_constraint_final_node
   | reachability_constraint_ingress_node
   | reachability_constraint_ip_protocol
   | reachability_constraint_src_ip
   | reachability_constraint_src_port
;

reachability_constraint_action
:
   ACTION EQUALS action_constraint
;

reachability_constraint_dst_ip
:
   DST_IP EQUALS ip_constraint
;

reachability_constraint_dst_port
:
   DST_PORT EQUALS range_constraint
;

reachability_constraint_final_node
:
   FINAL_NODE EQUALS node_constraint
;

reachability_constraint_ingress_node
:
   INGRESS_NODE EQUALS node_constraint
;

reachability_constraint_ip_protocol
:
   IP_PROTOCOL EQUALS range_constraint
;

reachability_constraint_src_ip
:
   SRC_IP EQUALS ip_constraint
;

reachability_constraint_src_port
:
   SRC_PORT EQUALS range_constraint
;

reachability_question
:
   REACHABILITY OPEN_BRACE reachability_constraint
   (
      COMMA reachability_constraint
   )* CLOSE_BRACE
;

route_filter_expr
:
   route_filter_route_filter_expr
;

route_filter_line_expr
:
   route_filter_line_line_expr
;

route_filter_line_line_expr
:
   LINE
;

route_filter_name_string_expr
:
   NAME
;

route_filter_route_filter_expr
:
   ROUTE_FILTER
;

route_filter_string_expr
:
   ROUTE_FILTER PERIOD route_filter_name_string_expr
;

set_add_method [VariableType type, String caller]
:
   ADD OPEN_PAREN
   (
      {$type == VariableType.SET_IP}?

      ip_expr
      |
      {$type == VariableType.SET_STRING}?

      expr
      |
      {$type == VariableType.SET_ROUTE_FILTER}?

      route_filter_expr
   ) CLOSE_PAREN
;

set_contains_expr
locals [VariableType type]
:
   caller = VARIABLE
   { $type = _typeBindings.get($caller.getText()); }

   PERIOD CONTAINS OPEN_PAREN
   (
      {$type == VariableType.SET_IP}?

      ip_expr
      |
      {$type == VariableType.SET_STRING}?

      expr
      |
      {$type == VariableType.SET_ROUTE_FILTER}?

      route_filter_expr
   ) CLOSE_PAREN
;

set_clear_method [VariableType type, String caller]
:
   CLEAR
;

set_declaration_statement
locals [String typeStr, VariableType type, VariableType oldType]
:
   var = VARIABLE COLON
   (
      {$oldType = _typeBindings.get($var.getText());}

      SET
      {$typeStr = "set<";}

      (
         settype = IP
         | settype = ROUTE_FILTER
         | settype = STRING
      )
      {
         $typeStr += $settype.getText() + ">";
         $type = VariableType.fromString($typeStr);
         _typeBindings.put($var.getText(), $type);
      }

   )
   {
      $oldType == null || $type == $oldType 
      /* Same variable declared with two different types*/
   }?

   SEMICOLON
;

set_size_int_expr
locals [VariableType type]
:
   caller = VARIABLE
   {$type = _typeBindings.get($caller.getText());}

   PERIOD SIZE
;

statement [String scope]
:
   assertion [scope]
   | assignment
   |
   {$scope.equals("node")}?

   foreach_bgp_neighbor_statement
   |
   {$scope.equals("policy")}?

   foreach_clause_statement
   |
   {$scope.equals("bgp_neighbor") || $scope.equals("node")}?

   foreach_generated_route_statement
   |
   {$scope.equals("node")}?

   foreach_interface_statement
   |
   {$scope.equals("node")}?

   foreach_ipsec_vpn_statement
   |
   {$scope.equals("route_filter")}?

   foreach_line_statement
   |
   {$scope.equals("clause")}?

   foreach_match_protocol_statement
   |
   {$scope.equals("clause")}?

   foreach_match_route_filter_statement
   |
   {$scope.equals("node")}?

   foreach_node_bgp_generated_route_statement
   |
   {$scope.equals("verify")}?

   foreach_node_statement
   |
   {$scope.equals("node")}?

   foreach_ospf_outbound_policy_statement
   |
   {$scope.equals("match_protocol")}?

   foreach_protocol_statement
   | foreach_route_filter_in_set_statement
   |
   {$scope.equals("match_route_filter")}?

   foreach_route_filter_statement
   |
   {$scope.equals("node")}?

   foreach_static_route_statement
   | if_statement [scope]
   | method [scope]
   | printf_statement
   | set_declaration_statement
;

static_route_administrative_cost_int_expr
:
   ADMINISTRATIVE_COST
;

static_route_boolean_expr
:
   STATIC_ROUTE PERIOD
   (
      static_route_has_next_hop_interface_boolean_expr
      | static_route_has_next_hop_ip_boolean_expr
   )
;

static_route_has_next_hop_interface_boolean_expr
:
   HAS_NEXT_HOP_INTERFACE
;

static_route_has_next_hop_ip_boolean_expr
:
   HAS_NEXT_HOP_IP
;

static_route_int_expr
:
   STATIC_ROUTE PERIOD static_route_administrative_cost_int_expr
;

static_route_ip_expr
:
   STATIC_ROUTE PERIOD static_route_next_hop_ip_ip_expr
;

static_route_next_hop_interface_string_expr
:
   NEXT_HOP_INTERFACE
;

static_route_next_hop_ip_ip_expr
:
   NEXT_HOP_IP
;

static_route_prefix_expr
:
   STATIC_ROUTE PERIOD static_route_prefix_prefix_expr
;

static_route_prefix_prefix_expr
:
   PREFIX
;

static_route_string_expr
:
   STATIC_ROUTE PERIOD static_route_next_hop_interface_string_expr
;

string_expr
:
   interface_string_expr
   | ipsec_vpn_string_expr
   | node_string_expr
   | protocol_string_expr
   | route_filter_string_expr
   | static_route_string_expr
   | string_literal_string_expr
;

string_literal_string_expr
:
   STRING_LITERAL
;

subrange
:
   low = DEC
   (
      MINUS high = DEC
   )?
;

traceroute_question
:
   TRACEROUTE OPEN_BRACE
   (
      explicit_flow SEMICOLON
   )+ CLOSE_BRACE
;

true_expr
:
   TRUE
;

typed_method [String scope, String caller]
locals [VariableType type]
:
   {$type = _typeBindings.get($caller);}

   set_add_method [$type, caller]
   | set_clear_method [$type, caller]
;

val_int_expr
:
   bgp_neighbor_int_expr
   | literal_int_expr
   | set_size_int_expr
   | static_route_int_expr
   | var_int_expr
;

var_int_expr
:
   VARIABLE
;

verify_question
:
   VERIFY OPEN_BRACE statement ["verify"]+ CLOSE_BRACE
;
