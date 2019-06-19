// Generated from org/batfish/grammar/f5_bigip_imish/F5BigipImishParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.f5_bigip_imish;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link F5BigipImishParser}.
 */
public interface F5BigipImishParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#f5_bigip_imish_configuration}.
	 * @param ctx the parse tree
	 */
	void enterF5_bigip_imish_configuration(F5BigipImishParser.F5_bigip_imish_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#f5_bigip_imish_configuration}.
	 * @param ctx the parse tree
	 */
	void exitF5_bigip_imish_configuration(F5BigipImishParser.F5_bigip_imish_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_end}.
	 * @param ctx the parse tree
	 */
	void enterS_end(F5BigipImishParser.S_endContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_end}.
	 * @param ctx the parse tree
	 */
	void exitS_end(F5BigipImishParser.S_endContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_ip_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterS_ip_prefix_list(F5BigipImishParser.S_ip_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_ip_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitS_ip_prefix_list(F5BigipImishParser.S_ip_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_line}.
	 * @param ctx the parse tree
	 */
	void enterS_line(F5BigipImishParser.S_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_line}.
	 * @param ctx the parse tree
	 */
	void exitS_line(F5BigipImishParser.S_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#l_con}.
	 * @param ctx the parse tree
	 */
	void enterL_con(F5BigipImishParser.L_conContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#l_con}.
	 * @param ctx the parse tree
	 */
	void exitL_con(F5BigipImishParser.L_conContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#l_login}.
	 * @param ctx the parse tree
	 */
	void enterL_login(F5BigipImishParser.L_loginContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#l_login}.
	 * @param ctx the parse tree
	 */
	void exitL_login(F5BigipImishParser.L_loginContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#l_vty}.
	 * @param ctx the parse tree
	 */
	void enterL_vty(F5BigipImishParser.L_vtyContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#l_vty}.
	 * @param ctx the parse tree
	 */
	void exitL_vty(F5BigipImishParser.L_vtyContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_null}.
	 * @param ctx the parse tree
	 */
	void enterS_null(F5BigipImishParser.S_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_null}.
	 * @param ctx the parse tree
	 */
	void exitS_null(F5BigipImishParser.S_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(F5BigipImishParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(F5BigipImishParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#ip_prefix}.
	 * @param ctx the parse tree
	 */
	void enterIp_prefix(F5BigipImishParser.Ip_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#ip_prefix}.
	 * @param ctx the parse tree
	 */
	void exitIp_prefix(F5BigipImishParser.Ip_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#ip_prefix_length}.
	 * @param ctx the parse tree
	 */
	void enterIp_prefix_length(F5BigipImishParser.Ip_prefix_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#ip_prefix_length}.
	 * @param ctx the parse tree
	 */
	void exitIp_prefix_length(F5BigipImishParser.Ip_prefix_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#line_action}.
	 * @param ctx the parse tree
	 */
	void enterLine_action(F5BigipImishParser.Line_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#line_action}.
	 * @param ctx the parse tree
	 */
	void exitLine_action(F5BigipImishParser.Line_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void enterNull_rest_of_line(F5BigipImishParser.Null_rest_of_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void exitNull_rest_of_line(F5BigipImishParser.Null_rest_of_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#uint32}.
	 * @param ctx the parse tree
	 */
	void enterUint32(F5BigipImishParser.Uint32Context ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#uint32}.
	 * @param ctx the parse tree
	 */
	void exitUint32(F5BigipImishParser.Uint32Context ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(F5BigipImishParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(F5BigipImishParser.WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#ip_spec}.
	 * @param ctx the parse tree
	 */
	void enterIp_spec(F5BigipImishParser.Ip_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#ip_spec}.
	 * @param ctx the parse tree
	 */
	void exitIp_spec(F5BigipImishParser.Ip_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_access_list}.
	 * @param ctx the parse tree
	 */
	void enterS_access_list(F5BigipImishParser.S_access_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_access_list}.
	 * @param ctx the parse tree
	 */
	void exitS_access_list(F5BigipImishParser.S_access_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_bgp_always_compare_med}.
	 * @param ctx the parse tree
	 */
	void enterRb_bgp_always_compare_med(F5BigipImishParser.Rb_bgp_always_compare_medContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_bgp_always_compare_med}.
	 * @param ctx the parse tree
	 */
	void exitRb_bgp_always_compare_med(F5BigipImishParser.Rb_bgp_always_compare_medContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_bgp_deterministic_med}.
	 * @param ctx the parse tree
	 */
	void enterRb_bgp_deterministic_med(F5BigipImishParser.Rb_bgp_deterministic_medContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_bgp_deterministic_med}.
	 * @param ctx the parse tree
	 */
	void exitRb_bgp_deterministic_med(F5BigipImishParser.Rb_bgp_deterministic_medContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_bgp_router_id}.
	 * @param ctx the parse tree
	 */
	void enterRb_bgp_router_id(F5BigipImishParser.Rb_bgp_router_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_bgp_router_id}.
	 * @param ctx the parse tree
	 */
	void exitRb_bgp_router_id(F5BigipImishParser.Rb_bgp_router_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_neighbor_ipv4}.
	 * @param ctx the parse tree
	 */
	void enterRb_neighbor_ipv4(F5BigipImishParser.Rb_neighbor_ipv4Context ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_neighbor_ipv4}.
	 * @param ctx the parse tree
	 */
	void exitRb_neighbor_ipv4(F5BigipImishParser.Rb_neighbor_ipv4Context ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_neighbor_ipv6}.
	 * @param ctx the parse tree
	 */
	void enterRb_neighbor_ipv6(F5BigipImishParser.Rb_neighbor_ipv6Context ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_neighbor_ipv6}.
	 * @param ctx the parse tree
	 */
	void exitRb_neighbor_ipv6(F5BigipImishParser.Rb_neighbor_ipv6Context ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_neighbor_peer_group}.
	 * @param ctx the parse tree
	 */
	void enterRb_neighbor_peer_group(F5BigipImishParser.Rb_neighbor_peer_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_neighbor_peer_group}.
	 * @param ctx the parse tree
	 */
	void exitRb_neighbor_peer_group(F5BigipImishParser.Rb_neighbor_peer_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_common}.
	 * @param ctx the parse tree
	 */
	void enterRbn_common(F5BigipImishParser.Rbn_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_common}.
	 * @param ctx the parse tree
	 */
	void exitRbn_common(F5BigipImishParser.Rbn_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_description}.
	 * @param ctx the parse tree
	 */
	void enterRbn_description(F5BigipImishParser.Rbn_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_description}.
	 * @param ctx the parse tree
	 */
	void exitRbn_description(F5BigipImishParser.Rbn_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_next_hop_self}.
	 * @param ctx the parse tree
	 */
	void enterRbn_next_hop_self(F5BigipImishParser.Rbn_next_hop_selfContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_next_hop_self}.
	 * @param ctx the parse tree
	 */
	void exitRbn_next_hop_self(F5BigipImishParser.Rbn_next_hop_selfContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_peer_group}.
	 * @param ctx the parse tree
	 */
	void enterRbn_peer_group(F5BigipImishParser.Rbn_peer_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_peer_group}.
	 * @param ctx the parse tree
	 */
	void exitRbn_peer_group(F5BigipImishParser.Rbn_peer_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_peer_group_assign}.
	 * @param ctx the parse tree
	 */
	void enterRbn_peer_group_assign(F5BigipImishParser.Rbn_peer_group_assignContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_peer_group_assign}.
	 * @param ctx the parse tree
	 */
	void exitRbn_peer_group_assign(F5BigipImishParser.Rbn_peer_group_assignContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_null}.
	 * @param ctx the parse tree
	 */
	void enterRbn_null(F5BigipImishParser.Rbn_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_null}.
	 * @param ctx the parse tree
	 */
	void exitRbn_null(F5BigipImishParser.Rbn_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_remote_as}.
	 * @param ctx the parse tree
	 */
	void enterRbn_remote_as(F5BigipImishParser.Rbn_remote_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_remote_as}.
	 * @param ctx the parse tree
	 */
	void exitRbn_remote_as(F5BigipImishParser.Rbn_remote_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rbn_route_map_out}.
	 * @param ctx the parse tree
	 */
	void enterRbn_route_map_out(F5BigipImishParser.Rbn_route_map_outContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rbn_route_map_out}.
	 * @param ctx the parse tree
	 */
	void exitRbn_route_map_out(F5BigipImishParser.Rbn_route_map_outContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_null}.
	 * @param ctx the parse tree
	 */
	void enterRb_null(F5BigipImishParser.Rb_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_null}.
	 * @param ctx the parse tree
	 */
	void exitRb_null(F5BigipImishParser.Rb_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rb_redistribute_kernel}.
	 * @param ctx the parse tree
	 */
	void enterRb_redistribute_kernel(F5BigipImishParser.Rb_redistribute_kernelContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rb_redistribute_kernel}.
	 * @param ctx the parse tree
	 */
	void exitRb_redistribute_kernel(F5BigipImishParser.Rb_redistribute_kernelContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_router_bgp}.
	 * @param ctx the parse tree
	 */
	void enterS_router_bgp(F5BigipImishParser.S_router_bgpContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_router_bgp}.
	 * @param ctx the parse tree
	 */
	void exitS_router_bgp(F5BigipImishParser.S_router_bgpContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#peer_group_name}.
	 * @param ctx the parse tree
	 */
	void enterPeer_group_name(F5BigipImishParser.Peer_group_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#peer_group_name}.
	 * @param ctx the parse tree
	 */
	void exitPeer_group_name(F5BigipImishParser.Peer_group_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#origin_type}.
	 * @param ctx the parse tree
	 */
	void enterOrigin_type(F5BigipImishParser.Origin_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#origin_type}.
	 * @param ctx the parse tree
	 */
	void exitOrigin_type(F5BigipImishParser.Origin_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rm_match}.
	 * @param ctx the parse tree
	 */
	void enterRm_match(F5BigipImishParser.Rm_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rm_match}.
	 * @param ctx the parse tree
	 */
	void exitRm_match(F5BigipImishParser.Rm_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rmm_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterRmm_ip_address(F5BigipImishParser.Rmm_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rmm_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitRmm_ip_address(F5BigipImishParser.Rmm_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rmm_ip_address_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterRmm_ip_address_prefix_list(F5BigipImishParser.Rmm_ip_address_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rmm_ip_address_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitRmm_ip_address_prefix_list(F5BigipImishParser.Rmm_ip_address_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rm_set}.
	 * @param ctx the parse tree
	 */
	void enterRm_set(F5BigipImishParser.Rm_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rm_set}.
	 * @param ctx the parse tree
	 */
	void exitRm_set(F5BigipImishParser.Rm_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rms_community}.
	 * @param ctx the parse tree
	 */
	void enterRms_community(F5BigipImishParser.Rms_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rms_community}.
	 * @param ctx the parse tree
	 */
	void exitRms_community(F5BigipImishParser.Rms_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rms_metric}.
	 * @param ctx the parse tree
	 */
	void enterRms_metric(F5BigipImishParser.Rms_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rms_metric}.
	 * @param ctx the parse tree
	 */
	void exitRms_metric(F5BigipImishParser.Rms_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#rms_origin}.
	 * @param ctx the parse tree
	 */
	void enterRms_origin(F5BigipImishParser.Rms_originContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#rms_origin}.
	 * @param ctx the parse tree
	 */
	void exitRms_origin(F5BigipImishParser.Rms_originContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#standard_community}.
	 * @param ctx the parse tree
	 */
	void enterStandard_community(F5BigipImishParser.Standard_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#standard_community}.
	 * @param ctx the parse tree
	 */
	void exitStandard_community(F5BigipImishParser.Standard_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link F5BigipImishParser#s_route_map}.
	 * @param ctx the parse tree
	 */
	void enterS_route_map(F5BigipImishParser.S_route_mapContext ctx);
	/**
	 * Exit a parse tree produced by {@link F5BigipImishParser#s_route_map}.
	 * @param ctx the parse tree
	 */
	void exitS_route_map(F5BigipImishParser.S_route_mapContext ctx);
}