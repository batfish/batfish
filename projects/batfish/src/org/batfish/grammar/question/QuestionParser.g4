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
    
   private java.util.Set<String> _immutableVars = new java.util.HashSet<String>();

   private String getParserPosition() {
      return "" + _ctx.start.getLine() + ":" + _ctx.start.getCharPositionInLine();
   }
    
   private Token next() {
      return _input.LT(1);
   }
   
   private Token second() {
      return _input.LT(2);
   }
    
   private boolean v(VariableType type) {
      return next().getType() == VARIABLE && retrieveTypeBinding(next().getText()) == type;
   }

   private boolean vp(VariableType type) {
      return next().getType() != VARIABLE || second().getType() == PERIOD || retrieveTypeBinding(next().getText()) == type;
   }

   private void assertMutable(String var) {
      if (_immutableVars.contains(var)) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Attempt to alter immutable variable: \"" + var + "\"");
      }
   }

   private void createTypeBinding(String var, VariableType type) {
      assertMutable(var);
      VariableType oldType = _typeBindings.get(var);
      if (oldType != null) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Attempt to create variable \"" + var + "\" with type: \"" + type + "\", but it already exists and has previously assigned type: \"" + oldType + "\"");
      }
      else {
         _typeBindings.put(var, type);
      }
   }
    
   private void createOrVerifyTypeBinding(String var, VariableType type) {
      assertMutable(var);
      VariableType oldType = _typeBindings.get(var);
      if (oldType != null && oldType != type) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Attempt to update variable \"" + var + "\" with value of type: \"" + type + "\", but it has previously assigned type: \"" + oldType + "\"");
      }
      else {
         _typeBindings.put(var, type);
      }
   }
    
   private VariableType retrieveTypeBinding(String var) {
      VariableType ret = _typeBindings.get(var);
      if (ret == null) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Missing type for variable: \"" + var + "\"");
      }
      return ret;
   }

   private void assertTypeBinding(String var, VariableType expectedType) {
      VariableType actualType = retrieveTypeBinding(var);
      if (actualType != expectedType) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Variable '" + var + "' is of type '" + actualType.toString() + "', but expected type '" + expectedType.toString() + "'");
      }
   }

   private void assertMatchingExprType(ExprContext lhs, ExprContext rhs) {
      VariableType lhsType = lhs.varType;
      VariableType rhsType = rhs.varType;
      if (lhsType != rhsType) {
         throw new org.batfish.common.BatfishException(getParserPosition() + ": Expression '" + lhs.getText() + "' of type '" + lhsType.toString() + "' cannot be compared to expression '" + rhs.getText()+ "' of type '" + rhsType.toString());
      }
   }

}

acl_reachability_question
:
   ACL_REACHABILITY OPEN_BRACE CLOSE_BRACE
;

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
   var = VARIABLE COLON_EQUALS
   (
      bgp_neighbor_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.BGP_NEIGHBOR);}

      | boolean_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.BOOLEAN);}

      | int_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.INT);}

      | interface_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.INTERFACE);}

      | ip_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.IP);}

      | ipsec_vpn_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.IPSEC_VPN);}

      | map_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.MAP);}

      | node_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.NODE);}

      | policy_map_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.POLICY_MAP);}

      | policy_map_clause_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.POLICY_MAP_CLAUSE);}

      | prefix_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.PREFIX);}

      | prefix_space_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.PREFIX_SPACE);}

      | route_filter_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.ROUTE_FILTER);}

      | route_filter_line_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.ROUTE_FILTER_LINE);}

      | set_prefix_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.SET_PREFIX);}

      | set_string_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.SET_STRING);}

      | static_route_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.STATIC_ROUTE);}

      | string_expr
      {createOrVerifyTypeBinding($var.getText(), VariableType.STRING);}

   ) SEMICOLON
;

bgp_neighbor_boolean_expr
:
   caller = bgp_neighbor_expr PERIOD
   (
      bgp_neighbor_has_generated_route_boolean_expr
      | bgp_neighbor_has_local_ip_boolean_expr
      | bgp_neighbor_has_remote_bgp_neighbor_boolean_expr
      | bgp_neighbor_has_single_remote_bgp_neighbor_boolean_expr
   )
;

bgp_neighbor_description_string_expr
:
   DESCRIPTION
;

bgp_neighbor_expr
:
   BGP_NEIGHBOR
   | REMOTE_BGP_NEIGHBOR
   |
   {v(VariableType.BGP_NEIGHBOR)}?

   var_bgp_neighbor_expr
;

bgp_neighbor_group_string_expr
:
   GROUP
;

bgp_neighbor_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

bgp_neighbor_has_local_ip_boolean_expr
:
   HAS_LOCAL_IP
;

bgp_neighbor_has_remote_bgp_neighbor_boolean_expr
:
   HAS_REMOTE_BGP_NEIGHBOR
;

bgp_neighbor_has_single_remote_bgp_neighbor_boolean_expr
:
   HAS_SINGLE_REMOTE_BGP_NEIGHBOR
;

bgp_neighbor_int_expr
:
   caller = bgp_neighbor_expr PERIOD
   (
      bgp_neighbor_local_as_int_expr
      | bgp_neighbor_remote_as_int_expr
   )
;

bgp_neighbor_ip_expr
:
   caller = bgp_neighbor_expr PERIOD
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

bgp_neighbor_name_string_expr
:
   NAME
;

bgp_neighbor_node_expr
:
   caller = bgp_neighbor_expr PERIOD
   (
      bgp_neighbor_owner_node_expr
   )
;

bgp_neighbor_owner_node_expr
:
   OWNER
;

bgp_neighbor_remote_as_int_expr
:
   REMOTE_AS
;

bgp_neighbor_remote_ip_ip_expr
:
   REMOTE_IP
;

bgp_neighbor_string_expr
:
   caller = bgp_neighbor_expr PERIOD
   (
      bgp_neighbor_description_string_expr
      | bgp_neighbor_group_string_expr
      | bgp_neighbor_name_string_expr
   )
;

boolean_expr
:
   and_expr
   | set_contains_expr
   | eq_expr
   | ge_expr
   | gt_expr
   | if_expr
   | false_expr
   | le_expr
   | lt_expr
   | neq_expr
   | not_expr
   | or_expr
   | property_boolean_expr
   | true_expr
   |
   |
   {v(VariableType.BOOLEAN)}?

   var_boolean_expr
   | lhs = boolean_expr
   (
      DOUBLE_EQUALS
      | NOT_EQUALS
   ) rhs = boolean_expr
;

boolean_literal
:
   TRUE
   | FALSE
;

default_binding
:
   var = VARIABLE EQUALS
   (
      action
      | boolean_literal
      {createTypeBinding($var.getText(), VariableType.BOOLEAN);}

      | integer_literal
      {createTypeBinding($var.getText(), VariableType.INT);}

      | IP_ADDRESS
      {createTypeBinding($var.getText(), VariableType.IP);}

      | IP_PREFIX
      {createTypeBinding($var.getText(), VariableType.PREFIX);}

      | ip_constraint_complex
      | range
      | REGEX
      |
      (
         SET str_set = STRING OPEN_BRACE
         (
            str_elem += string_literal_string_expr
            (
               COMMA str_elem += string_literal_string_expr
            )*
         )? CLOSE_BRACE
      )
      {createTypeBinding($var.getText(), VariableType.SET_STRING);}

      | str = string_literal_string_expr
      {createTypeBinding($var.getText(), VariableType.STRING);}

   ) SEMICOLON
;

defaults
:
   DEFAULTS OPEN_BRACE default_binding+ CLOSE_BRACE
;

eq_expr
:
   lhs = expr DOUBLE_EQUALS rhs = expr
   {assertMatchingExprType(_localctx.lhs, _localctx.rhs);}

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

expr returns [VariableType varType]
:
   {vp(VariableType.BGP_NEIGHBOR)}?

   bgp_neighbor_expr
   {$varType = VariableType.BGP_NEIGHBOR;}

   |
   {vp(VariableType.INT)}?

   int_expr
   {$varType = VariableType.INT;}

   |
   {vp(VariableType.INTERFACE)}?

   interface_expr
   {$varType = VariableType.INTERFACE;}

   |
   {vp(VariableType.IP)}?

   ip_expr
   {$varType = VariableType.IP;}

   |
   {vp(VariableType.IPSEC_VPN)}?

   ipsec_vpn_expr
   {$varType = VariableType.IPSEC_VPN;}

   |
   {vp(VariableType.MAP)}?

   map_expr
   |
   {vp(VariableType.NODE)}?

   node_expr
   {$varType = VariableType.NODE;}

   |
   {vp(VariableType.POLICY_MAP)}?

   policy_map_expr
   {$varType = VariableType.POLICY_MAP;}

   |
   {vp(VariableType.POLICY_MAP_CLAUSE)}?

   policy_map_clause_expr
   {$varType = VariableType.POLICY_MAP_CLAUSE;}

   |
   {vp(VariableType.PREFIX)}?

   prefix_expr
   {$varType = VariableType.PREFIX;}

   |
   {vp(VariableType.PREFIX_SPACE)}?

   prefix_space_expr
   {$varType = VariableType.PREFIX_SPACE;}

   |
   {vp(VariableType.ROUTE_FILTER)}?

   route_filter_expr
   {$varType = VariableType.ROUTE_FILTER;}

   |
   {vp(VariableType.ROUTE_FILTER_LINE)}?

   route_filter_line_expr
   {$varType = VariableType.ROUTE_FILTER_LINE;}

   |
   {vp(VariableType.SET_IP)}?

   set_ip_expr
   {$varType = VariableType.SET_IP;}

   |
   {vp(VariableType.SET_PREFIX)}?

   set_prefix_expr
   {$varType = VariableType.SET_PREFIX;}

   |
   {vp(VariableType.SET_STRING)}?

   set_string_expr
   {$varType = VariableType.SET_STRING;}

   |
   {vp(VariableType.STATIC_ROUTE)}?

   static_route_expr
   {$varType = VariableType.STATIC_ROUTE;}

   |
   {vp(VariableType.STRING)}?

   string_expr
   {$varType = VariableType.STRING;}

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
      ingress_node_str = string_literal_string_expr
      | ingress_node_var = VARIABLE
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

foreach_in_set_statement [String scope]
locals [VariableType setVarType]
:
   FOREACH v = VARIABLE COLON set_var = VARIABLE
   {$setVarType = retrieveTypeBinding($set_var.getText()); createTypeBinding($v.getText(), $setVarType.elementType()); _immutableVars.add($v.getText());}

   OPEN_BRACE statement [scope]+ CLOSE_BRACE
;

foreach_scoped_statement [String scope]
locals [VariableType varType, String newScope]
:
   FOREACH
   (
      {$scope.equals("node")}?

      BGP_NEIGHBOR
      {$varType = VariableType.BGP_NEIGHBOR; $newScope = "bgp_neighbor";}

      |
      {$scope.equals("policy")}?

      CLAUSE
      {$varType = VariableType.POLICY_MAP_CLAUSE; $newScope = "clause";}

      |
      {$scope.equals("bgp_neighbor") || $scope.equals("node")}?

      GENERATED_ROUTE
      {$varType = VariableType.GENERATED_ROUTE; $newScope = "generated_route";}

      |
      {$scope.equals("node")}?

      INTERFACE
      {$varType = VariableType.INTERFACE; $newScope = "interface";}

      |
      {$scope.equals("node")}?

      IPSEC_VPN
      {$varType = VariableType.IPSEC_VPN; $newScope = "ipsec_vpn";}

      |
      {$scope.equals("route_filter")}?

      LINE
      {$varType = VariableType.ROUTE_FILTER_LINE; $newScope = "route_filter_line";}

      |
      {$scope.equals("clause")}?

      MATCH_PROTOCOL
      {$varType = null; $newScope = "match_protocol";}

      |
      {$scope.equals("clause")}?

      MATCH_ROUTE_FILTER
      {$varType = null; $newScope = "match_route_filter";}

      |
      {$scope.equals("verify")}?

      NODE
      {$varType = VariableType.NODE; $newScope = "node";}

      |
      {$scope.equals("node")}?

      NODE_BGP_GENERATED_ROUTE
      {$varType = VariableType.GENERATED_ROUTE; $newScope = "generated_route";}

      |
      {$scope.equals("node")}?

      OSPF_OUTBOUND_POLICY
      {$varType = VariableType.POLICY_MAP; $newScope = "policy";}

      |
      {$scope.equals("match_protocol")}?

      PROTOCOL
      {$varType = null; $newScope = "protocol";}

      |
      {$scope.equals("bgp_neighbor")}?

      REMOTE_BGP_NEIGHBOR
      {$varType = VariableType.BGP_NEIGHBOR; $newScope = "remote_bgp_neighbor";}

      |
      {$scope.equals("ipsec_vpn")}?

      REMOTE_IPSEC_VPN
      {$varType = VariableType.IPSEC_VPN; $newScope = "remote_ipsec_vpn";}

      |
      {$scope.equals("match_route_filter")}?

      ROUTE_FILTER
      {$varType = VariableType.ROUTE_FILTER; $newScope = "route_filter";}

      |
      {$scope.equals("node")}?

      STATIC_ROUTE
      {$varType = VariableType.STATIC_ROUTE; $newScope = "static_route";}

   )
   (
      (
         {$varType != null}?

         var = VARIABLE
         {createTypeBinding($var.getText(), $varType); _immutableVars.add($var.getText());}

         OPEN_BRACE statement [scope]+ CLOSE_BRACE
      )
      |
      (
         OPEN_BRACE statement [$newScope]+ CLOSE_BRACE
      )
   )
;

format_string_expr
:
   FORMAT OPEN_PAREN format_string = string_expr
   (
      COMMA
      (
         replacements += printable_expr
      )
   )* CLOSE_PAREN
;

ge_expr
:
   (
      lhs_int = int_expr GE rhs_int = int_expr
   )
   |
   (
      lhs_string = string_expr GE rhs_string = string_expr
   )
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
   (
      lhs_int = int_expr GT rhs_int = int_expr
   )
   |
   (
      lhs_string = string_expr GT rhs_string = string_expr
   )
;

if_expr
:
   IF OPEN_PAREN antecedent = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   consequent = boolean_expr CLOSE_BRACE
;

if_statement [String scope]
:
   IF OPEN_PAREN guard = boolean_expr CLOSE_PAREN THEN? OPEN_BRACE
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

increment_statement
:
   var = VARIABLE
   {assertTypeBinding($var.getText(), VariableType.INT);}

   DOUBLE_PLUS SEMICOLON
;

ingress_path_question
:
   INGRESS_PATH
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

interface_all_prefixes_set_prefix_expr
:
   ALL_PREFIXES
;

interface_boolean_expr
:
   caller = interface_expr PERIOD
   (
      interface_enabled_boolean_expr
      | interface_has_ip_boolean_expr
      | interface_is_loopback_boolean_expr
      | interface_isis_boolean_expr
      | interface_ospf_boolean_expr
   )
;

interface_enabled_boolean_expr
:
   ENABLED
;

interface_expr
:
   INTERFACE
   |
   |
   {v(VariableType.INTERFACE)}?

   var_interface_expr
;

interface_has_ip_boolean_expr
:
   HAS_IP
;

interface_ip_expr
:
   caller = interface_expr PERIOD interface_ip_ip_expr
;

interface_ip_ip_expr
:
   IP
;

interface_is_loopback_boolean_expr
:
   IS_LOOPBACK
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
   caller = interface_expr PERIOD
   (
      interface_prefix_prefix_expr
      | interface_subnet_prefix_expr
   )
;

interface_prefix_prefix_expr
:
   PREFIX
;

interface_set_prefix_expr
:
   caller = interface_expr PERIOD
   (
      interface_all_prefixes_set_prefix_expr
   )
;

interface_string_expr
:
   caller = interface_expr PERIOD interface_name_string_expr
;

interface_subnet_prefix_expr
:
   SUBNET
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
   | ipsec_vpn_ip_expr
   | prefix_ip_expr
   | static_route_ip_expr
   | IP_ADDRESS
   |
   {v(VariableType.IP)}?

   var_ip_expr
;

ipsec_vpn_boolean_expr
:
   ipsec_vpn_expr PERIOD
   (
      ipsec_vpn_compatible_ike_proposals_boolean_expr
      | ipsec_vpn_compatible_ipsec_proposals_boolean_expr
      | ipsec_vpn_has_remote_ip_boolean_expr
      | ipsec_vpn_has_remote_ipsec_vpn_boolean_expr
      | ipsec_vpn_has_single_remote_ipsec_vpn_boolean_expr
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
   | REMOTE_IPSEC_VPN
   | ipsec_vpn_expr PERIOD ipsec_vpn_ipsec_vpn_expr
   |
   {v(VariableType.IPSEC_VPN)}?

   var_ipsec_vpn_expr
;

ipsec_vpn_has_remote_ip_boolean_expr
:
   HAS_REMOTE_IP
;

ipsec_vpn_has_remote_ipsec_vpn_boolean_expr
:
   HAS_REMOTE_IPSEC_VPN
;

ipsec_vpn_has_single_remote_ipsec_vpn_boolean_expr
:
   HAS_SINGLE_REMOTE_IPSEC_VPN
;

ipsec_vpn_ike_gateway_name_string_expr
:
   IKE_GATEWAY_NAME
;

ipsec_vpn_ike_policy_name_string_expr
:
   IKE_POLICY_NAME
;

ipsec_vpn_ip_expr
:
   caller = ipsec_vpn_expr PERIOD ipsec_vpn_remote_ip_ip_expr
;

ipsec_vpn_ipsec_policy_name_string_expr
:
   IPSEC_POLICY_NAME
;

ipsec_vpn_ipsec_vpn_expr
:
   (
      ipsec_vpn_remote_ipsec_vpn_ipsec_vpn_expr
   )
;

ipsec_vpn_name_string_expr
:
   NAME
;

ipsec_vpn_node_expr
:
   caller = ipsec_vpn_expr PERIOD
   (
      ipsec_vpn_owner_node_expr
   )
;

ipsec_vpn_owner_node_expr
:
   OWNER
;

ipsec_vpn_pre_shared_key_hash_string_expr
:
   PRE_SHARED_KEY_HASH
;

ipsec_vpn_remote_ip_ip_expr
:
   REMOTE_IP
;

ipsec_vpn_remote_ipsec_vpn_ipsec_vpn_expr
:
   REMOTE_IPSEC_VPN
;

ipsec_vpn_string_expr
:
   caller = ipsec_vpn_expr PERIOD
   (
      ipsec_vpn_ike_gateway_name_string_expr
      | ipsec_vpn_ike_policy_name_string_expr
      | ipsec_vpn_ipsec_policy_name_string_expr
      | ipsec_vpn_name_string_expr
      | ipsec_vpn_pre_shared_key_hash_string_expr
   )
;

le_expr
:
   (
      lhs_int = int_expr LE rhs_int = int_expr
   )
   |
   (
      lhs_string = string_expr LE rhs_string = string_expr
   )
;

line_boolean_expr
:
   caller = route_filter_line_expr PERIOD
   (
      line_permit_boolean_expr
   )
;

line_permit_boolean_expr
:
   PERMIT
;

literal_int_expr
:
   DEC
;

local_path_question
:
   LOCAL_PATH
;

lt_expr
:
   (
      lhs_int = int_expr LT rhs_int = int_expr
   )
   |
   (
      lhs_string = string_expr LT rhs_string = string_expr
   )
;

map_expr
:
   QUERY
   | new_map_expr
   | caller = map_expr PERIOD map_map_expr
   |
   {v(VariableType.MAP)}?

   var_map_expr
;

map_get_map_map_expr
:
   GET_MAP OPEN_PAREN key = printable_expr CLOSE_PAREN
;

map_get_string_expr
:
   GET OPEN_PAREN key = printable_expr CLOSE_PAREN
;

map_keys_set_string_expr
:
   KEYS
;

map_map_expr
:
   (
      map_get_map_map_expr
   )
;

map_set_method
:
   caller = map_expr PERIOD SET OPEN_PAREN key = printable_expr COMMA value =
   printable_expr CLOSE_PAREN SEMICOLON
;

map_set_string_expr
:
   caller = map_expr PERIOD
   (
      map_keys_set_string_expr
   )
;

map_string_expr
:
   caller = map_expr PERIOD
   (
      map_get_string_expr
   )
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
locals [VariableType varType]
:
   lhs = expr
   {$varType = $lhs.varType;}

   NOT_EQUALS rhs = expr
   {$varType == $rhs.varType}?

;

new_map_expr
:
   NEW_MAP
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

node_bgp_origination_space_explicit_prefix_space_expr
:
   BGP_ORIGINATION_SPACE_EXPLICIT
;

node_boolean_expr
:
   node_expr PERIOD
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
   | string_literal_string_expr
   | VARIABLE
;

node_expr
:
   NODE
   | bgp_neighbor_node_expr
   | ipsec_vpn_node_expr
   |
   {v(VariableType.NODE)}?

   var_node_expr
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

node_prefix_space_expr
:
   caller = node_expr PERIOD
   (
      node_bgp_origination_space_explicit_prefix_space_expr
   )
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
   caller = node_expr PERIOD node_name_string_expr
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

policy_map_clause_boolean_expr
:
   caller = policy_map_clause_expr PERIOD
   (
      policy_map_clause_permit_boolean_expr
   )
;

policy_map_clause_expr
:
   CLAUSE
   |
   {v(VariableType.POLICY_MAP_CLAUSE)}?

   var_policy_map_clause_expr
;

policy_map_clause_permit_boolean_expr
:
   PERMIT
;

policy_map_expr
:
   {v(VariableType.POLICY_MAP)}?

   var_policy_map_expr
;

prefix_address_ip_expr
:
   ADDRESS
;

prefix_expr
:
   generated_route_prefix_expr
   | interface_prefix_expr
   | static_route_prefix_expr
   {v(VariableType.PREFIX)}?

   | var_prefix_expr
;

prefix_ip_expr
:
   caller = prefix_expr PERIOD prefix_address_ip_expr
;

prefix_space_boolean_expr
:
   caller = prefix_space_expr PERIOD
   (
      prefix_space_overlaps_boolean_expr
   )
;

prefix_space_expr
:
   node_prefix_space_expr
   | caller = prefix_space_expr PERIOD prefix_space_prefix_space_expr
   |
   {v(VariableType.PREFIX_SPACE)}?

   var_prefix_space_expr
;

prefix_space_intersection_prefix_space_expr
:
   INTERSECTION OPEN_PAREN arg = prefix_space_expr CLOSE_PAREN
;

prefix_space_overlaps_boolean_expr
:
   OVERLAPS OPEN_PAREN arg = prefix_space_expr CLOSE_PAREN
;

prefix_space_prefix_space_expr
:
   (
      prefix_space_intersection_prefix_space_expr
   )
;

printable_expr
:
   {vp(VariableType.BOOLEAN)}?

   boolean_expr
   | expr
;

printf_statement
:
   PRINTF OPEN_PAREN format_string = string_expr
   (
      COMMA
      (
         replacements += printable_expr
      )
   )* CLOSE_PAREN SEMICOLON
;

property_boolean_expr
:
   bgp_neighbor_boolean_expr
   | interface_boolean_expr
   | ipsec_vpn_boolean_expr
   | line_boolean_expr
   | node_boolean_expr
   | policy_map_clause_boolean_expr
   | prefix_space_boolean_expr
   | static_route_boolean_expr
;

protocol_dependencies_question
:
   PROTOCOL_DEPENDENCIES
;

protocol_expr
:
   PROTOCOL
   |
   {v(VariableType.PROTOCOL)}?

   var_protocol_expr
;

protocol_name_string_expr
:
   NAME
;

protocol_string_expr
:
   caller = protocol_expr PERIOD protocol_name_string_expr
;

question
:
   defaults?
   (
      acl_reachability_question
      | reduced_reachability_question
      | ingress_path_question
      | local_path_question
      | multipath_question
      | protocol_dependencies_question
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

reduced_reachability_question
:
   REDUCED_REACHABILITY
;

route_filter_expr
:
   route_filter_route_filter_expr
   |
   {v(VariableType.ROUTE_FILTER)}?

   var_route_filter_expr
;

route_filter_line_expr
:
   route_filter_line_line_expr
   |
   {v(VariableType.ROUTE_FILTER)}?

   var_route_filter_line_expr
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
   caller = route_filter_expr PERIOD route_filter_name_string_expr
;

set_add_method [VariableType type, String caller]
:
   ADD OPEN_PAREN
   (
      {$type == VariableType.SET_BGP_NEIGHBOR}?

      bgp_neighbor_expr
      |
      {$type == VariableType.SET_INTERFACE}?

      interface_expr
      |
      {$type == VariableType.SET_IP}?

      ip_expr
      |
      {$type == VariableType.SET_IPSEC_VPN}?

      ipsec_vpn_expr
      |
      {$type == VariableType.SET_NODE}?

      node_expr
      |
      {$type == VariableType.SET_POLICY_MAP}?

      policy_map_expr
      |
      {$type == VariableType.SET_POLICY_MAP_CLAUSE}?

      policy_map_clause_expr
      |
      {$type == VariableType.SET_PREFIX}?

      prefix_expr
      |
      {$type == VariableType.SET_PREFIX_SPACE}?

      prefix_space_expr
      |
      {$type == VariableType.SET_ROUTE_FILTER_LINE}?

      route_filter_expr
      |
      {$type == VariableType.SET_ROUTE_FILTER}?

      route_filter_line_expr
      |
      {$type == VariableType.SET_STATIC_ROUTE}?

      static_route_expr
      |
      {$type == VariableType.SET_STRING}?

      string_expr
   ) CLOSE_PAREN
;

set_contains_expr
locals [VariableType type]
:
   caller = VARIABLE
   { $type = _typeBindings.get($caller.getText()); }

   PERIOD CONTAINS OPEN_PAREN
   (
      {$type == VariableType.SET_BGP_NEIGHBOR}?

      bgp_neighbor_expr
      |
      {$type == VariableType.SET_INTERFACE}?

      interface_expr
      |
      {$type == VariableType.SET_IP}?

      ip_expr
      |
      {$type == VariableType.SET_IPSEC_VPN}?

      ipsec_vpn_expr
      |
      {$type == VariableType.SET_NODE}?

      node_expr
      |
      {$type == VariableType.SET_POLICY_MAP}?

      policy_map_expr
      |
      {$type == VariableType.SET_POLICY_MAP_CLAUSE}?

      policy_map_clause_expr
      |
      {$type == VariableType.SET_PREFIX}?

      prefix_expr
      |
      {$type == VariableType.SET_PREFIX_SPACE}?

      prefix_space_expr
      |
      {$type == VariableType.SET_ROUTE_FILTER_LINE}?

      route_filter_expr
      |
      {$type == VariableType.SET_ROUTE_FILTER}?

      route_filter_line_expr
      |
      {$type == VariableType.SET_STATIC_ROUTE}?

      static_route_expr
      |
      {$type == VariableType.SET_STRING}?

      string_expr
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
         settype = BGP_NEIGHBOR
         | settype = INTERFACE
         | settype = IP
         | settype = IPSEC_VPN
         | settype = NODE
         | settype = POLICY_MAP
         | settype = POLICY_MAP_CLAUSE
         | settype = PREFIX
         | settype = PREFIX_SPACE
         | settype = ROUTE_FILTER
         | settype = ROUTE_FILTER_LINE
         | settype = STATIC_ROUTE
         | settype = STRING
      )
      {
         $typeStr += $settype.getText() + ">";
         $type = VariableType.fromString($typeStr);
         createTypeBinding($var.getText(), $type);
      }

   )
   {
      $oldType == null || $type == $oldType 
      /* Same variable declared with two different types*/
   }?

   SEMICOLON
;

set_ip_expr
:
   {v(VariableType.SET_IP)}?

   | var_set_ip_expr
;

set_prefix_expr
:
   interface_set_prefix_expr
   |
   {v(VariableType.SET_PREFIX)}?

   | var_set_prefix_expr
;

set_size_int_expr
locals [VariableType type]
:
   caller = VARIABLE
   {$type = _typeBindings.get($caller.getText());}

   PERIOD SIZE
;

set_string_expr
:
   map_set_string_expr
;

statement [String scope]
:
   assertion [scope]
   | assignment
   | foreach_in_set_statement [scope]
   | foreach_scoped_statement [scope]
   | if_statement [scope]
   | increment_statement
   | map_set_method
   | method [scope]
   | printf_statement
   | set_declaration_statement
   | unless_statement [scope]
;

static_route_administrative_cost_int_expr
:
   ADMINISTRATIVE_COST
;

static_route_boolean_expr
:
   caller = static_route_expr PERIOD
   (
      static_route_has_next_hop_interface_boolean_expr
      | static_route_has_next_hop_ip_boolean_expr
   )
;

static_route_expr
:
   STATIC_ROUTE
   |
   {v(VariableType.STATIC_ROUTE)}?

   var_static_route_expr
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
   caller = static_route_expr PERIOD static_route_administrative_cost_int_expr
;

static_route_ip_expr
:
   caller = static_route_expr PERIOD static_route_next_hop_ip_ip_expr
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
   caller = static_route_expr PERIOD static_route_prefix_prefix_expr
;

static_route_prefix_prefix_expr
:
   PREFIX
;

static_route_string_expr
:
   caller = static_route_expr PERIOD
   static_route_next_hop_interface_string_expr
;

base_string_expr
:
   bgp_neighbor_string_expr
   | format_string_expr
   | interface_string_expr
   | ipsec_vpn_string_expr
   | map_string_expr
   | node_string_expr
   | protocol_string_expr
   | route_filter_string_expr
   | static_route_string_expr
   |
   {v(VariableType.STRING)}?

   var_string_expr
;

string_expr
:
   base_string_expr
   | string_literal_string_expr
   | s1 = string_expr PLUS s2 = string_expr
;

string_literal_string_expr returns [String text] @init {
   $text = "";
}
:
   DOUBLE_QUOTE
   (
      sl = STRING_LITERAL
      {$text = $sl.getText();}

   )? DOUBLE_QUOTE
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
   {$type = retrieveTypeBinding($caller);}

   (
      set_add_method [$type, caller]
      | set_clear_method [$type, caller]
   )
;

unless_statement [String scope]
:
   UNLESS OPEN_PAREN guard = boolean_expr CLOSE_PAREN OPEN_BRACE
   (
      statements += statement [scope]
   )* CLOSE_BRACE
;

val_int_expr
:
   bgp_neighbor_int_expr
   | literal_int_expr
   | set_size_int_expr
   | static_route_int_expr
   |
   {v(VariableType.INT)}?

   var_int_expr
;

var_bgp_neighbor_expr
:
   var = VARIABLE
;

var_boolean_expr
:
   var = VARIABLE
;

var_int_expr
:
   VARIABLE
;

var_interface_expr
:
   VARIABLE
;

var_ip_expr
:
   VARIABLE
;

var_ipsec_vpn_expr
:
   var = VARIABLE
;

var_list_expr
:
   VARIABLE
;

var_map_expr
:
   VARIABLE
;

var_node_expr
:
   VARIABLE
;

var_policy_map_clause_expr
:
   VARIABLE
;

var_policy_map_expr
:
   VARIABLE
;

var_prefix_expr
:
   VARIABLE
;

var_prefix_space_expr
:
   VARIABLE
;

var_protocol_expr
:
   VARIABLE
;

var_route_filter_expr
:
   VARIABLE
;

var_route_filter_line_expr
:
   VARIABLE
;

var_set_ip_expr
:
   VARIABLE
;

var_set_prefix_expr
:
   VARIABLE
;

var_static_route_expr
:
   VARIABLE
;

var_string_expr
:
   VARIABLE
;

verify_question
:
   VERIFY OPEN_BRACE statement ["verify"]+ CLOSE_BRACE
;
