package org.batfish.grammar.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.BatfishExtractor;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.question.QuestionParser.*;
import org.batfish.common.BatfishException;
import org.batfish.question.AclReachabilityQuestion;
import org.batfish.question.CompareSameNameQuestion;
import org.batfish.question.Expr;
import org.batfish.question.ReducedReachabilityQuestion;
import org.batfish.question.ForwardingAction;
import org.batfish.question.GeneratedRoutePrefixExpr;
import org.batfish.question.IngressPathQuestion;
import org.batfish.question.LocalPathQuestion;
import org.batfish.question.MultipathQuestion;
import org.batfish.question.NeighborType;
import org.batfish.question.NeighborsQuestion;
import org.batfish.question.NodeType;
import org.batfish.question.NodesQuestion;
import org.batfish.question.ProtocolDependenciesQuestion;
import org.batfish.question.Question;
import org.batfish.question.QuestionParameters;
import org.batfish.question.ReachabilityQuestion;
import org.batfish.question.TracerouteQuestion;
import org.batfish.question.VerifyProgram;
import org.batfish.question.VerifyQuestion;
import org.batfish.question.bgp_advertisement_expr.BaseCaseBgpAdvertisementExpr;
import org.batfish.question.bgp_advertisement_expr.BgpAdvertisementExpr;
import org.batfish.question.bgp_neighbor_expr.BaseCaseBgpNeighborExpr;
import org.batfish.question.bgp_neighbor_expr.BgpNeighborExpr;
import org.batfish.question.bgp_neighbor_expr.VarBgpNeighborExpr;
import org.batfish.question.boolean_expr.AndExpr;
import org.batfish.question.boolean_expr.BaseCaseBooleanExpr;
import org.batfish.question.boolean_expr.BooleanExpr;
import org.batfish.question.boolean_expr.EqExpr;
import org.batfish.question.boolean_expr.NeqExpr;
import org.batfish.question.boolean_expr.SetContainsExpr;
import org.batfish.question.boolean_expr.IfExpr;
import org.batfish.question.boolean_expr.NotExpr;
import org.batfish.question.boolean_expr.OrExpr;
import org.batfish.question.boolean_expr.VarBooleanExpr;
import org.batfish.question.boolean_expr.bgp_neighbor.EbgpMultihopBgpNeighborBooleanExpr;
import org.batfish.question.boolean_expr.bgp_neighbor.HasGeneratedRouteBgpNeighborBooleanExpr;
import org.batfish.question.boolean_expr.bgp_neighbor.HasLocalIpBgpNeighborBooleanExpr;
import org.batfish.question.boolean_expr.bgp_neighbor.HasRemoteBgpNeighborBgpNeighborBooleanExpr;
import org.batfish.question.boolean_expr.bgp_neighbor.HasSingleRemoteBgpNeighborBgpNeighborBooleanExpr;
import org.batfish.question.boolean_expr.iface.EnabledInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.HasIpInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.IsLoopbackInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.IsisL1ActiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.IsisL1PassiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.IsisL2ActiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.IsisL2PassiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.OspfActiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.iface.OspfPassiveInterfaceBooleanExpr;
import org.batfish.question.boolean_expr.integer.IntGeExpr;
import org.batfish.question.boolean_expr.integer.IntGtExpr;
import org.batfish.question.boolean_expr.integer.IntLeExpr;
import org.batfish.question.boolean_expr.integer.IntLtExpr;
import org.batfish.question.boolean_expr.ipsec_vpn.CompatibleIkeProposalsIpsecVpnBooleanExpr;
import org.batfish.question.boolean_expr.ipsec_vpn.CompatibleIpsecProposalsIpsecVpnBooleanExpr;
import org.batfish.question.boolean_expr.ipsec_vpn.HasRemoteIpIpsecVpnBooleanExpr;
import org.batfish.question.boolean_expr.ipsec_vpn.HasRemoteIpsecVpnIpsecVpnBooleanExpr;
import org.batfish.question.boolean_expr.ipsec_vpn.HasSingleRemoteIpsecVpnIpsecVpnBooleanExpr;
import org.batfish.question.boolean_expr.node.BgpConfiguredNodeBooleanExpr;
import org.batfish.question.boolean_expr.node.BgpHasGeneratedRouteNodeBooleanExpr;
import org.batfish.question.boolean_expr.node.HasGeneratedRouteNodeBooleanExpr;
import org.batfish.question.boolean_expr.node.IsisConfiguredNodeBooleanExpr;
import org.batfish.question.boolean_expr.node.OspfConfiguredNodeBooleanExpr;
import org.batfish.question.boolean_expr.node.StaticConfiguredNodeBooleanExpr;
import org.batfish.question.boolean_expr.policy_map_clause.PermitPolicyMapClauseBooleanExpr;
import org.batfish.question.boolean_expr.prefix_space.OverlapsPrefixSpaceBooleanExpr;
import org.batfish.question.boolean_expr.route_filter_line.PermitLineBooleanExpr;
import org.batfish.question.boolean_expr.static_route.HasNextHopInterfaceStaticRouteBooleanExpr;
import org.batfish.question.boolean_expr.static_route.HasNextHopIpStaticRouteBooleanExpr;
import org.batfish.question.boolean_expr.string.StringGeExpr;
import org.batfish.question.boolean_expr.string.StringGtExpr;
import org.batfish.question.boolean_expr.string.StringLeExpr;
import org.batfish.question.boolean_expr.string.StringLtExpr;
import org.batfish.question.int_expr.DifferenceIntExpr;
import org.batfish.question.int_expr.IntExpr;
import org.batfish.question.int_expr.LiteralIntExpr;
import org.batfish.question.int_expr.ProductIntExpr;
import org.batfish.question.int_expr.QuotientIntExpr;
import org.batfish.question.int_expr.SetSizeIntExpr;
import org.batfish.question.int_expr.SumIntExpr;
import org.batfish.question.int_expr.VarIntExpr;
import org.batfish.question.int_expr.bgp_advertisement.LocalPreferenceBgpAdvertisementIntExpr;
import org.batfish.question.int_expr.bgp_advertisement.MedBgpAdvertisementIntExpr;
import org.batfish.question.int_expr.bgp_neighbor.LocalAsBgpNeighborIntExpr;
import org.batfish.question.int_expr.bgp_neighbor.RemoteAsBgpNeighborIntExpr;
import org.batfish.question.int_expr.route.AdministrativeCostRouteIntExpr;
import org.batfish.question.int_expr.route.CostRouteIntExpr;
import org.batfish.question.int_expr.route.TagRouteIntExpr;
import org.batfish.question.int_expr.static_route.AdministrativeCostStaticRouteIntExpr;
import org.batfish.question.interface_expr.BaseCaseInterfaceExpr;
import org.batfish.question.interface_expr.InterfaceExpr;
import org.batfish.question.interface_expr.VarInterfaceExpr;
import org.batfish.question.ip_expr.IpExpr;
import org.batfish.question.ip_expr.LiteralIpExpr;
import org.batfish.question.ip_expr.VarIpExpr;
import org.batfish.question.ip_expr.bgp_advertisement.DstIpBgpAdvertisementIpExpr;
import org.batfish.question.ip_expr.bgp_advertisement.NextHopIpBgpAdvertisementIpExpr;
import org.batfish.question.ip_expr.bgp_advertisement.SrcIpBgpAdvertisementIpExpr;
import org.batfish.question.ip_expr.bgp_neighbor.LocalIpBgpNeighborIpExpr;
import org.batfish.question.ip_expr.bgp_neighbor.RemoteIpBgpNeighborIpExpr;
import org.batfish.question.ip_expr.iface.IpInterfaceIpExpr;
import org.batfish.question.ip_expr.ipsec_vpn.RemoteIpIpsecVpnIpExpr;
import org.batfish.question.ip_expr.prefix.AddressPrefixIpExpr;
import org.batfish.question.ip_expr.route.NextHopIpRouteIpExpr;
import org.batfish.question.ip_expr.static_route.NextHopIpStaticRouteIpExpr;
import org.batfish.question.ip_set_expr.IpSetExpr;
import org.batfish.question.ip_set_expr.VarIpSetExpr;
import org.batfish.question.ipsec_vpn_expr.BaseCaseIpsecVpnExpr;
import org.batfish.question.ipsec_vpn_expr.IpsecVpnExpr;
import org.batfish.question.ipsec_vpn_expr.ipsec_vpn.RemoteIpsecVpnIpsecVpnExpr;
import org.batfish.question.map_expr.BaseCaseMapExpr;
import org.batfish.question.map_expr.MapExpr;
import org.batfish.question.map_expr.NewMapExpr;
import org.batfish.question.map_expr.VarMapExpr;
import org.batfish.question.map_expr.map.GetMapMapMapExpr;
import org.batfish.question.node_expr.BaseCaseNodeExpr;
import org.batfish.question.node_expr.NodeExpr;
import org.batfish.question.node_expr.VarNodeExpr;
import org.batfish.question.node_expr.bgp_neighbor.OwnerBgpNeighborNodeExpr;
import org.batfish.question.node_expr.ipsec_vpn.OwnerIpsecVpnNodeExpr;
import org.batfish.question.policy_map_clause_expr.BaseCasePolicyMapClauseExpr;
import org.batfish.question.policy_map_clause_expr.PolicyMapClauseExpr;
import org.batfish.question.policy_map_clause_expr.VarPolicyMapClauseExpr;
import org.batfish.question.policy_map_expr.PolicyMapExpr;
import org.batfish.question.policy_map_expr.VarPolicyMapExpr;
import org.batfish.question.prefix_expr.PrefixExpr;
import org.batfish.question.prefix_expr.VarPrefixExpr;
import org.batfish.question.prefix_expr.bgp_advertisement.NetworkBgpAdvertisementPrefixExpr;
import org.batfish.question.prefix_expr.iface.PrefixInterfacePrefixExpr;
import org.batfish.question.prefix_expr.iface.SubnetInterfacePrefixExpr;
import org.batfish.question.prefix_expr.route.NetworkRoutePrefixExpr;
import org.batfish.question.prefix_expr.static_route.PrefixStaticRoutePrefixExpr;
import org.batfish.question.prefix_set_expr.PrefixSetExpr;
import org.batfish.question.prefix_set_expr.VarPrefixSetExpr;
import org.batfish.question.prefix_set_expr.iface.AllPrefixesInterfacePrefixSetExpr;
import org.batfish.question.prefix_space_expr.PrefixSpaceExpr;
import org.batfish.question.prefix_space_expr.VarPrefixSpaceExpr;
import org.batfish.question.prefix_space_expr.node.BgpOriginationSpaceExplicitNodePrefixSpaceExpr;
import org.batfish.question.prefix_space_expr.prefix_space.IntersectionPrefixSpacePrefixSpaceExpr;
import org.batfish.question.route_expr.BaseCaseRouteExpr;
import org.batfish.question.route_expr.RouteExpr;
import org.batfish.question.route_expr.VarRouteExpr;
import org.batfish.question.route_filter_expr.BaseCaseRouteFilterExpr;
import org.batfish.question.route_filter_expr.RouteFilterExpr;
import org.batfish.question.route_filter_line_expr.BaseCaseRouterFilterLineExpr;
import org.batfish.question.route_filter_line_expr.RouteFilterLineExpr;
import org.batfish.question.statement.Assertion;
import org.batfish.question.statement.ForEachBgpAdvertisementStatement;
import org.batfish.question.statement.ForEachBgpNeighborStatement;
import org.batfish.question.statement.ForEachClauseStatement;
import org.batfish.question.statement.ForEachGeneratedRouteStatement;
import org.batfish.question.statement.ForEachIntegerStatement;
import org.batfish.question.statement.ForEachInterfaceStatement;
import org.batfish.question.statement.ForEachIpStatement;
import org.batfish.question.statement.ForEachIpsecVpnStatement;
import org.batfish.question.statement.ForEachLineStatement;
import org.batfish.question.statement.ForEachMapStatement;
import org.batfish.question.statement.ForEachMatchProtocolStatement;
import org.batfish.question.statement.ForEachMatchRouteFilterStatement;
import org.batfish.question.statement.ForEachNodeBgpGeneratedRouteStatement;
import org.batfish.question.statement.ForEachNodeStatement;
import org.batfish.question.statement.ForEachOspfOutboundPolicyStatement;
import org.batfish.question.statement.ForEachPolicyMapClauseStatement;
import org.batfish.question.statement.ForEachPolicyMapStatement;
import org.batfish.question.statement.ForEachPrefixSpaceStatement;
import org.batfish.question.statement.ForEachPrefixStatement;
import org.batfish.question.statement.ForEachProtocolStatement;
import org.batfish.question.statement.ForEachReceivedEbgpAdvertisementStatement;
import org.batfish.question.statement.ForEachReceivedIbgpAdvertisementStatement;
import org.batfish.question.statement.ForEachRemoteBgpNeighborStatement;
import org.batfish.question.statement.ForEachRemoteIpsecVpnStatement;
import org.batfish.question.statement.ForEachRouteFilterLineStatement;
import org.batfish.question.statement.ForEachRouteFilterStatement;
import org.batfish.question.statement.ForEachRouteStatement;
import org.batfish.question.statement.ForEachSentEbgpAdvertisementStatement;
import org.batfish.question.statement.ForEachSentIbgpAdvertisementStatement;
import org.batfish.question.statement.ForEachStatement;
import org.batfish.question.statement.ForEachStaticRouteStatement;
import org.batfish.question.statement.ForEachStringStatement;
import org.batfish.question.statement.IfStatement;
import org.batfish.question.statement.Assignment;
import org.batfish.question.statement.IncrementStatement;
import org.batfish.question.statement.MapSetMethod;
import org.batfish.question.statement.PrintfStatement;
import org.batfish.question.statement.SetAddStatement;
import org.batfish.question.statement.SetClearStatement;
import org.batfish.question.statement.Statement;
import org.batfish.question.statement.SetDeclarationStatement;
import org.batfish.question.statement.UnlessStatement;
import org.batfish.question.static_route_expr.BaseCaseStaticRouteExpr;
import org.batfish.question.static_route_expr.StaticRouteExpr;
import org.batfish.question.static_route_expr.VarStaticRouteExpr;
import org.batfish.question.string_expr.FormatStringExpr;
import org.batfish.question.string_expr.StringConcatenateExpr;
import org.batfish.question.string_expr.StringExpr;
import org.batfish.question.string_expr.StringLiteralStringExpr;
import org.batfish.question.string_expr.VarStringExpr;
import org.batfish.question.string_expr.bgp_advertisement.AsPathBgpAdvertisementStringExpr;
import org.batfish.question.string_expr.bgp_advertisement.DstNodeBgpAdvertisementStringExpr;
import org.batfish.question.string_expr.bgp_advertisement.SrcNodeBgpAdvertisementStringExpr;
import org.batfish.question.string_expr.bgp_neighbor.BgpNeighborStringExpr;
import org.batfish.question.string_expr.bgp_neighbor.DescriptionBgpNeighborStringExpr;
import org.batfish.question.string_expr.bgp_neighbor.GroupBgpNeighborStringExpr;
import org.batfish.question.string_expr.bgp_neighbor.NameBgpNeighborStringExpr;
import org.batfish.question.string_expr.iface.DescriptionInterfaceStringExpr;
import org.batfish.question.string_expr.iface.NameInterfaceStringExpr;
import org.batfish.question.string_expr.ipsec_vpn.IkeGatewayNameIpsecVpnStringExpr;
import org.batfish.question.string_expr.ipsec_vpn.IkePolicyNameIpsecVpnStringExpr;
import org.batfish.question.string_expr.ipsec_vpn.IpsecPolicyNameIpsecVpnStringExpr;
import org.batfish.question.string_expr.ipsec_vpn.NameIpsecVpnStringExpr;
import org.batfish.question.string_expr.ipsec_vpn.PreSharedKeyHashIpsecVpnStringExpr;
import org.batfish.question.string_expr.map.GetMapStringExpr;
import org.batfish.question.string_expr.node.NameNodeStringExpr;
import org.batfish.question.string_expr.protocol.ProtocolStringExpr;
import org.batfish.question.string_expr.route.NextHopInterfaceRouteStringExpr;
import org.batfish.question.string_expr.route.NextHopRouteStringExpr;
import org.batfish.question.string_expr.route.ProtocolRouteStringExpr;
import org.batfish.question.string_expr.route_filter.RouteFilterStringExpr;
import org.batfish.question.string_expr.static_route.StaticRouteStringExpr;
import org.batfish.question.string_set_expr.StringSetExpr;
import org.batfish.question.string_set_expr.bgp_advertisement.CommunitiesBgpAdvertisementStringSetExpr;
import org.batfish.question.string_set_expr.map.KeysMapStringSetExpr;
import org.batfish.representation.FlowBuilder;
import org.batfish.representation.Ip;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;
import org.batfish.util.Util;

public class QuestionExtractor extends QuestionParserBaseListener implements
      BatfishExtractor {

   private static final String ERR_CONVERT_ACTION = "Cannot convert parse tree node to action";

   private static final String ERR_CONVERT_BGP_ADVERTISEMENT = "Cannot convert parse tree node to bgp_advertisement expression";

   private static final String ERR_CONVERT_BGP_NEIGHBOR = "Cannot convert parse tree node to bgp_neighbor expression";

   private static final String ERR_CONVERT_BOOLEAN = "Cannot convert parse tree node to boolean expression";

   private static final String ERR_CONVERT_INT = "Cannot convert parse tree node to integer expression";

   private static final String ERR_CONVERT_INTERFACE = "Cannot convert parse tree node to interface expression";

   private static final String ERR_CONVERT_IP = "Cannot convert parse tree node to IP expression";

   private static final String ERR_CONVERT_IP_SET = "Cannot convert parse tree node to ip-set expression";

   private static final String ERR_CONVERT_IPSEC_VPN = "Cannot convert parse tree node to ipsec_vpn expression";

   private static final String ERR_CONVERT_MAP_EXPR = "Cannot convert parse tree node to map expression";

   private static final String ERR_CONVERT_NODE = "Cannot convert parse tree node to node expression";

   private static final String ERR_CONVERT_POLICY_MAP = "Cannot convert parse tree node to policy map expression";

   private static final String ERR_CONVERT_POLICY_MAP_CLAUSE = "Cannot convert parse tree node to policy map clause expression";

   private static final String ERR_CONVERT_PREFIX = "Cannot convert parse tree node to prefix expression";

   private static final String ERR_CONVERT_PREFIX_SET = "Cannot convert parse tree node to prefix-set expression";

   private static final String ERR_CONVERT_PREFIX_SPACE = "Cannot convert parse tree node to prefix_space expression";

   private static final String ERR_CONVERT_PRINTABLE = "Cannot convert parse tree node to printable expression";

   private static final String ERR_CONVERT_ROUTE = "Cannot convert parse tree node to route expression";

   private static final String ERR_CONVERT_ROUTE_FILTER = "Cannot convert parse tree node to route_filter expression";

   private static final String ERR_CONVERT_ROUTE_FILTER_LINE = "Cannot convert parse tree node to route_filter_line expression";

   private static final String ERR_CONVERT_STATEMENT = "Cannot convert parse tree node to statement";

   private static final String ERR_CONVERT_STATIC_ROUTE = "Cannot convert parse tree node to static_route expression";

   private static final String ERR_CONVERT_STRING = "Cannot convert parse tree node to string expression";

   private static final String ERR_CONVERT_STRING_SET = "Cannot convert parse tree node to string-set expression";;

   private FlowBuilder _currentFlowBuilder;

   private String _flowTag;

   private QuestionParameters _parameters;

   private QuestionCombinedParser _parser;

   private Question _question;

   private NeighborsQuestion _neighborsQuestion;
   
   private NodesQuestion _nodesQuestion;

   private ReachabilityQuestion _reachabilityQuestion;

   private Set<FlowBuilder> _tracerouteFlowBuilders;

   private VerifyProgram _verifyProgram;

   public QuestionExtractor(QuestionCombinedParser parser, String flowTag,
         QuestionParameters parameters) {
      _parser = parser;
      _flowTag = flowTag;
      _parameters = parameters;
   }

   private void bgp_advertisements() {
      _verifyProgram.setDataPlane(true);
      _verifyProgram.setDataPlaneBgpAdvertisements(true);
   }

   private BatfishException conversionError(String error, ParserRuleContext ctx) {
      String parseTree = ParseTreePrettyPrinter.print(ctx, _parser);
      return new BatfishException(error + ": \n" + parseTree);
   }

   @Override
   public void enterCompare_same_name_question(
         Compare_same_name_questionContext ctx) {
      _question = new CompareSameNameQuestion(_parameters);
   }

   @Override
   public void enterDefault_binding(Default_bindingContext ctx) {
      String var = ctx.VARIABLE().getText().substring(1);
      VariableType type;
      Object value;
      if (ctx.action() != null) {
         type = VariableType.ACTION;
         value = ForwardingAction.fromString(ctx.action().getText());
      }
      else if (ctx.boolean_literal() != null) {
         type = VariableType.BOOLEAN;
         value = Boolean.parseBoolean(ctx.boolean_literal().getText());
      }
      else if (ctx.integer_literal() != null) {
         type = VariableType.INT;
         value = Long.parseLong(ctx.integer_literal().getText());
      }
      else if (ctx.IP_ADDRESS() != null) {
         type = VariableType.IP;
         value = new Ip(ctx.IP_ADDRESS().getText());
      }
      else if (ctx.ip_constraint_complex() != null) {
         type = VariableType.SET_PREFIX;
         value = toPrefixSet(ctx.ip_constraint_complex());
      }
      else if (ctx.IP_PREFIX() != null) {
         type = VariableType.PREFIX;
         value = new Prefix(ctx.IP_PREFIX().getText());
      }
      else if (ctx.neighbor_type() != null) {
    	  type = VariableType.NEIGHBOR_TYPE;
    	  value = NeighborType.fromName(ctx.neighbor_type().getText());
      }
      else if (ctx.node_type() != null) {
    	  type = VariableType.NODE_TYPE;
    	  value = NodeType.fromName(ctx.node_type().getText());
      }
      else if (ctx.range() != null) {
         type = VariableType.RANGE;
         value = toRange(ctx.range());
      }
      else if (ctx.REGEX() != null) {
         type = VariableType.REGEX;
         value = ctx.REGEX().getText();
      }
      else if (ctx.str_set != null) {
         type = VariableType.SET_STRING;
         Set<String> strSet = new LinkedHashSet<String>();
         for (String_literal_string_exprContext t : ctx.str_elem) {
            strSet.add(t.text);
         }
         value = strSet;
      }
      else if (ctx.str != null) {
         type = VariableType.STRING;
         value = ctx.str.text;
      }
      else {
         throw new BatfishException("Invalid binding for variable: \"" + var
               + "\"");
      }
      if (_parameters.getTypeBindings().get(var) == null) {
         _parameters.getTypeBindings().put(var, type);
         _parameters.getStore().put(var, value);
      }
   }

   @Override
   public void enterExplicit_flow(Explicit_flowContext ctx) {
      _currentFlowBuilder = new FlowBuilder();
      _currentFlowBuilder.setTag(_flowTag);
   }

   @Override
   public void enterFlow_constraint_dst_ip(Flow_constraint_dst_ipContext ctx) {
      String valueText = ctx.dst_ip.getText();
      Ip dstIp;
      if (ctx.VARIABLE() != null) {
         String var = valueText.substring(1);
         dstIp = _parameters.getIp(var);
      }
      else {
         dstIp = new Ip(valueText);
      }
      _currentFlowBuilder.setDstIp(dstIp);
   }

   @Override
   public void enterFlow_constraint_dst_port(Flow_constraint_dst_portContext ctx) {
      String valueText = ctx.dst_port.getText();
      int dstPort;
      if (ctx.VARIABLE() != null) {
         String var = valueText.substring(1);
         dstPort = (int) _parameters.getInt(var);
      }
      else {
         dstPort = Integer.parseInt(valueText);
      }
      _currentFlowBuilder.setDstPort(dstPort);
   }

   @Override
   public void enterFlow_constraint_ingress_node(
         Flow_constraint_ingress_nodeContext ctx) {
      String ingressNode;
      if (ctx.ingress_node_var != null) {
         String var = ctx.ingress_node_var.getText().substring(1);
         ingressNode = _parameters.getString(var);
      }
      else {
         ingressNode = ctx.ingress_node_str.text;
      }
      _currentFlowBuilder.setIngressNode(ingressNode);
   }

   @Override
   public void enterFlow_constraint_ip_protocol(
         Flow_constraint_ip_protocolContext ctx) {
      String valueText = ctx.ip_protocol.getText();
      int ipProtocolInt;
      if (ctx.VARIABLE() != null) {
         String var = valueText.substring(1);
         ipProtocolInt = (int) _parameters.getInt(var);
      }
      else {
         ipProtocolInt = Integer.parseInt(valueText);
      }
      IpProtocol ipProtocol = IpProtocol.fromNumber(ipProtocolInt);
      _currentFlowBuilder.setIpProtocol(ipProtocol);
   }

   @Override
   public void enterFlow_constraint_src_ip(Flow_constraint_src_ipContext ctx) {
      String valueText = ctx.src_ip.getText();
      Ip srcIp;
      if (ctx.VARIABLE() != null) {
         String var = valueText.substring(1);
         srcIp = _parameters.getIp(var);
      }
      else {
         srcIp = new Ip(valueText);
      }
      _currentFlowBuilder.setSrcIp(srcIp);
   }

   @Override
   public void enterFlow_constraint_src_port(Flow_constraint_src_portContext ctx) {
      String valueText = ctx.src_port.getText();
      int srcPort;
      if (ctx.VARIABLE() != null) {
         String var = valueText.substring(1);
         srcPort = (int) _parameters.getInt(var);
      }
      else {
         srcPort = Integer.parseInt(valueText);
      }
      _currentFlowBuilder.setSrcPort(srcPort);
   }

   @Override
   public void enterIngress_path_question(Ingress_path_questionContext ctx) {
      IngressPathQuestion question = new IngressPathQuestion(_parameters);
      _question = question;
   }

   @Override
   public void enterLocal_path_question(Local_path_questionContext ctx) {
      LocalPathQuestion question = new LocalPathQuestion(_parameters);
      _question = question;
   }

   @Override
   public void enterMultipath_question(Multipath_questionContext ctx) {
      MultipathQuestion multipathQuestion = new MultipathQuestion(_parameters);
      _question = multipathQuestion;
   }

   @Override
   public void enterNeighbors_constraint_neighbor_type(
         Neighbors_constraint_neighbor_typeContext ctx) {
      _neighborsQuestion.getNeighborTypes().clear();
      NeighborType nType = toNeighborType(ctx.neighbor_type_constraint());
      _neighborsQuestion.getNeighborTypes().add(nType);
   }

   @Override
   public void enterNeighbors_constraint_dst_node(
         Neighbors_constraint_dst_nodeContext ctx) {
      String regex = toRegex(ctx.node_constraint());
      _neighborsQuestion.setDstNodeRegex(regex);
   }

   @Override
   public void enterNeighbors_constraint_src_node(
         Neighbors_constraint_src_nodeContext ctx) {
      String regex = toRegex(ctx.node_constraint());
      _neighborsQuestion.setSrcNodeRegex(regex);
   }

   @Override
   public void enterNeighbors_question(Neighbors_questionContext ctx) {
      _neighborsQuestion = new NeighborsQuestion(_parameters);
      _question = _neighborsQuestion;
   }

   @Override
   public void enterNodes_constraint_node_type(
         Nodes_constraint_node_typeContext ctx) {
      _nodesQuestion.getNodeTypes().clear();
      NodeType nType = toNodeType(ctx.node_type_constraint());
      _nodesQuestion.getNodeTypes().add(nType);
   }

   @Override
   public void enterNodes_constraint_node(
         Nodes_constraint_nodeContext ctx) {
      String regex = toRegex(ctx.node_constraint());
      _nodesQuestion.setNodeRegex(regex);
   }

   @Override
   public void enterNodes_question(Nodes_questionContext ctx) {
      _nodesQuestion = new NodesQuestion(_parameters);
      _question = _nodesQuestion;
   }

   @Override
   public void enterProtocol_dependencies_question(
         Protocol_dependencies_questionContext ctx) {
      _question = new ProtocolDependenciesQuestion(_parameters);
   }

   @Override
   public void enterReachability_constraint_action(
         Reachability_constraint_actionContext ctx) {
      _reachabilityQuestion.getActions().clear();
      ForwardingAction action = toAction(ctx.action_constraint());
      _reachabilityQuestion.getActions().add(action);
   }

   @Override
   public void enterReachability_constraint_dst_ip(
         Reachability_constraint_dst_ipContext ctx) {
      Set<Prefix> prefixes = toPrefixSet(ctx.ip_constraint());
      _reachabilityQuestion.getDstPrefixes().addAll(prefixes);
   }

   @Override
   public void enterReachability_constraint_dst_port(
         Reachability_constraint_dst_portContext ctx) {
      Set<SubRange> range = toRange(ctx.range_constraint());
      _reachabilityQuestion.getDstPortRange().addAll(range);
   }

   @Override
   public void enterReachability_constraint_final_node(
         Reachability_constraint_final_nodeContext ctx) {
      String regex = toRegex(ctx.node_constraint());
      _reachabilityQuestion.setFinalNodeRegex(regex);
   }

   @Override
   public void enterReachability_constraint_icmp_code(
         Reachability_constraint_icmp_codeContext ctx) {
      int icmpCode = toInt(ctx.int_constraint());
      _reachabilityQuestion.setIcmpCode(icmpCode);
   }

   @Override
   public void enterReachability_constraint_icmp_type(
         Reachability_constraint_icmp_typeContext ctx) {
      int icmpType = toInt(ctx.int_constraint());
      _reachabilityQuestion.setIcmpType(icmpType);
   }

   @Override
   public void enterReachability_constraint_ingress_node(
         Reachability_constraint_ingress_nodeContext ctx) {
      String regex = toRegex(ctx.node_constraint());
      _reachabilityQuestion.setIngressNodeRegex(regex);
   }

   @Override
   public void enterReachability_constraint_ip_protocol(
         Reachability_constraint_ip_protocolContext ctx) {
      Set<SubRange> range = toRange(ctx.range_constraint());
      _reachabilityQuestion.getIpProtocolRange().addAll(range);
   }

   @Override
   public void enterReachability_constraint_src_ip(
         Reachability_constraint_src_ipContext ctx) {
      Set<Prefix> prefixes = toPrefixSet(ctx.ip_constraint());
      _reachabilityQuestion.getSrcPrefixes().addAll(prefixes);
   }

   @Override
   public void enterReachability_constraint_src_port(
         Reachability_constraint_src_portContext ctx) {
      Set<SubRange> range = toRange(ctx.range_constraint());
      _reachabilityQuestion.getSrcPortRange().addAll(range);
   }

   @Override
   public void enterReachability_constraint_tcp_flags(
         Reachability_constraint_tcp_flagsContext ctx) {
      // TODO: allow constraining tcpFlags
      /*
       * int tcpFlags = toInt(ctx.int_constraint());
       * _reachabilityQuestion.setTcpFlags(tcpFlags);
       */
   }

   @Override
   public void enterReachability_question(Reachability_questionContext ctx) {
      _reachabilityQuestion = new ReachabilityQuestion(_parameters);
      _question = _reachabilityQuestion;
   }

   @Override
   public void enterReduced_reachability_question(
         Reduced_reachability_questionContext ctx) {
      ReducedReachabilityQuestion reducedReachabilityQuestion = new ReducedReachabilityQuestion(
            _parameters);
      _question = reducedReachabilityQuestion;
   }

   @Override
   public void enterTraceroute_question(Traceroute_questionContext ctx) {
      TracerouteQuestion question = new TracerouteQuestion(_parameters);
      _question = question;
      _tracerouteFlowBuilders = question.getFlowBuilders();
   }

   @Override
   public void enterVerify_question(Verify_questionContext ctx) {
      VerifyQuestion verifyQuestion = new VerifyQuestion(_parameters);
      _question = verifyQuestion;
      _verifyProgram = verifyQuestion.getProgram();
      for (StatementContext sctx : ctx.statement()) {
         Statement statement = toStatement(sctx);
         _verifyProgram.getStatements().add(statement);
      }
   }

   @Override
   public void exitAcl_reachability_question(
         Acl_reachability_questionContext ctx) {
      _question = new AclReachabilityQuestion(_parameters);
   }

   @Override
   public void exitExplicit_flow(Explicit_flowContext ctx) {
      _tracerouteFlowBuilders.add(_currentFlowBuilder);
      _currentFlowBuilder = null;
   }

   public Question getQuestion() {
      return _question;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   private void routes() {
      _verifyProgram.setDataPlane(true);
      _verifyProgram.setDataPlaneRoutes(true);
   }

   private ForwardingAction toAction(Action_constraintContext ctx) {
      ForwardingAction action;
      if (ctx.action() != null) {
         if (ctx.action().ACCEPT() != null) {
            action = ForwardingAction.ACCEPT;
         }
         else if (ctx.action().DROP() != null) {
            action = ForwardingAction.DROP;
         }
         else {
            throw conversionError(ERR_CONVERT_ACTION, ctx);
         }
      }
      else if (ctx.VARIABLE() != null) {
         action = _parameters.getAction(ctx.VARIABLE().getText().substring(1));
      }
      else {
         throw conversionError(ERR_CONVERT_ACTION, ctx);
      }
      return action;
   }

   private BgpAdvertisementExpr toBgpAdvertisementExpr(
         Bgp_advertisement_exprContext ctx) {
      if (ctx.RECEIVED_EBGP_ADVERTISEMENT() != null) {
         return BaseCaseBgpAdvertisementExpr.RECEIVED_EBGP_ADVERTISEMENT;
      }
      else if (ctx.RECEIVED_IBGP_ADVERTISEMENT() != null) {
         return BaseCaseBgpAdvertisementExpr.RECEIVED_IBGP_ADVERTISEMENT;
      }
      else if (ctx.SENT_EBGP_ADVERTISEMENT() != null) {
         return BaseCaseBgpAdvertisementExpr.SENT_EBGP_ADVERTISEMENT;
      }
      else if (ctx.SENT_IBGP_ADVERTISEMENT() != null) {
         return BaseCaseBgpAdvertisementExpr.SENT_IBGP_ADVERTISEMENT;
      }
      else {
         throw conversionError(ERR_CONVERT_BGP_ADVERTISEMENT, ctx);
      }
   }

   private BgpNeighborExpr toBgpNeighborExpr(Bgp_neighbor_exprContext ctx) {
      if (ctx.BGP_NEIGHBOR() != null) {
         return BaseCaseBgpNeighborExpr.BGP_NEIGHBOR;
      }
      else if (ctx.REMOTE_BGP_NEIGHBOR() != null) {
         return BaseCaseBgpNeighborExpr.REMOTE_BGP_NEIGHBOR;
      }
      else if (ctx.variable() != null) {
         return new VarBgpNeighborExpr(ctx.variable().var.getText());
      }
      else {
         throw conversionError(ERR_CONVERT_BGP_NEIGHBOR, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(And_exprContext expr) {
      Set<BooleanExpr> conjuncts = new HashSet<BooleanExpr>();
      for (Boolean_exprContext conjunctCtx : expr.conjuncts) {
         BooleanExpr conjunct = toBooleanExpr(conjunctCtx);
         conjuncts.add(conjunct);
      }
      AndExpr andExpr = new AndExpr(conjuncts);
      return andExpr;
   }

   private BooleanExpr toBooleanExpr(Bgp_neighbor_boolean_exprContext ctx) {
      BgpNeighborExpr caller = toBgpNeighborExpr(ctx.caller);
      if (ctx.bgp_neighbor_ebgp_multihop_boolean_expr() != null) {
         return new EbgpMultihopBgpNeighborBooleanExpr(caller);
      }
      else if (ctx.bgp_neighbor_has_generated_route_boolean_expr() != null) {
         return new HasGeneratedRouteBgpNeighborBooleanExpr(caller);
      }
      else if (ctx.bgp_neighbor_has_local_ip_boolean_expr() != null) {
         return new HasLocalIpBgpNeighborBooleanExpr(caller);
      }
      else if (ctx.bgp_neighbor_has_remote_bgp_neighbor_boolean_expr() != null) {
         return new HasRemoteBgpNeighborBgpNeighborBooleanExpr(caller);
      }
      else if (ctx.bgp_neighbor_has_single_remote_bgp_neighbor_boolean_expr() != null) {
         return new HasSingleRemoteBgpNeighborBgpNeighborBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Boolean_exprContext ctx) {
      if (ctx.and_expr() != null) {
         return toBooleanExpr(ctx.and_expr());
      }
      else if (ctx.false_expr() != null) {
         return BaseCaseBooleanExpr.FALSE;
      }
      else if (ctx.eq_expr() != null) {
         return toBooleanExpr(ctx.eq_expr());
      }
      else if (ctx.ge_expr() != null) {
         return toBooleanExpr(ctx.ge_expr());
      }
      else if (ctx.gt_expr() != null) {
         return toBooleanExpr(ctx.gt_expr());
      }
      else if (ctx.if_expr() != null) {
         return toBooleanExpr(ctx.if_expr());
      }
      else if (ctx.le_expr() != null) {
         return toBooleanExpr(ctx.le_expr());
      }
      else if (ctx.lt_expr() != null) {
         return toBooleanExpr(ctx.lt_expr());
      }
      else if (ctx.neq_expr() != null) {
         return toBooleanExpr(ctx.neq_expr());
      }
      else if (ctx.not_expr() != null) {
         return toBooleanExpr(ctx.not_expr());
      }
      else if (ctx.or_expr() != null) {
         return toBooleanExpr(ctx.or_expr());
      }
      else if (ctx.property_boolean_expr() != null) {
         return toBooleanExpr(ctx.property_boolean_expr());
      }
      else if (ctx.set_contains_expr() != null) {
         return toBooleanExpr(ctx.set_contains_expr());
      }
      else if (ctx.true_expr() != null) {
         return BaseCaseBooleanExpr.TRUE;
      }
      else if (ctx.variable() != null) {
         return toBooleanExpr(ctx.variable());
      }
      else if (ctx.lhs != null) {
         BooleanExpr lhs = toBooleanExpr(ctx.lhs);
         BooleanExpr rhs = toBooleanExpr(ctx.rhs);
         if (ctx.DOUBLE_EQUALS() != null) {
            return new EqExpr(lhs, rhs);
         }
         else if (ctx.NOT_EQUALS() != null) {
            return new NeqExpr(lhs, rhs);
         }
      }
      throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
   }

   private BooleanExpr toBooleanExpr(Eq_exprContext ctx) {
      Expr lhs = toExpr(ctx.lhs);
      Expr rhs = toExpr(ctx.rhs);
      return new EqExpr(lhs, rhs);
   }

   private BooleanExpr toBooleanExpr(Ge_exprContext ctx) {
      if (ctx.lhs_int != null) {
         IntExpr lhs = toIntExpr(ctx.lhs_int);
         IntExpr rhs = toIntExpr(ctx.rhs_int);
         return new IntGeExpr(lhs, rhs);
      }
      else if (ctx.lhs_string != null) {
         StringExpr lhs = toStringExpr(ctx.lhs_string);
         StringExpr rhs = toStringExpr(ctx.rhs_string);
         return new StringGeExpr(lhs, rhs);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Gt_exprContext ctx) {
      if (ctx.lhs_int != null) {
         IntExpr lhs = toIntExpr(ctx.lhs_int);
         IntExpr rhs = toIntExpr(ctx.rhs_int);
         return new IntGtExpr(lhs, rhs);
      }
      else if (ctx.lhs_string != null) {
         StringExpr lhs = toStringExpr(ctx.lhs_string);
         StringExpr rhs = toStringExpr(ctx.rhs_string);
         return new StringGtExpr(lhs, rhs);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(If_exprContext ctx) {
      BooleanExpr antecedent = toBooleanExpr(ctx.antecedent);
      BooleanExpr consequent = toBooleanExpr(ctx.consequent);
      IfExpr ifExpr = new IfExpr(antecedent, consequent);
      return ifExpr;
   }

   private BooleanExpr toBooleanExpr(Interface_boolean_exprContext ctx) {
      InterfaceExpr caller = toInterfaceExpr(ctx.caller);
      if (ctx.interface_enabled_boolean_expr() != null) {
         return new EnabledInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_has_ip_boolean_expr() != null) {
         return new HasIpInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_isis_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.interface_isis_boolean_expr());
      }
      else if (ctx.interface_is_loopback_boolean_expr() != null) {
         return new IsLoopbackInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_ospf_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.interface_ospf_boolean_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(InterfaceExpr caller,
         Interface_isis_boolean_exprContext ctx) {
      if (ctx.interface_isis_l1_active_boolean_expr() != null) {
         return new IsisL1ActiveInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_isis_l1_passive_boolean_expr() != null) {
         return new IsisL1PassiveInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_isis_l2_active_boolean_expr() != null) {
         return new IsisL2ActiveInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_isis_l2_passive_boolean_expr() != null) {
         return new IsisL2PassiveInterfaceBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(InterfaceExpr caller,
         Interface_ospf_boolean_exprContext ctx) {
      if (ctx.interface_ospf_active_boolean_expr() != null) {
         return new OspfActiveInterfaceBooleanExpr(caller);
      }
      else if (ctx.interface_ospf_passive_boolean_expr() != null) {
         return new OspfPassiveInterfaceBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Ipsec_vpn_boolean_exprContext ctx) {
      IpsecVpnExpr caller = toIpsecVpnExpr(ctx.ipsec_vpn_expr());
      if (ctx.ipsec_vpn_compatible_ike_proposals_boolean_expr() != null) {
         return new CompatibleIkeProposalsIpsecVpnBooleanExpr(caller);
      }
      else if (ctx.ipsec_vpn_compatible_ipsec_proposals_boolean_expr() != null) {
         return new CompatibleIpsecProposalsIpsecVpnBooleanExpr(caller);
      }
      else if (ctx.ipsec_vpn_has_remote_ip_boolean_expr() != null) {
         return new HasRemoteIpIpsecVpnBooleanExpr(caller);
      }
      else if (ctx.ipsec_vpn_has_remote_ipsec_vpn_boolean_expr() != null) {
         return new HasRemoteIpsecVpnIpsecVpnBooleanExpr(caller);
      }
      else if (ctx.ipsec_vpn_has_single_remote_ipsec_vpn_boolean_expr() != null) {
         return new HasSingleRemoteIpsecVpnIpsecVpnBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Le_exprContext ctx) {
      if (ctx.lhs_int != null) {
         IntExpr lhs = toIntExpr(ctx.lhs_int);
         IntExpr rhs = toIntExpr(ctx.rhs_int);
         return new IntLeExpr(lhs, rhs);
      }
      else if (ctx.lhs_string != null) {
         StringExpr lhs = toStringExpr(ctx.lhs_string);
         StringExpr rhs = toStringExpr(ctx.rhs_string);
         return new StringLeExpr(lhs, rhs);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Line_boolean_exprContext ctx) {
      RouteFilterLineExpr caller = toRouteFilterLineExpr(ctx.caller);
      if (ctx.line_permit_boolean_expr() != null) {
         return new PermitLineBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Lt_exprContext ctx) {
      if (ctx.lhs_int != null) {
         IntExpr lhs = toIntExpr(ctx.lhs_int);
         IntExpr rhs = toIntExpr(ctx.rhs_int);
         return new IntLtExpr(lhs, rhs);
      }
      else if (ctx.lhs_string != null) {
         StringExpr lhs = toStringExpr(ctx.lhs_string);
         StringExpr rhs = toStringExpr(ctx.rhs_string);
         return new StringLtExpr(lhs, rhs);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Neq_exprContext ctx) {
      Expr lhs = toExpr(ctx.lhs);
      Expr rhs = toExpr(ctx.rhs);
      return new NeqExpr(lhs, rhs);
   }

   private BooleanExpr toBooleanExpr(Node_boolean_exprContext ctx) {
      NodeExpr caller = toNodeExpr(ctx.node_expr());
      if (ctx.node_bgp_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.node_bgp_boolean_expr());
      }
      else if (ctx.node_has_generated_route_boolean_expr() != null) {
         return new HasGeneratedRouteNodeBooleanExpr(caller);
      }
      else if (ctx.node_isis_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.node_isis_boolean_expr());
      }
      else if (ctx.node_ospf_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.node_ospf_boolean_expr());
      }
      else if (ctx.node_static_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.node_static_boolean_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(NodeExpr caller,
         Node_bgp_boolean_exprContext ctx) {
      if (ctx.node_bgp_configured_boolean_expr() != null) {
         return new BgpConfiguredNodeBooleanExpr(caller);
      }
      else if (ctx.node_bgp_has_generated_route_boolean_expr() != null) {
         return new BgpHasGeneratedRouteNodeBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(NodeExpr caller,
         Node_isis_boolean_exprContext ctx) {
      if (ctx.node_isis_configured_boolean_expr() != null) {
         return new IsisConfiguredNodeBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(NodeExpr caller,
         Node_ospf_boolean_exprContext ctx) {
      if (ctx.node_ospf_configured_boolean_expr() != null) {
         return new OspfConfiguredNodeBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(NodeExpr caller,
         Node_static_boolean_exprContext ctx) {
      if (ctx.node_static_configured_boolean_expr() != null) {
         return new StaticConfiguredNodeBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Not_exprContext ctx) {
      BooleanExpr argument = toBooleanExpr(ctx.boolean_expr());
      NotExpr notExpr = new NotExpr(argument);
      return notExpr;
   }

   private BooleanExpr toBooleanExpr(Or_exprContext ctx) {
      Set<BooleanExpr> disjuncts = new HashSet<BooleanExpr>();
      for (Boolean_exprContext disjunctCtx : ctx.disjuncts) {
         BooleanExpr disjunct = toBooleanExpr(disjunctCtx);
         disjuncts.add(disjunct);
      }
      OrExpr orExpr = new OrExpr(disjuncts);
      return orExpr;
   }

   private BooleanExpr toBooleanExpr(Policy_map_clause_boolean_exprContext ctx) {
      PolicyMapClauseExpr caller = toPolicyMapClauseExpr(ctx.caller);
      if (ctx.policy_map_clause_permit_boolean_expr() != null) {
         return new PermitPolicyMapClauseBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Prefix_space_boolean_exprContext ctx) {
      PrefixSpaceExpr caller = toPrefixSpaceExpr(ctx.caller);
      if (ctx.prefix_space_overlaps_boolean_expr() != null) {
         return toBooleanExpr(caller, ctx.prefix_space_overlaps_boolean_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(PrefixSpaceExpr caller,
         Prefix_space_overlaps_boolean_exprContext ctx) {
      PrefixSpaceExpr arg = toPrefixSpaceExpr(ctx.arg);
      return new OverlapsPrefixSpaceBooleanExpr(caller, arg);
   }

   private BooleanExpr toBooleanExpr(Property_boolean_exprContext ctx) {
      if (ctx.bgp_neighbor_boolean_expr() != null) {
         return toBooleanExpr(ctx.bgp_neighbor_boolean_expr());
      }
      else if (ctx.interface_boolean_expr() != null) {
         return toBooleanExpr(ctx.interface_boolean_expr());
      }
      else if (ctx.ipsec_vpn_boolean_expr() != null) {
         return toBooleanExpr(ctx.ipsec_vpn_boolean_expr());
      }
      else if (ctx.line_boolean_expr() != null) {
         return toBooleanExpr(ctx.line_boolean_expr());
      }
      else if (ctx.node_boolean_expr() != null) {
         return toBooleanExpr(ctx.node_boolean_expr());
      }
      else if (ctx.policy_map_clause_boolean_expr() != null) {
         return toBooleanExpr(ctx.policy_map_clause_boolean_expr());
      }
      else if (ctx.prefix_space_boolean_expr() != null) {
         return toBooleanExpr(ctx.prefix_space_boolean_expr());
      }
      else if (ctx.static_route_boolean_expr() != null) {
         return toBooleanExpr(ctx.static_route_boolean_expr());
      }
      else {
         throw conversionError("Missing conversion for expression", ctx);
      }
   }

   private BooleanExpr toBooleanExpr(Set_contains_exprContext ctx) {
      Expr expr;
      if (ctx.bgp_neighbor_expr() != null) {
         expr = toBgpNeighborExpr(ctx.bgp_neighbor_expr());
      }
      else if (ctx.interface_expr() != null) {
         expr = toInterfaceExpr(ctx.interface_expr());
      }
      else if (ctx.ip_expr() != null) {
         expr = toIpExpr(ctx.ip_expr());
      }
      else if (ctx.ipsec_vpn_expr() != null) {
         expr = toIpsecVpnExpr(ctx.ipsec_vpn_expr());
      }
      else if (ctx.node_expr() != null) {
         expr = toNodeExpr(ctx.node_expr());
      }
      else if (ctx.prefix_expr() != null) {
         expr = toPrefixExpr(ctx.prefix_expr());
      }
      else if (ctx.prefix_space_expr() != null) {
         expr = toPrefixSpaceExpr(ctx.prefix_space_expr());
      }
      else if (ctx.policy_map_expr() != null) {
         expr = toPolicyMapExpr(ctx.policy_map_expr());
      }
      else if (ctx.policy_map_clause_expr() != null) {
         expr = toPolicyMapClauseExpr(ctx.policy_map_clause_expr());
      }
      else if (ctx.route_filter_expr() != null) {
         expr = toRouteFilterExpr(ctx.route_filter_expr());
      }
      else if (ctx.route_filter_line_expr() != null) {
         expr = toRouteFilterLineExpr(ctx.route_filter_line_expr());
      }
      else if (ctx.static_route_expr() != null) {
         expr = toStaticRouteExpr(ctx.static_route_expr());
      }
      else if (ctx.string_expr() != null) {
         expr = toStringExpr(ctx.string_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
      return new SetContainsExpr(expr, ctx.caller.getText(), ctx.type);
   }

   private BooleanExpr toBooleanExpr(Static_route_boolean_exprContext ctx) {
      StaticRouteExpr caller = toStaticRouteExpr(ctx.caller);
      if (ctx.static_route_has_next_hop_interface_boolean_expr() != null) {
         return new HasNextHopInterfaceStaticRouteBooleanExpr(caller);
      }
      else if (ctx.static_route_has_next_hop_ip_boolean_expr() != null) {
         return new HasNextHopIpStaticRouteBooleanExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_BOOLEAN, ctx);
      }
   }

   private BooleanExpr toBooleanExpr(VariableContext ctx) {
      String variable = ctx.VARIABLE().getText();
      return new VarBooleanExpr(variable);
   }

   private Expr toExpr(ExprContext ctx) {
      if (ctx.bgp_neighbor_expr() != null) {
         return toBgpNeighborExpr(ctx.bgp_neighbor_expr());
      }
      else if (ctx.int_expr() != null) {
         return toIntExpr(ctx.int_expr());
      }
      else if (ctx.interface_expr() != null) {
         return toInterfaceExpr(ctx.interface_expr());
      }
      else if (ctx.ip_expr() != null) {
         return toIpExpr(ctx.ip_expr());
      }
      else if (ctx.ipsec_vpn_expr() != null) {
         return toIpsecVpnExpr(ctx.ipsec_vpn_expr());
      }
      else if (ctx.node_expr() != null) {
         return toNodeExpr(ctx.node_expr());
      }
      else if (ctx.policy_map_expr() != null) {
         return toPolicyMapExpr(ctx.policy_map_expr());
      }
      else if (ctx.policy_map_clause_expr() != null) {
         return toPolicyMapClauseExpr(ctx.policy_map_clause_expr());
      }
      else if (ctx.prefix_expr() != null) {
         return toPrefixExpr(ctx.prefix_expr());
      }
      else if (ctx.prefix_space_expr() != null) {
         return toPrefixSpaceExpr(ctx.prefix_space_expr());
      }
      else if (ctx.route_filter_expr() != null) {
         return toRouteFilterExpr(ctx.route_filter_expr());
      }
      else if (ctx.route_filter_line_expr() != null) {
         return toRouteFilterLineExpr(ctx.route_filter_line_expr());
      }
      else if (ctx.set_ip_expr() != null) {
         return toIpSetExpr(ctx.set_ip_expr());
      }
      else if (ctx.set_prefix_expr() != null) {
         return toPrefixSetExpr(ctx.set_prefix_expr());
      }
      else if (ctx.set_string_expr() != null) {
         return toStringSetExpr(ctx.set_string_expr());
      }
      else if (ctx.static_route_expr() != null) {
         return toStaticRouteExpr(ctx.static_route_expr());
      }
      else if (ctx.string_expr() != null) {
         return toStringExpr(ctx.string_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_PRINTABLE, ctx);
      }
   }

   private Expr toExpr(Printable_exprContext ctx) {
      if (ctx.expr() != null) {
         return toExpr(ctx.expr());
      }
      else if (ctx.boolean_expr() != null) {
         return toBooleanExpr(ctx.boolean_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_PRINTABLE, ctx);
      }
   }

   private int toInt(Int_constraintContext ctx) {
      if (ctx.DEC() != null) {
         return Integer.parseInt(ctx.DEC().getText());
      }
      else if (ctx.VARIABLE() != null) {
         String var = ctx.VARIABLE().getText().substring(1);
         VariableType type = _parameters.getTypeBindings().get(var);
         switch (type) {
         case INT:
            int value = (int) _parameters.getInt(var);
            return value;

            // $CASES-OMITTED$
         default:
            throw new BatfishException("invalid constraint");
         }
      }
      else {
         throw new BatfishException("invalid int constraint: " + ctx.getText());
      }
   }

   private InterfaceExpr toInterfaceExpr(Interface_exprContext ctx) {
      if (ctx.INTERFACE() != null) {
         return BaseCaseInterfaceExpr.INTERFACE;
      }
      else if (ctx.variable() != null) {
         return new VarInterfaceExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_INTERFACE, ctx);
      }
   }

   private IntExpr toIntExpr(Bgp_advertisement_int_exprContext ctx) {
      BgpAdvertisementExpr caller = toBgpAdvertisementExpr(ctx.caller);
      if (ctx.bgp_advertisement_local_preference_int_expr() != null) {
         return new LocalPreferenceBgpAdvertisementIntExpr(caller);
      }
      else if (ctx.bgp_advertisement_med_int_expr() != null) {
         return new MedBgpAdvertisementIntExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(Bgp_neighbor_int_exprContext ctx) {
      BgpNeighborExpr caller = toBgpNeighborExpr(ctx.caller);
      if (ctx.bgp_neighbor_local_as_int_expr() != null) {
         return new LocalAsBgpNeighborIntExpr(caller);
      }
      else if (ctx.bgp_neighbor_remote_as_int_expr() != null) {
         return new RemoteAsBgpNeighborIntExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(Int_exprContext ctx) {
      if (ctx.ASTERISK() != null) {
         IntExpr multiplicand1 = toIntExpr(ctx.multiplicand1);
         IntExpr multiplicand2 = toIntExpr(ctx.multiplicand2);
         return new ProductIntExpr(multiplicand1, multiplicand2);
      }
      else if (ctx.FORWARD_SLASH() != null) {
         IntExpr dividend = toIntExpr(ctx.dividend);
         IntExpr divisor = toIntExpr(ctx.divisor);
         return new QuotientIntExpr(dividend, divisor);
      }
      else if (ctx.MINUS() != null) {
         IntExpr subtrahend = toIntExpr(ctx.subtrahend);
         IntExpr minuend = toIntExpr(ctx.minuend);
         return new DifferenceIntExpr(subtrahend, minuend);
      }
      else if (ctx.OPEN_PAREN() != null) {
         return toIntExpr(ctx.parenthesized);
      }
      else if (ctx.PLUS() != null) {
         IntExpr addend1 = toIntExpr(ctx.addend1);
         IntExpr addend2 = toIntExpr(ctx.addend2);
         return new SumIntExpr(addend1, addend2);
      }
      else if (ctx.val_int_expr() != null) {
         return toIntExpr(ctx.val_int_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(Literal_int_exprContext ctx) {
      int i = Integer.parseInt(ctx.DEC().toString());
      return new LiteralIntExpr(i);
   }

   private IntExpr toIntExpr(Route_int_exprContext ctx) {
      RouteExpr caller = toRouteExpr(ctx.caller);
      if (ctx.route_administrative_cost_int_expr() != null) {
         return new AdministrativeCostRouteIntExpr(caller);
      }
      else if (ctx.route_cost_int_expr() != null) {
         return new CostRouteIntExpr(caller);
      }
      else if (ctx.route_tag_int_expr() != null) {
         return new TagRouteIntExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(Set_size_int_exprContext ctx) {
      String caller = ctx.caller.getText();
      VariableType type = ctx.type;
      return new SetSizeIntExpr(caller, type);
   }

   private IntExpr toIntExpr(Static_route_int_exprContext ctx) {
      StaticRouteExpr caller = toStaticRouteExpr(ctx.caller);
      if (ctx.static_route_administrative_cost_int_expr() != null) {
         return new AdministrativeCostStaticRouteIntExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(Val_int_exprContext ctx) {
      if (ctx.bgp_advertisement_int_expr() != null) {
         return toIntExpr(ctx.bgp_advertisement_int_expr());
      }
      else if (ctx.bgp_neighbor_int_expr() != null) {
         return toIntExpr(ctx.bgp_neighbor_int_expr());
      }
      else if (ctx.literal_int_expr() != null) {
         return toIntExpr(ctx.literal_int_expr());
      }
      else if (ctx.set_size_int_expr() != null) {
         return toIntExpr(ctx.set_size_int_expr());
      }
      else if (ctx.route_int_expr() != null) {
         return toIntExpr(ctx.route_int_expr());
      }
      else if (ctx.static_route_int_expr() != null) {
         return toIntExpr(ctx.static_route_int_expr());
      }
      else if (ctx.variable() != null) {
         return toIntExpr(ctx.variable());
      }
      else {
         throw conversionError(ERR_CONVERT_INT, ctx);
      }
   }

   private IntExpr toIntExpr(VariableContext ctx) {
      String variable = ctx.VARIABLE().getText();
      return new VarIntExpr(variable);
   }

   private IpExpr toIpExpr(Bgp_advertisement_ip_exprContext ctx) {
      BgpAdvertisementExpr caller = toBgpAdvertisementExpr(ctx.caller);
      if (ctx.bgp_advertisement_dst_ip_ip_expr() != null) {
         return new DstIpBgpAdvertisementIpExpr(caller);
      }
      else if (ctx.bgp_advertisement_next_hop_ip_ip_expr() != null) {
         return new NextHopIpBgpAdvertisementIpExpr(caller);
      }
      else if (ctx.bgp_advertisement_src_ip_ip_expr() != null) {
         return new SrcIpBgpAdvertisementIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Bgp_neighbor_ip_exprContext ctx) {
      BgpNeighborExpr caller = toBgpNeighborExpr(ctx.caller);
      if (ctx.bgp_neighbor_local_ip_ip_expr() != null) {
         return new LocalIpBgpNeighborIpExpr(caller);
      }
      else if (ctx.bgp_neighbor_remote_ip_ip_expr() != null) {
         return new RemoteIpBgpNeighborIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Interface_ip_exprContext ctx) {
      InterfaceExpr caller = toInterfaceExpr(ctx.caller);
      if (ctx.interface_ip_ip_expr() != null) {
         return new IpInterfaceIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Ip_exprContext ctx) {
      if (ctx.bgp_advertisement_ip_expr() != null) {
         return toIpExpr(ctx.bgp_advertisement_ip_expr());
      }
      else if (ctx.bgp_neighbor_ip_expr() != null) {
         return toIpExpr(ctx.bgp_neighbor_ip_expr());
      }
      else if (ctx.interface_ip_expr() != null) {
         return toIpExpr(ctx.interface_ip_expr());
      }
      else if (ctx.ipsec_vpn_ip_expr() != null) {
         return toIpExpr(ctx.ipsec_vpn_ip_expr());
      }
      else if (ctx.prefix_ip_expr() != null) {
         return toIpExpr(ctx.prefix_ip_expr());
      }
      else if (ctx.route_ip_expr() != null) {
         return toIpExpr(ctx.route_ip_expr());
      }
      else if (ctx.static_route_ip_expr() != null) {
         return toIpExpr(ctx.static_route_ip_expr());
      }
      else if (ctx.IP_ADDRESS() != null) {
         return new LiteralIpExpr(new Ip(ctx.IP_ADDRESS().getText()));
      }
      else if (ctx.variable() != null) {
         return new VarIpExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Ipsec_vpn_ip_exprContext ctx) {
      IpsecVpnExpr caller = toIpsecVpnExpr(ctx.caller);
      if (ctx.ipsec_vpn_remote_ip_ip_expr() != null) {
         return new RemoteIpIpsecVpnIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Prefix_ip_exprContext ctx) {
      PrefixExpr caller = toPrefixExpr(ctx.caller);
      if (ctx.prefix_address_ip_expr() != null) {
         return new AddressPrefixIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Route_ip_exprContext ctx) {
      RouteExpr caller = toRouteExpr(ctx.caller);
      if (ctx.route_next_hop_ip_ip_expr() != null) {
         return new NextHopIpRouteIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpExpr toIpExpr(Static_route_ip_exprContext ctx) {
      StaticRouteExpr caller = toStaticRouteExpr(ctx.caller);
      if (ctx.static_route_next_hop_ip_ip_expr() != null) {
         return new NextHopIpStaticRouteIpExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IP, ctx);
      }
   }

   private IpsecVpnExpr toIpsecVpnExpr(Ipsec_vpn_exprContext ctx) {
      if (ctx.IPSEC_VPN() != null) {
         return BaseCaseIpsecVpnExpr.IPSEC_VPN;
      }
      else if (ctx.REMOTE_IPSEC_VPN() != null) {
         return BaseCaseIpsecVpnExpr.REMOTE_IPSEC_VPN;
      }
      else if (ctx.ipsec_vpn_expr() != null) {
         return toIpsecVpnExpr(ctx.ipsec_vpn_expr(),
               ctx.ipsec_vpn_ipsec_vpn_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_IPSEC_VPN, ctx);
      }
   }

   private IpsecVpnExpr toIpsecVpnExpr(Ipsec_vpn_exprContext callerCtx,
         Ipsec_vpn_ipsec_vpn_exprContext ctx) {
      IpsecVpnExpr caller = toIpsecVpnExpr(callerCtx);
      if (ctx.ipsec_vpn_remote_ipsec_vpn_ipsec_vpn_expr() != null) {
         return new RemoteIpsecVpnIpsecVpnExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_IPSEC_VPN, ctx);
      }
   }

   private IpSetExpr toIpSetExpr(Set_ip_exprContext ctx) {
      if (ctx.variable() != null) {
         return toIpSetExpr(ctx.variable());
      }
      else {
         throw conversionError(ERR_CONVERT_IP_SET, ctx);
      }
   }

   private IpSetExpr toIpSetExpr(VariableContext ctx) {
      return new VarIpSetExpr(ctx.VARIABLE().getText());
   }

   private MapExpr toMapExpr(Map_exprContext ctx) {
      if (ctx.QUERY() != null) {
         return BaseCaseMapExpr.QUERY;
      }
      else if (ctx.new_map_expr() != null) {
         return new NewMapExpr();
      }
      else if (ctx.caller != null) {
         MapExpr caller = toMapExpr(ctx.caller);
         if (ctx.map_map_expr() != null) {
            return toMapExpr(caller, ctx.map_map_expr());
         }
         else {
            throw conversionError(ERR_CONVERT_MAP_EXPR, ctx);
         }
      }
      else if (ctx.variable() != null) {
         String var = ctx.variable().VARIABLE().getText();
         return new VarMapExpr(var);
      }
      else {
         throw conversionError(ERR_CONVERT_MAP_EXPR, ctx);
      }
   }

   private MapExpr toMapExpr(MapExpr caller, Map_get_map_map_exprContext ctx) {
      Expr key = toExpr(ctx.key);
      return new GetMapMapMapExpr(caller, key);
   }

   private MapExpr toMapExpr(MapExpr caller, Map_map_exprContext ctx) {
      if (ctx.map_get_map_map_expr() != null) {
         return toMapExpr(caller, ctx.map_get_map_map_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_MAP_EXPR, ctx);
      }
   }

   private NodeExpr toNodeExpr(Bgp_neighbor_node_exprContext ctx) {
      BgpNeighborExpr caller = toBgpNeighborExpr(ctx.caller);
      if (ctx.bgp_neighbor_owner_node_expr() != null) {
         return new OwnerBgpNeighborNodeExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_NODE, ctx);
      }
   }

   private NodeExpr toNodeExpr(Ipsec_vpn_node_exprContext ctx) {
      IpsecVpnExpr caller = toIpsecVpnExpr(ctx.caller);
      if (ctx.ipsec_vpn_owner_node_expr() != null) {
         return new OwnerIpsecVpnNodeExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_NODE, ctx);
      }
   }

   private NodeExpr toNodeExpr(Node_exprContext ctx) {
      if (ctx.NODE() != null) {
         return BaseCaseNodeExpr.NODE;
      }
      else if (ctx.bgp_neighbor_node_expr() != null) {
         return toNodeExpr(ctx.bgp_neighbor_node_expr());
      }
      else if (ctx.ipsec_vpn_node_expr() != null) {
         return toNodeExpr(ctx.ipsec_vpn_node_expr());
      }
      else if (ctx.variable() != null) {
         return new VarNodeExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_NODE, ctx);
      }

   }

   private NeighborType toNeighborType(Neighbor_type_constraintContext ctx) {
	   NeighborType nType;
	   if (ctx.neighbor_type() != null) {
		   nType = NeighborType.fromName(ctx.neighbor_type().getText());
	   }
	   else if (ctx.VARIABLE() != null) {
		   nType = _parameters.getNeighborType(ctx.VARIABLE().getText().substring(1));
	   }
	   else {
		   throw conversionError(ERR_CONVERT_ACTION, ctx);
	   }
	   return nType;
   }

   private NodeType toNodeType(Node_type_constraintContext ctx) {
	   NodeType nType;
	   if (ctx.node_type() != null) {
		   nType = NodeType.fromName(ctx.node_type().getText());
	   }
	   else if (ctx.VARIABLE() != null) {
		   nType = _parameters.getNodeType(ctx.VARIABLE().getText().substring(1));
	   }
	   else {
		   throw conversionError(ERR_CONVERT_ACTION, ctx);
	   }
	   return nType;
   }

   private PolicyMapClauseExpr toPolicyMapClauseExpr(
         Policy_map_clause_exprContext ctx) {
      if (ctx.CLAUSE() != null) {
         return BaseCasePolicyMapClauseExpr.CLAUSE;
      }
      else if (ctx.variable() != null) {
         return new VarPolicyMapClauseExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_POLICY_MAP_CLAUSE, ctx);
      }
   }

   private PolicyMapExpr toPolicyMapExpr(Policy_map_exprContext ctx) {
      if (ctx.variable() != null) {
         return new VarPolicyMapExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_POLICY_MAP, ctx);
      }
   }

   private Prefix toPrefix(Ip_constraint_simpleContext ctx) {
      if (ctx.IP_ADDRESS() != null) {
         Ip ip = new Ip(ctx.IP_ADDRESS().getText());
         return new Prefix(ip, 32);
      }
      else if (ctx.IP_PREFIX() != null) {
         return new Prefix(ctx.IP_PREFIX().getText());
      }
      else {
         throw conversionError(
               "invalid simple ip constraint: " + ctx.getText(), ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Bgp_advertisement_prefix_exprContext ctx) {
      BgpAdvertisementExpr caller = toBgpAdvertisementExpr(ctx.caller);
      if (ctx.bgp_advertisement_network_prefix_expr() != null) {
         return new NetworkBgpAdvertisementPrefixExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Generated_route_prefix_exprContext ctx) {
      if (ctx.generated_route_prefix_prefix_expr() != null) {
         return GeneratedRoutePrefixExpr.GENERATED_ROUTE_PREFIX;
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Interface_prefix_exprContext ctx) {
      InterfaceExpr caller = toInterfaceExpr(ctx.caller);
      if (ctx.interface_prefix_prefix_expr() != null) {
         return new PrefixInterfacePrefixExpr(caller);
      }
      else if (ctx.interface_subnet_prefix_expr() != null) {
         return new SubnetInterfacePrefixExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Prefix_exprContext ctx) {
      if (ctx.bgp_advertisement_prefix_expr() != null) {
         return toPrefixExpr(ctx.bgp_advertisement_prefix_expr());
      }
      else if (ctx.generated_route_prefix_expr() != null) {
         return toPrefixExpr(ctx.generated_route_prefix_expr());
      }
      else if (ctx.interface_prefix_expr() != null) {
         return toPrefixExpr(ctx.interface_prefix_expr());
      }
      else if (ctx.route_prefix_expr() != null) {
         return toPrefixExpr(ctx.route_prefix_expr());
      }
      else if (ctx.static_route_prefix_expr() != null) {
         return toPrefixExpr(ctx.static_route_prefix_expr());
      }
      else if (ctx.variable() != null) {
         return toPrefixExpr(ctx.variable());
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Route_prefix_exprContext ctx) {
      RouteExpr caller = toRouteExpr(ctx.caller);
      if (ctx.route_network_prefix_expr() != null) {
         return new NetworkRoutePrefixExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(Static_route_prefix_exprContext ctx) {
      StaticRouteExpr caller = toStaticRouteExpr(ctx.caller);
      if (ctx.static_route_prefix_prefix_expr() != null) {
         return new PrefixStaticRoutePrefixExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX, ctx);
      }
   }

   private PrefixExpr toPrefixExpr(VariableContext ctx) {
      String var = ctx.VARIABLE().getText();
      return new VarPrefixExpr(var);
   }

   private Set<Prefix> toPrefixSet(Ip_constraint_complexContext ctx) {
      Set<Prefix> prefixes = new LinkedHashSet<Prefix>();
      for (Ip_constraint_simpleContext sctx : ctx.ip_constraint_simple()) {
         Prefix prefix = toPrefix(sctx);
         prefixes.add(prefix);
      }
      return prefixes;
   }

   private Set<Prefix> toPrefixSet(Ip_constraintContext ctx) {
      if (ctx.ip_constraint_complex() != null) {
         return toPrefixSet(ctx.ip_constraint_complex());
      }
      else if (ctx.ip_constraint_simple() != null) {
         return Collections.singleton(toPrefix(ctx.ip_constraint_simple()));
      }
      else if (ctx.VARIABLE() != null) {
         Set<Prefix> prefixes;
         String var = ctx.VARIABLE().getText().substring(1);
         VariableType type = _parameters.getTypeBindings().get(var);
         switch (type) {
         case SET_PREFIX:
            prefixes = _parameters.getPrefixSet(var);
            break;

         case PREFIX:
            Prefix prefix = _parameters.getPrefix(var);
            prefixes = Collections.singleton(prefix);
            break;

         case IP:
            Ip ip = _parameters.getIp(var);
            prefixes = Collections.singleton(new Prefix(ip, 32));
            break;
         // $CASES-OMITTED$
         default:
            throw new BatfishException("invalid constraint");
         }
         return prefixes;
      }
      else {
         throw new BatfishException("invalid ip constraint: " + ctx.getText());
      }
   }

   private PrefixSetExpr toPrefixSetExpr(Interface_set_prefix_exprContext ctx) {
      InterfaceExpr caller = toInterfaceExpr(ctx.caller);
      if (ctx.interface_all_prefixes_set_prefix_expr() != null) {
         return new AllPrefixesInterfacePrefixSetExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX_SET, ctx);
      }
   }

   private PrefixSetExpr toPrefixSetExpr(Set_prefix_exprContext ctx) {
      if (ctx.interface_set_prefix_expr() != null) {
         return toPrefixSetExpr(ctx.interface_set_prefix_expr());
      }
      else if (ctx.variable() != null) {
         return toPrefixSetExpr(ctx.variable());
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX_SET, ctx);
      }
   }

   private PrefixSetExpr toPrefixSetExpr(VariableContext ctx) {
      return new VarPrefixSetExpr(ctx.VARIABLE().getText());
   }

   private PrefixSpaceExpr toPrefixSpaceExpr(Node_prefix_space_exprContext ctx) {
      NodeExpr caller = toNodeExpr(ctx.caller);
      if (ctx.node_bgp_origination_space_explicit_prefix_space_expr() != null) {
         return new BgpOriginationSpaceExplicitNodePrefixSpaceExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX_SPACE, ctx);
      }
   }

   private PrefixSpaceExpr toPrefixSpaceExpr(Prefix_space_exprContext ctx) {
      if (ctx.node_prefix_space_expr() != null) {
         return toPrefixSpaceExpr(ctx.node_prefix_space_expr());
      }
      else if (ctx.prefix_space_prefix_space_expr() != null) {
         PrefixSpaceExpr caller = toPrefixSpaceExpr(ctx.prefix_space_expr());
         return toPrefixSpaceExpr(caller, ctx.prefix_space_prefix_space_expr());
      }
      else if (ctx.variable() != null) {
         return new VarPrefixSpaceExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX_SPACE, ctx);
      }
   }

   private PrefixSpaceExpr toPrefixSpaceExpr(PrefixSpaceExpr caller,
         Prefix_space_prefix_space_exprContext ctx) {
      if (ctx.prefix_space_intersection_prefix_space_expr() != null) {
         PrefixSpaceExpr arg = toPrefixSpaceExpr(ctx
               .prefix_space_intersection_prefix_space_expr().arg);
         return new IntersectionPrefixSpacePrefixSpaceExpr(caller, arg);
      }
      else {
         throw conversionError(ERR_CONVERT_PREFIX_SPACE, ctx);
      }
   }

   private Set<SubRange> toRange(Range_constraintContext ctx) {
      if (ctx.range() != null) {
         return toRange(ctx.range());
      }
      else if (ctx.subrange() != null) {
         return Collections.singleton(toSubRange(ctx.subrange()));
      }
      else if (ctx.VARIABLE() != null) {
         String var = ctx.VARIABLE().getText().substring(1);
         VariableType type = _parameters.getTypeBindings().get(var);
         switch (type) {
         case INT:
            int value = (int) _parameters.getInt(var);
            return Collections.singleton(new SubRange(value, value));

         case RANGE:
            return _parameters.getRange(var);

            // $CASES-OMITTED$
         default:
            throw new BatfishException("invalid constraint");
         }
      }
      else {
         throw new BatfishException("invalid range constraint: "
               + ctx.getText());
      }
   }

   private Set<SubRange> toRange(RangeContext ctx) {
      Set<SubRange> range = new LinkedHashSet<SubRange>();
      for (SubrangeContext sctx : ctx.range_list) {
         SubRange subRange = toSubRange(sctx);
         range.add(subRange);
      }
      return range;
   }

   private String toRegex(Node_constraintContext ctx) {
      if (ctx.REGEX() != null) {
         String regex = toRegex(ctx.REGEX().getText());
         return regex;
      }
      else if (ctx.string_literal_string_expr() != null) {
         return Pattern.quote(ctx.string_literal_string_expr().text);
      }
      else if (ctx.VARIABLE() != null) {
         String var = ctx.VARIABLE().getText().substring(1);
         VariableType type = _parameters.getTypeBindings().get(var);
         switch (type) {
         case REGEX:
            return toRegex(_parameters.getRegex(var));

         case STRING:
            String str = _parameters.getString(var);
            return Pattern.quote(str);

            // $CASES-OMITTED$
         default:
            throw new BatfishException("invalid constraint");
         }
      }
      else {
         throw new BatfishException("invalid node constraint: " + ctx.getText());
      }
   }

   private String toRegex(String text) {
      String prefix = "regex<";
      if (!text.startsWith(prefix) || !text.endsWith(">")) {
         throw new BatfishException("unexpected regex token text: " + text);
      }
      return text.substring(prefix.length(), text.length() - 1);
   }

   private RouteExpr toRouteExpr(Route_exprContext ctx) {
      routes();
      if (ctx.ROUTE() != null) {
         return BaseCaseRouteExpr.ROUTE;
      }
      else if (ctx.variable() != null) {
         return new VarRouteExpr(ctx.variable().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_ROUTE, ctx);
      }
   }

   private RouteFilterExpr toRouteFilterExpr(Route_filter_exprContext ctx) {
      if (ctx.route_filter_route_filter_expr() != null) {
         return BaseCaseRouteFilterExpr.ROUTE_FILTER;
      }
      else {
         throw conversionError(ERR_CONVERT_ROUTE_FILTER, ctx);
      }
   }

   private RouteFilterLineExpr toRouteFilterLineExpr(
         Route_filter_line_exprContext ctx) {
      if (ctx.route_filter_line_line_expr() != null) {
         return BaseCaseRouterFilterLineExpr.LINE;
      }
      else {
         throw conversionError(ERR_CONVERT_ROUTE_FILTER_LINE, ctx);
      }
   }

   private Statement toStatement(AssertionContext ctx) {
      BooleanExpr expr = toBooleanExpr(ctx.boolean_expr());
      List<Statement> onErrorStatements = toStatements(ctx.statements);
      Assertion assertion = new Assertion(expr, ctx.getText(),
            onErrorStatements);
      return assertion;
   }

   private Statement toStatement(AssignmentContext ctx) {
      String variable = ctx.VARIABLE().getText();
      if (ctx.bgp_neighbor_expr() != null) {
         BgpNeighborExpr expr = toBgpNeighborExpr(ctx.bgp_neighbor_expr());
         return new Assignment(variable, expr, VariableType.BGP_NEIGHBOR);
      }
      else if (ctx.boolean_expr() != null) {
         BooleanExpr expr = toBooleanExpr(ctx.boolean_expr());
         return new Assignment(variable, expr, VariableType.BOOLEAN);
      }
      else if (ctx.int_expr() != null) {
         IntExpr expr = toIntExpr(ctx.int_expr());
         return new Assignment(variable, expr, VariableType.INT);
      }
      else if (ctx.interface_expr() != null) {
         InterfaceExpr expr = toInterfaceExpr(ctx.interface_expr());
         return new Assignment(variable, expr, VariableType.INTERFACE);
      }
      else if (ctx.ip_expr() != null) {
         IpExpr expr = toIpExpr(ctx.ip_expr());
         return new Assignment(variable, expr, VariableType.IP);
      }
      else if (ctx.ipsec_vpn_expr() != null) {
         IpsecVpnExpr expr = toIpsecVpnExpr(ctx.ipsec_vpn_expr());
         return new Assignment(variable, expr, VariableType.IPSEC_VPN);
      }
      else if (ctx.map_expr() != null) {
         MapExpr expr = toMapExpr(ctx.map_expr());
         return new Assignment(variable, expr, VariableType.MAP);
      }
      else if (ctx.node_expr() != null) {
         NodeExpr expr = toNodeExpr(ctx.node_expr());
         return new Assignment(variable, expr, VariableType.NODE);
      }
      else if (ctx.policy_map_expr() != null) {
         PolicyMapExpr expr = toPolicyMapExpr(ctx.policy_map_expr());
         return new Assignment(variable, expr, VariableType.POLICY_MAP);
      }
      else if (ctx.policy_map_clause_expr() != null) {
         PolicyMapClauseExpr expr = toPolicyMapClauseExpr(ctx
               .policy_map_clause_expr());
         return new Assignment(variable, expr, VariableType.POLICY_MAP_CLAUSE);
      }
      else if (ctx.prefix_expr() != null) {
         PrefixExpr expr = toPrefixExpr(ctx.prefix_expr());
         return new Assignment(variable, expr, VariableType.PREFIX);
      }
      else if (ctx.prefix_space_expr() != null) {
         PrefixSpaceExpr expr = toPrefixSpaceExpr(ctx.prefix_space_expr());
         return new Assignment(variable, expr, VariableType.PREFIX_SPACE);
      }
      else if (ctx.route_filter_expr() != null) {
         RouteFilterExpr expr = toRouteFilterExpr(ctx.route_filter_expr());
         return new Assignment(variable, expr, VariableType.ROUTE_FILTER);
      }
      else if (ctx.route_filter_line_expr() != null) {
         RouteFilterLineExpr expr = toRouteFilterLineExpr(ctx
               .route_filter_line_expr());
         return new Assignment(variable, expr, VariableType.ROUTE_FILTER_LINE);
      }
      else if (ctx.set_prefix_expr() != null) {
         PrefixSetExpr expr = toPrefixSetExpr(ctx.set_prefix_expr());
         return new Assignment(variable, expr, VariableType.SET_PREFIX);
      }
      else if (ctx.set_string_expr() != null) {
         StringSetExpr expr = toStringSetExpr(ctx.set_string_expr());
         return new Assignment(variable, expr, VariableType.SET_STRING);
      }
      else if (ctx.static_route_expr() != null) {
         StaticRouteExpr expr = toStaticRouteExpr(ctx.static_route_expr());
         return new Assignment(variable, expr, VariableType.STATIC_ROUTE);
      }
      else if (ctx.string_expr() != null) {
         StringExpr expr = toStringExpr(ctx.string_expr());
         return new Assignment(variable, expr, VariableType.STRING);
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
   }

   private ForEachStatement<?> toStatement(Foreach_in_set_statementContext ctx) {
      String var = ctx.v.getText();
      String setVar = ctx.set_var.getText();
      VariableType elementType = ctx.setVarType.elementType();
      List<Statement> statements = toStatements(ctx.statement());
      switch (elementType) {

      case BGP_ADVERTISEMENT:
         return new ForEachBgpAdvertisementStatement(statements, var, setVar);

      case BGP_NEIGHBOR:
         return new ForEachBgpNeighborStatement(statements, var, setVar);

      case GENERATED_ROUTE:
         return new ForEachGeneratedRouteStatement(statements, var, setVar);

      case INT:
         return new ForEachIntegerStatement(statements, var, setVar);

      case INTERFACE:
         return new ForEachInterfaceStatement(statements, var, setVar);

      case IP:
         return new ForEachIpStatement(statements, var, setVar);

      case IPSEC_VPN:
         return new ForEachIpsecVpnStatement(statements, var, setVar);

      case MAP:
         return new ForEachMapStatement(statements, var, setVar);

      case NODE:
         return new ForEachNodeStatement(statements, var, setVar);

      case POLICY_MAP:
         return new ForEachPolicyMapStatement(statements, var, setVar);

      case POLICY_MAP_CLAUSE:
         return new ForEachPolicyMapClauseStatement(statements, var, setVar);

      case PREFIX:
         return new ForEachPrefixStatement(statements, var, setVar);

      case PREFIX_SPACE:
         return new ForEachPrefixSpaceStatement(statements, var, setVar);

      case ROUTE:
         return new ForEachRouteStatement(statements, var, setVar);

      case ROUTE_FILTER:
         return new ForEachRouteFilterStatement(statements, var, setVar);

      case ROUTE_FILTER_LINE:
         return new ForEachRouteFilterLineStatement(statements, var, setVar);

      case STATIC_ROUTE:
         return new ForEachStaticRouteStatement(statements, var, setVar);

      case STRING:
         return new ForEachStringStatement(statements, var, setVar);

      case ACTION:
      case BOOLEAN:
      case NEIGHBOR_TYPE:
      case NODE_TYPE:
      case PROTOCOL:
      case RANGE:
      case REGEX:
      case SET_BGP_ADVERTISEMENT:
      case SET_BGP_NEIGHBOR:
      case SET_INT:
      case SET_INTERFACE:
      case SET_IP:
      case SET_IPSEC_VPN:
      case SET_NODE:
      case SET_POLICY_MAP:
      case SET_POLICY_MAP_CLAUSE:
      case SET_PREFIX:
      case SET_PREFIX_SPACE:
      case SET_ROUTE:
      case SET_ROUTE_FILTER:
      case SET_ROUTE_FILTER_LINE:
      case SET_STATIC_ROUTE:
      case SET_STRING:
      default:
         throw new BatfishException("element type not implemented: "
               + elementType);
      }
   }

   private ForEachStatement<?> toStatement(Foreach_scoped_statementContext ctx) {
      List<Statement> statements = toStatements(ctx.statement());
      String var = null;
      String setVar = null;
      if (ctx.var != null) {
         var = ctx.var.getText();
      }
      if (ctx.BGP_NEIGHBOR() != null) {
         return new ForEachBgpNeighborStatement(statements, var, setVar);
      }
      else if (ctx.CLAUSE() != null) {
         return new ForEachClauseStatement(statements, var, setVar);
      }
      else if (ctx.GENERATED_ROUTE() != null) {
         return new ForEachGeneratedRouteStatement(statements, var, setVar);
      }
      else if (ctx.INTERFACE() != null) {
         return new ForEachInterfaceStatement(statements, var, setVar);
      }
      else if (ctx.IPSEC_VPN() != null) {
         return new ForEachIpsecVpnStatement(statements, var, setVar);
      }
      else if (ctx.LINE() != null) {
         return new ForEachLineStatement(statements, var, setVar);
      }
      else if (ctx.MATCH_PROTOCOL() != null) {
         return new ForEachMatchProtocolStatement(statements, var, setVar);
      }
      else if (ctx.MATCH_ROUTE_FILTER() != null) {
         return new ForEachMatchRouteFilterStatement(statements, var, setVar);
      }
      else if (ctx.NODE() != null) {
         return new ForEachNodeStatement(statements, var, setVar);
      }
      else if (ctx.NODE_BGP_GENERATED_ROUTE() != null) {
         return new ForEachNodeBgpGeneratedRouteStatement(statements, var,
               setVar);
      }
      else if (ctx.OSPF_OUTBOUND_POLICY() != null) {
         return new ForEachOspfOutboundPolicyStatement(statements, var, setVar);
      }
      else if (ctx.PROTOCOL() != null) {
         return new ForEachProtocolStatement(statements, var, setVar);
      }
      else if (ctx.RECEIVED_EBGP_ADVERTISEMENT() != null) {
         return new ForEachReceivedEbgpAdvertisementStatement(statements, var,
               setVar);
      }
      else if (ctx.RECEIVED_IBGP_ADVERTISEMENT() != null) {
         return new ForEachReceivedIbgpAdvertisementStatement(statements, var,
               setVar);
      }
      else if (ctx.REMOTE_BGP_NEIGHBOR() != null) {
         return new ForEachRemoteBgpNeighborStatement(statements, var, setVar);
      }
      else if (ctx.REMOTE_IPSEC_VPN() != null) {
         return new ForEachRemoteIpsecVpnStatement(statements, var, setVar);
      }
      else if (ctx.ROUTE() != null) {
         routes();
         return new ForEachRouteStatement(statements, var, setVar);
      }
      else if (ctx.ROUTE_FILTER() != null) {
         return new ForEachRouteFilterStatement(statements, var, setVar);
      }
      else if (ctx.SENT_EBGP_ADVERTISEMENT() != null) {
         bgp_advertisements();
         return new ForEachSentEbgpAdvertisementStatement(statements, var,
               setVar);
      }
      else if (ctx.SENT_IBGP_ADVERTISEMENT() != null) {
         return new ForEachSentIbgpAdvertisementStatement(statements, var,
               setVar);
      }
      else if (ctx.STATIC_ROUTE() != null) {
         return new ForEachStaticRouteStatement(statements, var, setVar);
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
   }

   private Statement toStatement(If_statementContext ctx) {
      BooleanExpr guard = toBooleanExpr(ctx.guard);
      List<Statement> trueStatements = toStatements(ctx.true_statements);
      List<Statement> falseStatements = toStatements(ctx.false_statements);
      return new IfStatement(guard, trueStatements, falseStatements);
   }

   private Statement toStatement(Increment_statementContext ctx) {
      String var = ctx.var.getText();
      return new IncrementStatement(var);
   }

   private Statement toStatement(Map_set_methodContext ctx) {
      MapExpr caller = toMapExpr(ctx.caller);
      Expr key = toExpr(ctx.key);
      Expr value = toExpr(ctx.value);
      return new MapSetMethod(caller, key, value);
   }

   private Statement toStatement(MethodContext ctx) {
      if (ctx.typed_method() != null) {
         return toStatement(ctx.typed_method());
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
   }

   private Statement toStatement(Printf_statementContext ctx) {
      StringExpr formatString = toStringExpr(ctx.format_string);
      List<Expr> replacements = new ArrayList<Expr>();
      for (Printable_exprContext pexpr : ctx.replacements) {
         Expr replacement = toExpr(pexpr);
         replacements.add(replacement);
      }
      return new PrintfStatement(formatString, replacements);
   }

   private Statement toStatement(Set_add_methodContext ctx) {
      Expr expr;
      if (ctx.bgp_neighbor_expr() != null) {
         expr = toBgpNeighborExpr(ctx.bgp_neighbor_expr());
      }
      else if (ctx.interface_expr() != null) {
         expr = toInterfaceExpr(ctx.interface_expr());
      }
      else if (ctx.ip_expr() != null) {
         expr = toIpExpr(ctx.ip_expr());
      }
      else if (ctx.ipsec_vpn_expr() != null) {
         expr = toIpsecVpnExpr(ctx.ipsec_vpn_expr());
      }
      else if (ctx.node_expr() != null) {
         expr = toNodeExpr(ctx.node_expr());
      }
      else if (ctx.prefix_expr() != null) {
         expr = toPrefixExpr(ctx.prefix_expr());
      }
      else if (ctx.prefix_space_expr() != null) {
         expr = toPrefixSpaceExpr(ctx.prefix_space_expr());
      }
      else if (ctx.policy_map_expr() != null) {
         expr = toPolicyMapExpr(ctx.policy_map_expr());
      }
      else if (ctx.policy_map_clause_expr() != null) {
         expr = toPolicyMapClauseExpr(ctx.policy_map_clause_expr());
      }
      else if (ctx.route_filter_expr() != null) {
         expr = toRouteFilterExpr(ctx.route_filter_expr());
      }
      else if (ctx.route_filter_line_expr() != null) {
         expr = toRouteFilterLineExpr(ctx.route_filter_line_expr());
      }
      else if (ctx.static_route_expr() != null) {
         expr = toStaticRouteExpr(ctx.static_route_expr());
      }
      else if (ctx.string_expr() != null) {
         expr = toStringExpr(ctx.string_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
      return new SetAddStatement(expr, ctx.caller, ctx.type);
   }

   private Statement toStatement(Set_clear_methodContext ctx) {
      return new SetClearStatement(ctx.caller, ctx.type);
   }

   private Statement toStatement(Set_declaration_statementContext ctx) {
      String var = ctx.var.getText();
      VariableType type = VariableType.fromString(ctx.typeStr);
      return new SetDeclarationStatement(var, type);
   }

   private Statement toStatement(StatementContext ctx) {
      if (ctx.assertion() != null) {
         return toStatement(ctx.assertion());
      }
      else if (ctx.assignment() != null) {
         return toStatement(ctx.assignment());
      }
      else if (ctx.foreach_in_set_statement() != null) {
         return toStatement(ctx.foreach_in_set_statement());
      }
      else if (ctx.foreach_scoped_statement() != null) {
         return toStatement(ctx.foreach_scoped_statement());
      }
      else if (ctx.increment_statement() != null) {
         return toStatement(ctx.increment_statement());
      }
      else if (ctx.if_statement() != null) {
         return toStatement(ctx.if_statement());
      }
      else if (ctx.map_set_method() != null) {
         return toStatement(ctx.map_set_method());
      }
      else if (ctx.method() != null) {
         return toStatement(ctx.method());
      }
      else if (ctx.printf_statement() != null) {
         return toStatement(ctx.printf_statement());
      }
      else if (ctx.set_declaration_statement() != null) {
         return toStatement(ctx.set_declaration_statement());
      }
      else if (ctx.unless_statement() != null) {
         return toStatement(ctx.unless_statement());
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
   }

   private Statement toStatement(Typed_methodContext ctx) {
      if (ctx.set_add_method() != null) {
         return toStatement(ctx.set_add_method());
      }
      else if (ctx.set_clear_method() != null) {
         return toStatement(ctx.set_clear_method());
      }
      else {
         throw conversionError(ERR_CONVERT_STATEMENT, ctx);
      }
   }

   private Statement toStatement(Unless_statementContext ctx) {
      BooleanExpr guard = toBooleanExpr(ctx.guard);
      List<Statement> statements = toStatements(ctx.statements);
      return new UnlessStatement(guard, statements);
   }

   private List<Statement> toStatements(List<StatementContext> sctxList) {
      List<Statement> statements = new ArrayList<Statement>();
      for (StatementContext sctx : sctxList) {
         Statement statement = toStatement(sctx);
         statements.add(statement);
      }
      return statements;
   }

   private StaticRouteExpr toStaticRouteExpr(Static_route_exprContext ctx) {
      if (ctx.STATIC_ROUTE() != null) {
         return BaseCaseStaticRouteExpr.STATIC_ROUTE;
      }
      else if (ctx.variable() != null) {
         return new VarStaticRouteExpr(ctx.variable().VARIABLE().getText());
      }
      else {
         throw conversionError(ERR_CONVERT_STATIC_ROUTE, ctx);
      }
   }

   private StringExpr toStringExpr(Base_string_exprContext ctx) {
      if (ctx.bgp_advertisement_string_expr() != null) {
         return toStringExpr(ctx.bgp_advertisement_string_expr());
      }
      else if (ctx.bgp_neighbor_string_expr() != null) {
         return toStringExpr(ctx.bgp_neighbor_string_expr());
      }
      else if (ctx.format_string_expr() != null) {
         return toStringExpr(ctx.format_string_expr());
      }
      else if (ctx.interface_string_expr() != null) {
         return toStringExpr(ctx.interface_string_expr());
      }
      else if (ctx.ipsec_vpn_string_expr() != null) {
         return toStringExpr(ctx.ipsec_vpn_string_expr());
      }
      else if (ctx.map_string_expr() != null) {
         return toStringExpr(ctx.map_string_expr());
      }
      else if (ctx.node_string_expr() != null) {
         return toStringExpr(ctx.node_string_expr());
      }
      else if (ctx.protocol_string_expr() != null) {
         return toStringExpr(ctx.protocol_string_expr());
      }
      else if (ctx.route_string_expr() != null) {
         return toStringExpr(ctx.route_string_expr());
      }
      else if (ctx.static_route_string_expr() != null) {
         return toStringExpr(ctx.static_route_string_expr());
      }
      else if (ctx.route_filter_string_expr() != null) {
         return toStringExpr(ctx.route_filter_string_expr());
      }
      else if (ctx.variable() != null) {
         return toStringExpr(ctx.variable());
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Bgp_advertisement_string_exprContext ctx) {
      BgpAdvertisementExpr caller = toBgpAdvertisementExpr(ctx.caller);
      if (ctx.bgp_advertisement_as_path_string_expr() != null) {
         return new AsPathBgpAdvertisementStringExpr(caller);
      }
      else if (ctx.bgp_advertisement_dst_node_string_expr() != null) {
         return new DstNodeBgpAdvertisementStringExpr(caller);
      }
      else if (ctx.bgp_advertisement_src_node_string_expr() != null) {
         return new SrcNodeBgpAdvertisementStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private BgpNeighborStringExpr toStringExpr(
         Bgp_neighbor_string_exprContext ctx) {
      BgpNeighborExpr caller = toBgpNeighborExpr(ctx.caller);
      if (ctx.bgp_neighbor_description_string_expr() != null) {
         return new DescriptionBgpNeighborStringExpr(caller);
      }
      else if (ctx.bgp_neighbor_group_string_expr() != null) {
         return new GroupBgpNeighborStringExpr(caller);
      }
      else if (ctx.bgp_neighbor_name_string_expr() != null) {
         return new NameBgpNeighborStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Format_string_exprContext ctx) {
      StringExpr formatString = toStringExpr(ctx.format_string);
      List<Expr> replacements = new ArrayList<Expr>();
      for (Printable_exprContext pexpr : ctx.replacements) {
         Expr replacement = toExpr(pexpr);
         replacements.add(replacement);
      }
      return new FormatStringExpr(formatString, replacements);
   }

   private StringExpr toStringExpr(Interface_string_exprContext ctx) {
      InterfaceExpr caller = toInterfaceExpr(ctx.caller);
      if (ctx.interface_description_string_expr() != null) {
         return new NameInterfaceStringExpr(caller);
      }
      else if (ctx.interface_name_string_expr() != null) {
         return new DescriptionInterfaceStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Ipsec_vpn_string_exprContext ctx) {
      IpsecVpnExpr caller = toIpsecVpnExpr(ctx.ipsec_vpn_expr());
      if (ctx.ipsec_vpn_ike_gateway_name_string_expr() != null) {
         return new IkeGatewayNameIpsecVpnStringExpr(caller);
      }
      else if (ctx.ipsec_vpn_ike_policy_name_string_expr() != null) {
         return new IkePolicyNameIpsecVpnStringExpr(caller);
      }
      else if (ctx.ipsec_vpn_ipsec_policy_name_string_expr() != null) {
         return new IpsecPolicyNameIpsecVpnStringExpr(caller);
      }
      else if (ctx.ipsec_vpn_name_string_expr() != null) {
         return new NameIpsecVpnStringExpr(caller);
      }
      else if (ctx.ipsec_vpn_pre_shared_key_hash_string_expr() != null) {
         return new PreSharedKeyHashIpsecVpnStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Map_string_exprContext ctx) {
      MapExpr caller = toMapExpr(ctx.caller);
      if (ctx.map_get_string_expr() != null) {
         return toStringExpr(caller, ctx.map_get_string_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(MapExpr caller,
         Map_get_string_exprContext ctx) {
      Expr key = toExpr(ctx.key);
      return new GetMapStringExpr(caller, key);
   }

   private StringExpr toStringExpr(Node_string_exprContext ctx) {
      NodeExpr caller = toNodeExpr(ctx.node_expr());
      if (ctx.node_name_string_expr() != null) {
         return new NameNodeStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Protocol_string_exprContext ctx) {
      if (ctx.protocol_name_string_expr() != null) {
         return ProtocolStringExpr.PROTOCOL_NAME;
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Route_filter_string_exprContext ctx) {
      if (ctx.route_filter_name_string_expr() != null) {
         return RouteFilterStringExpr.NAME;
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Route_string_exprContext ctx) {
      RouteExpr caller = toRouteExpr(ctx.caller);
      if (ctx.route_next_hop_interface_string_expr() != null) {
         return new NextHopInterfaceRouteStringExpr(caller);
      }
      else if (ctx.route_next_hop_string_expr() != null) {
         return new NextHopRouteStringExpr(caller);
      }
      else if (ctx.route_protocol_string_expr() != null) {
         return new ProtocolRouteStringExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(Static_route_string_exprContext ctx) {
      if (ctx.static_route_next_hop_interface_string_expr() != null) {
         return StaticRouteStringExpr.STATICROUTE_NEXT_HOP_INTERFACE;
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(String_exprContext ctx) {
      if (ctx.base_string_expr() != null) {
         return toStringExpr(ctx.base_string_expr());
      }
      else if (ctx.string_literal_string_expr() != null) {
         return toStringExpr(ctx.string_literal_string_expr());
      }
      else if (ctx.PLUS() != null) {
         StringExpr s1 = toStringExpr(ctx.s1);
         StringExpr s2 = toStringExpr(ctx.s2);
         return new StringConcatenateExpr(s1, s2);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING, ctx);
      }
   }

   private StringExpr toStringExpr(String_literal_string_exprContext ctx) {
      String withEscapes = ctx.text;
      String processed = Util.unescapeJavaString(withEscapes);
      return new StringLiteralStringExpr(processed);
   }

   private StringExpr toStringExpr(VariableContext ctx) {
      String var = ctx.VARIABLE().getText();
      return new VarStringExpr(var);
   }

   private StringSetExpr toStringSetExpr(
         Bgp_advertisement_set_string_exprContext ctx) {
      BgpAdvertisementExpr caller = toBgpAdvertisementExpr(ctx.caller);
      if (ctx.bgp_advertisement_communities_set_string_expr() != null) {
         return new CommunitiesBgpAdvertisementStringSetExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING_SET, ctx);
      }
   }

   private StringSetExpr toStringSetExpr(Map_set_string_exprContext ctx) {
      MapExpr caller = toMapExpr(ctx.caller);
      if (ctx.map_keys_set_string_expr() != null) {
         return new KeysMapStringSetExpr(caller);
      }
      else {
         throw conversionError(ERR_CONVERT_STRING_SET, ctx);
      }
   }

   private StringSetExpr toStringSetExpr(Set_string_exprContext ctx) {
      if (ctx.bgp_advertisement_set_string_expr() != null) {
         return toStringSetExpr(ctx.bgp_advertisement_set_string_expr());
      }
      else if (ctx.map_set_string_expr() != null) {
         return toStringSetExpr(ctx.map_set_string_expr());
      }
      else {
         throw conversionError(ERR_CONVERT_STRING_SET, ctx);
      }
   }

   private SubRange toSubRange(SubrangeContext ctx) {
      int low = Integer.parseInt(ctx.low.getText());
      if (ctx.high != null) {
         int high = Integer.parseInt(ctx.high.getText());
         return new SubRange(low, high);
      }
      else {
         return new SubRange(low, low);
      }
   }

}
