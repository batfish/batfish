package org.batfish.grammar.cisco.controlplane;

import java.util.ArrayList;
import java.util.List;

//import org.antlr.v4.runtime.CommonToken;
//import org.antlr.v4.runtime.ParserRuleContext;
//import org.antlr.v4.runtime.Token;
//import org.antlr.v4.runtime.tree.TerminalNode;
//import org.antlr.v4.runtime.tree.TerminalNodeImpl;


import org.batfish.grammar.cisco.*;
import org.batfish.grammar.cisco.CiscoParser.*;

public class CiscoControlPlaneStripper extends CiscoParserBaseListener {
   /*
    * @Override public void visitTerminal(TerminalNode t) { Token symbol =
    * t.getSymbol(); int type = symbol.getType(); switch (type) { case
    * CiscoCommonLexer.COMMENT_LINE: ParserRuleContext parent =
    * (ParserRuleContext)t.getParent().getPayload(); int index =
    * parent.children.indexOf(t); CommonToken ct = new
    * CommonToken(CiscoCommonLexer.COMMENT_LINE, "! (COMMENT REMOVED)\n");
    * TerminalNodeImpl newNode = new TerminalNodeImpl(ct);
    * parent.children.remove(index); parent.children.add(index, newNode); break;
    * default: break; } }
    */

   public List<StanzaContext> _stanzas;

   public CiscoControlPlaneStripper() {
      _stanzas = new ArrayList<StanzaContext>();
   }
   /*
    * @Override public void exitIp_ospf_dead_interval_if_stanza(
    * Ip_ospf_dead_interval_if_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterStandard_access_list_stanza(
    * Standard_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitStandard_access_list_stanza(
    * Standard_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_route_map_rb_stanza(
    * Neighbor_route_map_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_route_map_rb_stanza(
    * Neighbor_route_map_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterMatch_ipv6_rm_stanza(Match_ipv6_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitMatch_ipv6_rm_stanza(Match_ipv6_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterRouter_id_ro_stanza(Router_id_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitRouter_id_ro_stanza(Router_id_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterPassive_interface_default_ro_stanza(
    * Passive_interface_default_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitPassive_interface_default_ro_stanza(
    * Passive_interface_default_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterAuto_summary_af_stanza(Auto_summary_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitAuto_summary_af_stanza(Auto_summary_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterRouter_ospf_stanza(Router_ospf_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitRouter_ospf_stanza(Router_ospf_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void
    * enterMaximum_paths_ro_stanza(Maximum_paths_ro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitMaximum_paths_ro_stanza(Maximum_paths_ro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterLog_adjacency_changes_ipv6_ro_stanza(
    * Log_adjacency_changes_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitLog_adjacency_changes_ipv6_ro_stanza(
    * Log_adjacency_changes_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterDefault_information_ipv6_ro_stanza(
    * Default_information_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitDefault_information_ipv6_ro_stanza(
    * Default_information_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNull_ipv6_ro_stanza(Null_ipv6_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitNull_ipv6_ro_stanza(Null_ipv6_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNeighbor_route_reflector_client_af_stanza(
    * Neighbor_route_reflector_client_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNeighbor_route_reflector_client_af_stanza(
    * Neighbor_route_reflector_client_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterCertificate_stanza(Certificate_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitCertificate_stanza(Certificate_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNeighbor_peer_group_assignment_tail_bgp(
    * Neighbor_peer_group_assignment_tail_bgpContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNeighbor_peer_group_assignment_tail_bgp(
    * Neighbor_peer_group_assignment_tail_bgpContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * enterAccess_list_ip_range(Access_list_ip_rangeContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitAccess_list_ip_range(Access_list_ip_rangeContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterAggregate_address_af_stanza(
    * Aggregate_address_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitAggregate_address_af_stanza(
    * Aggregate_address_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_default_originate_af_stanza(
    * Neighbor_default_originate_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_default_originate_af_stanza(
    * Neighbor_default_originate_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterSwitchport_mode_trunk_stanza(
    * Switchport_mode_trunk_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSwitchport_mode_trunk_stanza(
    * Switchport_mode_trunk_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_peer_group_assignment_af_stanza(
    * Neighbor_peer_group_assignment_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNeighbor_peer_group_assignment_af_stanza(
    * Neighbor_peer_group_assignment_af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterRedistribute_bgp_ro_stanza(
    * Redistribute_bgp_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_bgp_ro_stanza(
    * Redistribute_bgp_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterSwitchport_access_if_stanza(
    * Switchport_access_if_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSwitchport_access_if_stanza(
    * Switchport_access_if_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterAccess_list_action(Access_list_actionContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitAccess_list_action(Access_list_actionContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNetwork6_rb_stanza(Network6_rb_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitNetwork6_rb_stanza(Network6_rb_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNeighbor_peer_group_creation_rb_stanza(
    * Neighbor_peer_group_creation_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNeighbor_peer_group_creation_rb_stanza(
    * Neighbor_peer_group_creation_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterSet_local_preference_rm_stanza(
    * Set_local_preference_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSet_local_preference_rm_stanza(
    * Set_local_preference_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterNull_block_substanza(Null_block_substanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNull_block_substanza(Null_block_substanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterSwitchport_mode_dynamic_auto_stanza(
    * Switchport_mode_dynamic_auto_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitSwitchport_mode_dynamic_auto_stanza(
    * Switchport_mode_dynamic_auto_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterCluster_id_bgp_rb_stanza(Cluster_id_bgp_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void
    * exitCluster_id_bgp_rb_stanza(Cluster_id_bgp_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterIp_community_list_standard_numbered_stanza(
    * Ip_community_list_standard_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_community_list_standard_numbered_stanza(
    * Ip_community_list_standard_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNull_if_stanza(Null_if_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitNull_if_stanza(Null_if_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterStandard_access_list_named_stanza(
    * Standard_access_list_named_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitStandard_access_list_named_stanza(
    * Standard_access_list_named_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterMatch_community_list_rm_stanza(
    * Match_community_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitMatch_community_list_rm_stanza(
    * Match_community_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterRouter_id_ipv6_ro_stanza(Router_id_ipv6_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void
    * exitRouter_id_ipv6_ro_stanza(Router_id_ipv6_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterNetwork6_tail_bgp(Network6_tail_bgpContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitNetwork6_tail_bgp(Network6_tail_bgpContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterStanza(StanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitStanza(StanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterPassive_interface_ro_stanza(
    * Passive_interface_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitPassive_interface_ro_stanza(
    * Passive_interface_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterHostname_stanza(Hostname_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitHostname_stanza(Hostname_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterSet_community_none_rm_stanza(
    * Set_community_none_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSet_community_none_rm_stanza(
    * Set_community_none_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterAggregate_address_rb_stanza(
    * Aggregate_address_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitAggregate_address_rb_stanza(
    * Aggregate_address_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterAf_stanza(Af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitAf_stanza(Af_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_send_community_af_stanza(
    * Neighbor_send_community_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_send_community_af_stanza(
    * Neighbor_send_community_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIp_community_list_expanded_stanza(
    * Ip_community_list_expanded_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitIp_community_list_expanded_stanza(
    * Ip_community_list_expanded_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIpx_sap_access_list_numbered_stanza(
    * Ipx_sap_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitIpx_sap_access_list_numbered_stanza(
    * Ipx_sap_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterMatch_tag_rm_stanza(Match_tag_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitMatch_tag_rm_stanza(Match_tag_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void
    * enterSet_metric_rm_stanza(Set_metric_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitSet_metric_rm_stanza(Set_metric_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterRedistribute_ipv6_ro_stanza(
    * Redistribute_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_ipv6_ro_stanza(
    * Redistribute_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNetwork6_af_stanza(Network6_af_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitNetwork6_af_stanza(Network6_af_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNeighbor_shutdown_rb_stanza(
    * Neighbor_shutdown_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_shutdown_rb_stanza(
    * Neighbor_shutdown_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterRedistribute_static_rb_stanza(
    * Redistribute_static_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_static_rb_stanza(
    * Redistribute_static_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterIp_community_list_standard_named_stanza(
    * Ip_community_list_standard_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_community_list_standard_named_stanza(
    * Ip_community_list_standard_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterPort(PortContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitPort(PortContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterSet_interface_rm_stanza(Set_interface_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitSet_interface_rm_stanza(Set_interface_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterAppletalk_access_list_null_tail(
    * Appletalk_access_list_null_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitAppletalk_access_list_null_tail(
    * Appletalk_access_list_null_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterRange(RangeContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitRange(RangeContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIp_as_path_access_list_tail(
    * Ip_as_path_access_list_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIp_as_path_access_list_tail(
    * Ip_as_path_access_list_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterExtended_access_list_null_tail(
    * Extended_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitExtended_access_list_null_tail(
    * Extended_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterNo_ip_address_if_stanza(No_ip_address_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitNo_ip_address_if_stanza(No_ip_address_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * enterDefault_metric_af_stanza(Default_metric_af_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void
    * exitDefault_metric_af_stanza(Default_metric_af_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterNetwork_ro_stanza(Network_ro_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitNetwork_ro_stanza(Network_ro_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterInterface_name(Interface_nameContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitInterface_name(Interface_nameContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterSwitchport_trunk_encapsulation_if_stanza(
    * Switchport_trunk_encapsulation_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitSwitchport_trunk_encapsulation_if_stanza(
    * Switchport_trunk_encapsulation_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterSwitchport_trunk_encapsulation(
    * Switchport_trunk_encapsulationContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSwitchport_trunk_encapsulation(
    * Switchport_trunk_encapsulationContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterProtocol_type_code_access_list_null_tail(
    * Protocol_type_code_access_list_null_tailContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitProtocol_type_code_access_list_null_tail(
    * Protocol_type_code_access_list_null_tailContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * enterSet_origin_rm_stanza(Set_origin_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitSet_origin_rm_stanza(Set_origin_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterShutdown_if_stanza(Shutdown_if_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitShutdown_if_stanza(Shutdown_if_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterIp_community_list_expanded_numbered_stanza(
    * Ip_community_list_expanded_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_community_list_expanded_numbered_stanza(
    * Ip_community_list_expanded_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterMatch_ip_prefix_list_rm_stanza(
    * Match_ip_prefix_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitMatch_ip_prefix_list_rm_stanza(
    * Match_ip_prefix_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRedistribute_ospf_rb_stanza(
    * Redistribute_ospf_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_ospf_rb_stanza(
    * Redistribute_ospf_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterIp_route_stanza(Ip_route_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitIp_route_stanza(Ip_route_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterStandard_access_list_null_tail(
    * Standard_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitStandard_access_list_null_tail(
    * Standard_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_activate_af_stanza(
    * Neighbor_activate_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_activate_af_stanza(
    * Neighbor_activate_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterRouter_id_bgp_rb_stanza(Router_id_bgp_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitRouter_id_bgp_rb_stanza(Router_id_bgp_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterIf_stanza(If_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIf_stanza(If_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterIp_community_list_standard_stanza(
    * Ip_community_list_standard_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitIp_community_list_standard_stanza(
    * Ip_community_list_standard_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterMatch_rm_stanza(Match_rm_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitMatch_rm_stanza(Match_rm_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterNull_ro_stanza(Null_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitNull_ro_stanza(Null_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterSet_ipv6_rm_stanza(Set_ipv6_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitSet_ipv6_rm_stanza(Set_ipv6_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterNull_rb_stanza(Null_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitNull_rb_stanza(Null_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterIp_community_list_expanded_named_stanza(
    * Ip_community_list_expanded_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_community_list_expanded_named_stanza(
    * Ip_community_list_expanded_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterProtocol_type_code_access_list_numbered_stanza(
    * Protocol_type_code_access_list_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitProtocol_type_code_access_list_numbered_stanza(
    * Protocol_type_code_access_list_numbered_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_update_source_rb_stanza(
    * Neighbor_update_source_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_update_source_rb_stanza(
    * Neighbor_update_source_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterRedistribute_connected_tail_bgp(
    * Redistribute_connected_tail_bgpContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitRedistribute_connected_tail_bgp(
    * Redistribute_connected_tail_bgpContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIp_prefix_list_tail(Ip_prefix_list_tailContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterRoute_map_stanza(Route_map_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitRoute_map_stanza(Route_map_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void enterCisco_configuration(Cisco_configurationContext
    * ctx) { _stanzas.addAll(ctx.sl); }
    * @Override public void exitCisco_configuration(Cisco_configurationContext
    * ctx) { ctx.sl = _stanzas; }
    * @Override public void enterMatch_as_path_access_list_rm_stanza(
    * Match_as_path_access_list_rm_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitMatch_as_path_access_list_rm_stanza(
    * Match_as_path_access_list_rm_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterAddress_family_rb_stanza(Address_family_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void
    * exitAddress_family_rb_stanza(Address_family_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterRo_stanza(Ro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitRo_stanza(Ro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_ebgp_multihop_rb_stanza(
    * Neighbor_ebgp_multihop_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_ebgp_multihop_rb_stanza(
    * Neighbor_ebgp_multihop_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterSet_community_rm_stanza(Set_community_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitSet_community_rm_stanza(Set_community_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterRedistribute_static_tail_bgp(
    * Redistribute_static_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_static_tail_bgp(
    * Redistribute_static_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterMacro_stanza(Macro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitMacro_stanza(Macro_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNull_standalone_if_stanza(
    * Null_standalone_if_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitNull_standalone_if_stanza(
    * Null_standalone_if_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitBanner_stanza(Banner_stanzaContext ctx) {
    * _stanzas.remove(ctx); }
    * @Override public void enterSet_comm_list_delete_rm_stanza(
    * Set_comm_list_delete_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSet_comm_list_delete_rm_stanza(
    * Set_comm_list_delete_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterSwitchport_trunk_native_if_stanza(
    * Switchport_trunk_native_if_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitSwitchport_trunk_native_if_stanza(
    * Switchport_trunk_native_if_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterRoute_map_tail(Route_map_tailContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitRoute_map_tail(Route_map_tailContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterIp_address_secondary_if_stanza(
    * Ip_address_secondary_if_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIp_address_secondary_if_stanza(
    * Ip_address_secondary_if_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_route_map_af_stanza(
    * Neighbor_route_map_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_route_map_af_stanza(
    * Neighbor_route_map_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterMatch_length_rm_stanza(Match_length_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitMatch_length_rm_stanza(Match_length_rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterRedistribute_connected_rb_stanza(
    * Redistribute_connected_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitRedistribute_connected_rb_stanza(
    * Redistribute_connected_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNull_standalone_ro_stanza(
    * Null_standalone_ro_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitNull_standalone_ro_stanza(
    * Null_standalone_ro_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterExact_match(Exact_matchContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitExact_match(Exact_matchContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_prefix_list_rb_stanza(
    * Neighbor_prefix_list_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_prefix_list_rb_stanza(
    * Neighbor_prefix_list_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterStandard_access_list_numbered_stanza(
    * Standard_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitStandard_access_list_numbered_stanza(
    * Standard_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNeighbor_route_map_tail_bgp(
    * Neighbor_route_map_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_route_map_tail_bgp(
    * Neighbor_route_map_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterSubrange(SubrangeContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitSubrange(SubrangeContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterSet_rm_stanza(Set_rm_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitSet_rm_stanza(Set_rm_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterPort_specifier(Port_specifierContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitPort_specifier(Port_specifierContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterAggregate_address_tail_bgp(
    * Aggregate_address_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitAggregate_address_tail_bgp(
    * Aggregate_address_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRedistribute_connected_ro_stanza(
    * Redistribute_connected_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitRedistribute_connected_ro_stanza(
    * Redistribute_connected_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterIpv6_router_ospf_stanza(Ipv6_router_ospf_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitIpv6_router_ospf_stanza(Ipv6_router_ospf_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterInterface_stanza(Interface_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitInterface_stanza(Interface_stanzaContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void enterNeighbor_send_community_tail_bgp(
    * Neighbor_send_community_tail_bgpContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_send_community_tail_bgp(
    * Neighbor_send_community_tail_bgpContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNull_standalone_rb_stanza(
    * Null_standalone_rb_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitNull_standalone_rb_stanza(
    * Null_standalone_rb_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterExtended_access_list_named_stanza(
    * Extended_access_list_named_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitExtended_access_list_named_stanza(
    * Extended_access_list_named_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterSwitchport_trunk_allowed_if_stanza(
    * Switchport_trunk_allowed_if_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitSwitchport_trunk_allowed_if_stanza(
    * Switchport_trunk_allowed_if_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterSwitchport_mode_dynamic_desirable_stanza(
    * Switchport_mode_dynamic_desirable_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitSwitchport_mode_dynamic_desirable_stanza(
    * Switchport_mode_dynamic_desirable_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterExtended_access_list_stanza(
    * Extended_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitExtended_access_list_stanza(
    * Extended_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterIp_ospf_dead_interval_minimal_if_stanza(
    * Ip_ospf_dead_interval_minimal_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_ospf_dead_interval_minimal_if_stanza(
    * Ip_ospf_dead_interval_minimal_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterAppletalk_access_list_numbered_stanza(
    * Appletalk_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitAppletalk_access_list_numbered_stanza(
    * Appletalk_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterRb_stanza(Rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitRb_stanza(Rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_filter_list_af_stanza(
    * Neighbor_filter_list_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_filter_list_af_stanza(
    * Neighbor_filter_list_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterMatch_ip_access_list_rm_stanza(
    * Match_ip_access_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitMatch_ip_access_list_rm_stanza(
    * Match_ip_access_list_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterIp_ospf_cost_if_stanza(Ip_ospf_cost_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitIp_ospf_cost_if_stanza(Ip_ospf_cost_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterDefault_information_ro_stanza(
    * Default_information_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitDefault_information_ro_stanza(
    * Default_information_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterIp_community_list_standard_tail(
    * Ip_community_list_standard_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitIp_community_list_standard_tail(
    * Ip_community_list_standard_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIp_access_group_if_stanza(
    * Ip_access_group_if_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitIp_access_group_if_stanza(
    * Ip_access_group_if_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterNull_af_stanza(Null_af_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitNull_af_stanza(Null_af_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterNeighbor_remote_as_rb_stanza(
    * Neighbor_remote_as_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_remote_as_rb_stanza(
    * Neighbor_remote_as_rb_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterIp_address_if_stanza(Ip_address_if_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitIp_address_if_stanza(Ip_address_if_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterExtended_access_list_tail(
    * Extended_access_list_tailContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitExtended_access_list_tail(
    * Extended_access_list_tailContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterNull_stanza(Null_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNull_stanza(Null_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNo_neighbor_activate_af_stanza(
    * No_neighbor_activate_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNo_neighbor_activate_af_stanza(
    * No_neighbor_activate_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterAddress_family_vrf_stanza(
    * Address_family_vrf_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitAddress_family_vrf_stanza(
    * Address_family_vrf_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterSet_as_path_prepend_rm_stanza(
    * Set_as_path_prepend_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSet_as_path_prepend_rm_stanza(
    * Set_as_path_prepend_rm_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterSwitchport_mode_access_stanza(
    * Switchport_mode_access_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitSwitchport_mode_access_stanza(
    * Switchport_mode_access_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_send_community_rb_stanza(
    * Neighbor_send_community_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_send_community_rb_stanza(
    * Neighbor_send_community_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNull_standalone_af_stanza(
    * Null_standalone_af_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitNull_standalone_af_stanza(
    * Null_standalone_af_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterPassive_interface_ipv6_ro_stanza(
    * Passive_interface_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitPassive_interface_ipv6_ro_stanza(
    * Passive_interface_ipv6_ro_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterRedistribute_ospf_tail_bgp(
    * Redistribute_ospf_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_ospf_tail_bgp(
    * Redistribute_ospf_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterIp_default_gateway_stanza(
    * Ip_default_gateway_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitIp_default_gateway_stanza(
    * Ip_default_gateway_stanzaContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterArea_nssa_ro_stanza(Area_nssa_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitArea_nssa_ro_stanza(Area_nssa_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterIp_community_list_expanded_tail(
    * Ip_community_list_expanded_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitIp_community_list_expanded_tail(
    * Ip_community_list_expanded_tailContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIpx_sap_access_list_stanza(
    * Ipx_sap_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIpx_sap_access_list_stanza(
    * Ipx_sap_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRedistribute_connected_af_stanza(
    * Redistribute_connected_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitRedistribute_connected_af_stanza(
    * Redistribute_connected_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterProtocol_type_code_access_list_stanza(
    * Protocol_type_code_access_list_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitProtocol_type_code_access_list_stanza(
    * Protocol_type_code_access_list_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNeighbor_filter_list_tail_bgp(
    * Neighbor_filter_list_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_filter_list_tail_bgp(
    * Neighbor_filter_list_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRm_stanza(Rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitRm_stanza(Rm_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * enterRoute_map_named_stanza(Route_map_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitRoute_map_named_stanza(Route_map_named_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterRedistribute_static_ro_stanza(
    * Redistribute_static_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_static_ro_stanza(
    * Redistribute_static_ro_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterExtended_access_list_numbered_stanza(
    * Extended_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitExtended_access_list_numbered_stanza(
    * Extended_access_list_numbered_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void
    * enterDefault_metric_tail_bgp(Default_metric_tail_bgpContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitDefault_metric_tail_bgp(Default_metric_tail_bgpContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_next_hop_self_rb_stanza(
    * Neighbor_next_hop_self_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_next_hop_self_rb_stanza(
    * Neighbor_next_hop_self_rb_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterNeighbor_prefix_list_tail_bgp(
    * Neighbor_prefix_list_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_prefix_list_tail_bgp(
    * Neighbor_prefix_list_tail_bgpContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterRedistribute_ospf_af_stanza(
    * Redistribute_ospf_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_ospf_af_stanza(
    * Redistribute_ospf_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterAppletalk_access_list_stanza(
    * Appletalk_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitAppletalk_access_list_stanza(
    * Appletalk_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNeighbor_peer_group_assignment_rb_stanza(
    * Neighbor_peer_group_assignment_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitNeighbor_peer_group_assignment_rb_stanza(
    * Neighbor_peer_group_assignment_rb_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNeighbor_remove_private_as_af_stanza(
    * Neighbor_remove_private_as_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitNeighbor_remove_private_as_af_stanza(
    * Neighbor_remove_private_as_af_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterIpx_sap_access_list_null_tail(
    * Ipx_sap_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIpx_sap_access_list_null_tail(
    * Ipx_sap_access_list_null_tailContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void
    * enterDefault_metric_rb_stanza(Default_metric_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void
    * exitDefault_metric_rb_stanza(Default_metric_rb_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterIp_as_path_access_list_stanza(
    * Ip_as_path_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIp_as_path_access_list_stanza(
    * Ip_as_path_access_list_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNull_block_stanza(Null_block_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitNull_block_stanza(Null_block_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterProtocol(ProtocolContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitProtocol(ProtocolContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterArea_ipv6_ro_stanza(Area_ipv6_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitArea_ipv6_ro_stanza(Area_ipv6_ro_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void enterIp_prefix_list_named_stanza(
    * Ip_prefix_list_named_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitIp_prefix_list_named_stanza(
    * Ip_prefix_list_named_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterCommunity(CommunityContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitCommunity(CommunityContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNull_rm_stanza(Null_rm_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitNull_rm_stanza(Null_rm_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterNeighbor_prefix_list_af_stanza(
    * Neighbor_prefix_list_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitNeighbor_prefix_list_af_stanza(
    * Neighbor_prefix_list_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterSet_ip_df_rm_stanza(Set_ip_df_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void exitSet_ip_df_rm_stanza(Set_ip_df_rm_stanzaContext
    * ctx) { // TODO Auto-generated method stub }
    * @Override public void
    * enterNull_standalone_stanza(Null_standalone_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void
    * exitNull_standalone_stanza(Null_standalone_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterIpv6_ro_stanza(Ipv6_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void exitIpv6_ro_stanza(Ipv6_ro_stanzaContext ctx) { //
    * TODO Auto-generated method stub }
    * @Override public void enterStandard_access_list_tail(
    * Standard_access_list_tailContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void exitStandard_access_list_tail(
    * Standard_access_list_tailContext ctx) { // TODO Auto-generated method stub
    * }
    * @Override public void enterRedistribute_static_af_stanza(
    * Redistribute_static_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void exitRedistribute_static_af_stanza(
    * Redistribute_static_af_stanzaContext ctx) { // TODO Auto-generated method
    * stub }
    * @Override public void enterNetwork_af_stanza(Network_af_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitNetwork_af_stanza(Network_af_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterSet_community_additive_rm_stanza(
    * Set_community_additive_rm_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void exitSet_community_additive_rm_stanza(
    * Set_community_additive_rm_stanzaContext ctx) { // TODO Auto-generated
    * method stub }
    * @Override public void enterVrf_stanza(Vrf_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void exitVrf_stanza(Vrf_stanzaContext ctx) { // TODO
    * Auto-generated method stub }
    * @Override public void enterNetwork_rb_stanza(Network_rb_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void exitNetwork_rb_stanza(Network_rb_stanzaContext ctx)
    * { // TODO Auto-generated method stub }
    * @Override public void enterNetwork_tail_bgp(Network_tail_bgpContext ctx) {
    * // TODO Auto-generated method stub }
    * @Override public void exitNetwork_tail_bgp(Network_tail_bgpContext ctx) {
    * // TODO Auto-generated method stub }
    */
}
