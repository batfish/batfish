package batfish.grammar.flatjuniper;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ParseTreePrettyPrinter;
import batfish.grammar.cisco.CiscoGrammar;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;
import batfish.representation.juniper.JuniperVendorConfiguration;

public class ConfigurationBuilder extends FlatJuniperGrammarParserBaseListener {

   private JuniperVendorConfiguration _configuration;
   private JuniperVendorConfiguration _masterConfiguration;
   private BatfishCombinedParser<?, ?> _parser;
   private Set<String> _rulesWithSuppressedWarnings;
   private boolean _set;
   private String _text;
   private List<String> _warnings;

   public ConfigurationBuilder(BatfishCombinedParser<?, ?> parser, String text,
         Set<String> rulesWithSuppressedWarnings, List<String> warnings) {
      _parser = parser;
      _text = text;
      _rulesWithSuppressedWarnings = rulesWithSuppressedWarnings;
      _warnings = warnings;
      _masterConfiguration = new JuniperVendorConfiguration();
      _configuration = _masterConfiguration;
   }

   @Override
   public void enterBfi6t_unicast(Bfi6t_unicastContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfi6t_unicast_tail(Bfi6t_unicast_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfi6ut_prefix_limit(Bfi6ut_prefix_limitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfit_flow(Bfit_flowContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfit_unicast(Bfit_unicastContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfit_unicast_tail(Bfit_unicast_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfiut_prefix_limit(Bfiut_prefix_limitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBft_inet(Bft_inetContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBft_inet_tail(Bft_inet_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBft_inet6(Bft_inet6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBft_inet6_tail(Bft_inet6_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_advertise_inactive(Bt_advertise_inactiveContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_cluster(Bt_clusterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_common(Bt_commonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_description(Bt_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_export(Bt_exportContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_family(Bt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_family_tail(Bt_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_group(Bt_groupContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_group_header(Bt_group_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_group_tail(Bt_group_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_import(Bt_importContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_local_address(Bt_local_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_local_as(Bt_local_asContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_multipath(Bt_multipathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_neighbor(Bt_neighborContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_neighbor_header(Bt_neighbor_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_neighbor_tail(Bt_neighbor_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_null(Bt_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_path_selection(Bt_path_selectionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_path_selection_tail(Bt_path_selection_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_peer_as(Bt_peer_asContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBt_type(Bt_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterColort_apply_groups(Colort_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterColort_color(Colort_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCt_members(Ct_membersContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDeactivate_line(Deactivate_lineContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDirection(DirectionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEveryRule(ParserRuleContext arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily(FamilyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamt_inet(Famt_inetContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamt_inet_tail(Famt_inet_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamt_inet6(Famt_inet6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamt_mpls(Famt_mplsContext ctx) {
      todo(ctx);
   }

   @Override
   public void enterFamt_mpls_tail(Famt_mpls_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter(FilterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter_header(Filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter_tail(Filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_as_path(Fromt_as_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_color(Fromt_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_community(Fromt_communityContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_family(Fromt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_interface(Fromt_interfaceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_policy(Fromt_policyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_prefix_list(Fromt_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_protocol(Fromt_protocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_route_filter(Fromt_route_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_route_filter_header(
         Fromt_route_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_route_filter_tail(Fromt_route_filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_source_address_filter(
         Fromt_source_address_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_source_address_filter_header(
         Fromt_source_address_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFromt_tag(Fromt_tagContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_address(Fwfromt_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_destination_address(
         Fwfromt_destination_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_destination_port(Fwfromt_destination_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_dscp(Fwfromt_dscpContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_exp(Fwfromt_expContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_icmp_code(Fwfromt_icmp_codeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_icmp_type(Fwfromt_icmp_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_next_header(Fwfromt_next_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_port(Fwfromt_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_prefix_list(Fwfromt_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_protocol(Fwfromt_protocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_source_address(Fwfromt_source_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_source_port(Fwfromt_source_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_source_prefix_list(
         Fwfromt_source_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_tcp_established(Fwfromt_tcp_establishedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwfromt_tcp_flags(Fwfromt_tcp_flagsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwft_interface_specific(Fwft_interface_specificContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwft_term(Fwft_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwft_term_header(Fwft_term_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwft_term_tail(Fwft_term_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_family(Fwt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_family_header(Fwt_family_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_family_tail(Fwt_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_filter(Fwt_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_filter_header(Fwt_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_filter_tail(Fwt_filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwt_null(Fwt_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwthent_accept(Fwthent_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwthent_discard(Fwthent_discardContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwthent_null(Fwthent_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwthent_reject(Fwthent_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwtt_from(Fwtt_fromContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwtt_from_tail(Fwtt_from_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwtt_then(Fwtt_thenContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFwtt_then_tail(Fwtt_then_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIcmp_code(Icmp_codeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIcmp_type(Icmp_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIfamt_address(Ifamt_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIfamt_filter(Ifamt_filterContext ctx) {
      assert Boolean.TRUE;

   }

   @Override
   public void enterIfamt_no_redirects(Ifamt_no_redirectsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_apply_groups(It_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_description(It_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_disable(It_disableContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_mtu(It_mtuContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_null(It_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_unit(It_unitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_unit_header(It_unit_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_unit_tail(It_unit_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIt_vlan_tagging(It_vlan_taggingContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMet_metric(Met_metricContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMet_metric2(Met_metric2Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetrict_constant(Metrict_constantContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetrict_expression(Metrict_expressionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetrict_expression_tail(Metrict_expression_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMfamt_filter(Mfamt_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMfamt_maximum_labels(Mfamt_maximum_labelsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPe_conjunction(Pe_conjunctionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPe_disjunction(Pe_disjunctionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPe_nested(Pe_nestedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPlt_apply_path(Plt_apply_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPlt_network(Plt_networkContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPlt_network6(Plt_network6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPolicy_expression(Policy_expressionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPort(PortContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_as_path(Pot_as_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_as_path_header(Pot_as_path_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_as_path_tail(Pot_as_path_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_community(Pot_communityContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_community_header(Pot_community_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_community_tail(Pot_community_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_policy_statement(Pot_policy_statementContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_policy_statement_header(
         Pot_policy_statement_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_policy_statement_tail(
         Pot_policy_statement_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_prefix_list(Pot_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_prefix_list_header(Pot_prefix_list_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPot_prefix_list_tail(Pot_prefix_list_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPrefix_length_range(Prefix_length_rangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocol(ProtocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPst_always_compare_med(Pst_always_compare_medContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPst_term(Pst_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPst_term_header(Pst_term_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPst_term_tail(Pst_term_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRange(RangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRft_exact(Rft_exactContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRft_orlonger(Rft_orlongerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRft_prefix_length_range(Rft_prefix_length_rangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRft_upto(Rft_uptoContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRgt_import_rib(Rgt_import_ribContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRibt_static(Ribt_staticContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRit_apply_groups(Rit_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRit_common(Rit_commonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRit_named_routing_instance(
         Rit_named_routing_instanceContext ctx) {
      if (_set) {
         String name;
         if (ctx.name != null) {
            name = ctx.name.getText();
         }
         else {
            name = ctx.WILDCARD().getText();
         }
         _configuration = _masterConfiguration.getRoutingInstances().get(name);
         if (_configuration == null) {
            _configuration = new JuniperVendorConfiguration();
            _masterConfiguration.getRoutingInstances()
                  .put(name, _configuration);
         }
      }
   }

   @Override
   public void enterRit_named_routing_instance_tail(
         Rit_named_routing_instance_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRit_routing_options(Rit_routing_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_aggregate(Rot_aggregateContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_autonomous_system(Rot_autonomous_systemContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_martians(Rot_martiansContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_null(Rot_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib(Rot_ribContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib_groups(Rot_rib_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib_groups_header(Rot_rib_groups_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib_groups_tail(Rot_rib_groups_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib_header(Rot_rib_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_rib_tail(Rot_rib_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_router_id(Rot_router_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRot_static(Rot_staticContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_apply_groups(S_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_description(S_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_firewall(S_firewallContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_firewall_tail(S_firewall_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_groups(S_groupsContext ctx) {
      assert Boolean.TRUE;

   }

   @Override
   public void enterS_groups_named(S_groups_namedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_groups_tail(S_groups_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_interfaces(S_interfacesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_interfaces_header(S_interfaces_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_interfaces_tail(S_interfaces_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_null(S_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_policy_options(S_policy_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_policy_options_tail(S_policy_options_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols(S_protocolsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_bgp(S_protocols_bgpContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_bgp_tail(S_protocols_bgp_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_isis(S_protocols_isisContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_mpls(S_protocols_mplsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_null(S_protocols_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_ospf(S_protocols_ospfContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_protocols_tail(S_protocols_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_routing_instances(S_routing_instancesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_routing_instances_header(
         S_routing_instances_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_routing_instances_tail(S_routing_instances_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_routing_options(S_routing_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_routing_options_tail(S_routing_options_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_system(S_systemContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_system_tail(S_system_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterS_version(S_versionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSet_line(Set_lineContext ctx) {
      _set = true;
   }

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSt_null(St_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterStatement(StatementContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSubrange(SubrangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_accept(Tht_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_as_path_prepend(Tht_as_path_prependContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_color(Tht_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_color_tail(Tht_color_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_community_add(Tht_community_addContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_community_delete(Tht_community_deleteContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_community_set(Tht_community_setContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_cos_next_hop_map(Tht_cos_next_hop_mapContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_default_action_accept(
         Tht_default_action_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_default_action_reject(
         Tht_default_action_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_local_preference(Tht_local_preferenceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_metric(Tht_metricContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_metric_tail(Tht_metric_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_next_hop(Tht_next_hopContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_next_policy(Tht_next_policyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_next_term(Tht_next_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_null(Tht_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_origin(Tht_originContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_reject(Tht_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTht_tag(Tht_tagContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTt_apply_groups(Tt_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTt_from(Tt_fromContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTt_from_tail(Tt_from_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTt_then(Tt_thenContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTt_then_tail(Tt_then_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUt_description(Ut_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUt_family(Ut_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUt_family_tail(Ut_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUt_null(Ut_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUt_vlan_id(Ut_vlan_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVariable(VariableContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfi6t_unicast(Bfi6t_unicastContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfi6t_unicast_tail(Bfi6t_unicast_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfi6ut_prefix_limit(Bfi6ut_prefix_limitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfit_flow(Bfit_flowContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfit_unicast(Bfit_unicastContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfit_unicast_tail(Bfit_unicast_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfiut_prefix_limit(Bfiut_prefix_limitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBft_inet(Bft_inetContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBft_inet_tail(Bft_inet_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBft_inet6(Bft_inet6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBft_inet6_tail(Bft_inet6_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_advertise_inactive(Bt_advertise_inactiveContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_cluster(Bt_clusterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_common(Bt_commonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_description(Bt_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_export(Bt_exportContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_family(Bt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_family_tail(Bt_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_group(Bt_groupContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_group_header(Bt_group_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_group_tail(Bt_group_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_import(Bt_importContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_local_address(Bt_local_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_local_as(Bt_local_asContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_multipath(Bt_multipathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_neighbor(Bt_neighborContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_neighbor_header(Bt_neighbor_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_neighbor_tail(Bt_neighbor_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_null(Bt_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_path_selection(Bt_path_selectionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_path_selection_tail(Bt_path_selection_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_peer_as(Bt_peer_asContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBt_type(Bt_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitColort_apply_groups(Colort_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitColort_color(Colort_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCt_members(Ct_membersContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDeactivate_line(Deactivate_lineContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDirection(DirectionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEveryRule(ParserRuleContext arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily(FamilyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamt_inet(Famt_inetContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamt_inet_tail(Famt_inet_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamt_inet6(Famt_inet6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamt_mpls(Famt_mplsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamt_mpls_tail(Famt_mpls_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter(FilterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter_header(Filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter_tail(Filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_as_path(Fromt_as_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_color(Fromt_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_community(Fromt_communityContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_family(Fromt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_interface(Fromt_interfaceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_policy(Fromt_policyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_prefix_list(Fromt_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_protocol(Fromt_protocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_route_filter(Fromt_route_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_route_filter_header(
         Fromt_route_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_route_filter_tail(Fromt_route_filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_source_address_filter(
         Fromt_source_address_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_source_address_filter_header(
         Fromt_source_address_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFromt_tag(Fromt_tagContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_address(Fwfromt_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_destination_address(
         Fwfromt_destination_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_destination_port(Fwfromt_destination_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_dscp(Fwfromt_dscpContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_exp(Fwfromt_expContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_icmp_code(Fwfromt_icmp_codeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_icmp_type(Fwfromt_icmp_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_next_header(Fwfromt_next_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_port(Fwfromt_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_prefix_list(Fwfromt_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_protocol(Fwfromt_protocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_source_address(Fwfromt_source_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_source_port(Fwfromt_source_portContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_source_prefix_list(
         Fwfromt_source_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_tcp_established(Fwfromt_tcp_establishedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwfromt_tcp_flags(Fwfromt_tcp_flagsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwft_interface_specific(Fwft_interface_specificContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwft_term(Fwft_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwft_term_header(Fwft_term_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwft_term_tail(Fwft_term_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_family(Fwt_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_family_header(Fwt_family_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_family_tail(Fwt_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_filter(Fwt_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_filter_header(Fwt_filter_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_filter_tail(Fwt_filter_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwt_null(Fwt_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwthent_accept(Fwthent_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwthent_discard(Fwthent_discardContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwthent_null(Fwthent_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwthent_reject(Fwthent_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwtt_from(Fwtt_fromContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwtt_from_tail(Fwtt_from_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwtt_then(Fwtt_thenContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFwtt_then_tail(Fwtt_then_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIcmp_code(Icmp_codeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIcmp_type(Icmp_typeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIfamt_address(Ifamt_addressContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIfamt_filter(Ifamt_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIfamt_no_redirects(Ifamt_no_redirectsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_apply_groups(It_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_description(It_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_disable(It_disableContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_mtu(It_mtuContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_null(It_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_unit(It_unitContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_unit_header(It_unit_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_unit_tail(It_unit_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIt_vlan_tagging(It_vlan_taggingContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMet_metric(Met_metricContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMet_metric2(Met_metric2Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetrict_constant(Metrict_constantContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetrict_expression(Metrict_expressionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetrict_expression_tail(Metrict_expression_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMfamt_filter(Mfamt_filterContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMfamt_maximum_labels(Mfamt_maximum_labelsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPe_conjunction(Pe_conjunctionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPe_disjunction(Pe_disjunctionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPe_nested(Pe_nestedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPlt_apply_path(Plt_apply_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPlt_network(Plt_networkContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPlt_network6(Plt_network6Context ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPolicy_expression(Policy_expressionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPort(PortContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_as_path(Pot_as_pathContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_as_path_header(Pot_as_path_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_as_path_tail(Pot_as_path_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_community(Pot_communityContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_community_header(Pot_community_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_community_tail(Pot_community_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_policy_statement(Pot_policy_statementContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_policy_statement_header(
         Pot_policy_statement_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_policy_statement_tail(
         Pot_policy_statement_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_prefix_list(Pot_prefix_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_prefix_list_header(Pot_prefix_list_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPot_prefix_list_tail(Pot_prefix_list_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPrefix_length_range(Prefix_length_rangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol(ProtocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPst_always_compare_med(Pst_always_compare_medContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPst_term(Pst_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPst_term_header(Pst_term_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPst_term_tail(Pst_term_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRange(RangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRft_exact(Rft_exactContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRft_orlonger(Rft_orlongerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRft_prefix_length_range(Rft_prefix_length_rangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRft_upto(Rft_uptoContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRgt_import_rib(Rgt_import_ribContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRibt_static(Ribt_staticContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRit_apply_groups(Rit_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRit_common(Rit_commonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRit_named_routing_instance(
         Rit_named_routing_instanceContext ctx) {
      _configuration = _masterConfiguration;
   }

   @Override
   public void exitRit_named_routing_instance_tail(
         Rit_named_routing_instance_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRit_routing_options(Rit_routing_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_aggregate(Rot_aggregateContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_autonomous_system(Rot_autonomous_systemContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_martians(Rot_martiansContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_null(Rot_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib(Rot_ribContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib_groups(Rot_rib_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib_groups_header(Rot_rib_groups_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib_groups_tail(Rot_rib_groups_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib_header(Rot_rib_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_rib_tail(Rot_rib_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_router_id(Rot_router_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRot_static(Rot_staticContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_apply_groups(S_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_description(S_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_firewall(S_firewallContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_firewall_tail(S_firewall_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_groups(S_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_groups_named(S_groups_namedContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_groups_tail(S_groups_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_interfaces(S_interfacesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_interfaces_header(S_interfaces_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_interfaces_tail(S_interfaces_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_null(S_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_policy_options(S_policy_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_policy_options_tail(S_policy_options_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols(S_protocolsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_bgp(S_protocols_bgpContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_bgp_tail(S_protocols_bgp_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_isis(S_protocols_isisContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_mpls(S_protocols_mplsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_null(S_protocols_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_ospf(S_protocols_ospfContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_protocols_tail(S_protocols_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_routing_instances(S_routing_instancesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_routing_instances_header(
         S_routing_instances_headerContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_routing_instances_tail(S_routing_instances_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_routing_options(S_routing_optionsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_routing_options_tail(S_routing_options_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_system(S_systemContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_system_tail(S_system_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitS_version(S_versionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      _set = false;
   }

   @Override
   public void exitSet_line_tail(Set_line_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSt_host_name(St_host_nameContext ctx) {
      if (_set) {
         String hostname = ctx.variable().getText();
         _configuration.setHostname(hostname);
      }
   }

   @Override
   public void exitSt_null(St_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitStatement(StatementContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSubrange(SubrangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_accept(Tht_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_as_path_prepend(Tht_as_path_prependContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_color(Tht_colorContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_color_tail(Tht_color_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_community_add(Tht_community_addContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_community_delete(Tht_community_deleteContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_community_set(Tht_community_setContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_cos_next_hop_map(Tht_cos_next_hop_mapContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_default_action_accept(
         Tht_default_action_acceptContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_default_action_reject(
         Tht_default_action_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_local_preference(Tht_local_preferenceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_metric(Tht_metricContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_metric_tail(Tht_metric_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_next_hop(Tht_next_hopContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_next_policy(Tht_next_policyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_next_term(Tht_next_termContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_null(Tht_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_origin(Tht_originContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_reject(Tht_rejectContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTht_tag(Tht_tagContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTt_apply_groups(Tt_apply_groupsContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTt_from(Tt_fromContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTt_from_tail(Tt_from_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTt_then(Tt_thenContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTt_then_tail(Tt_then_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUt_description(Ut_descriptionContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUt_family(Ut_familyContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUt_family_tail(Ut_family_tailContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUt_null(Ut_nullContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUt_vlan_id(Ut_vlan_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVariable(VariableContext ctx) {
      // TODO Auto-generated method stub

   }

   public JuniperVendorConfiguration getConfiguration() {
      return _masterConfiguration;
   }

   private void todo(ParserRuleContext ctx) {
      todo(ctx, "Unknown");
   }

   private void todo(ParserRuleContext ctx, String reason) {
      String ruleName = _parser.getParser().getRuleNames()[ctx.getRuleIndex()];
      if (_rulesWithSuppressedWarnings.contains(ruleName)) {
         return;
      }
      String prefix = "WARNING " + (_warnings.size() + 1) + ": ";
      StringBuilder sb = new StringBuilder();
      List<String> ruleNames = Arrays.asList(CiscoGrammar.ruleNames);
      String ruleStack = ctx.toString(ruleNames);
      sb.append(prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append(prefix + "Reason: " + reason + "\n");
      sb.append(prefix + "Rule context follows:\n");
      int start = ctx.start.getStartIndex();
      int startLine = ctx.start.getLine();
      int end = ctx.stop.getStopIndex();
      String ruleText = _text.substring(start, end + 1);
      String[] ruleTextLines = ruleText.split("\\n");
      for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
         String contextPrefix = prefix + " line " + line + ": ";
         sb.append(contextPrefix + ruleTextLines[i] + "\n");
      }
      sb.append(prefix + "Parse tree follows:\n");
      String parseTreePrefix = prefix + "PARSE TREE: ";
      String parseTreeText = ParseTreePrettyPrinter.print(ctx, _parser);
      String[] parseTreeLines = parseTreeText.split("\n");
      for (String parseTreeLine : parseTreeLines) {
         sb.append(parseTreePrefix + parseTreeLine + "\n");
      }
      _warnings.add(sb.toString());
   }

   @Override
   public void visitErrorNode(ErrorNode arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void visitTerminal(TerminalNode arg0) {
      // TODO Auto-generated method stub

   }

}
