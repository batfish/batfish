// Generated from org/batfish/grammar/cumulus_nclu/CumulusNcluParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.cumulus_nclu;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CumulusNcluParser}.
 */
public interface CumulusNcluParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#cumulus_nclu_configuration}.
	 * @param ctx the parse tree
	 */
	void enterCumulus_nclu_configuration(CumulusNcluParser.Cumulus_nclu_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#cumulus_nclu_configuration}.
	 * @param ctx the parse tree
	 */
	void exitCumulus_nclu_configuration(CumulusNcluParser.Cumulus_nclu_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(CumulusNcluParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(CumulusNcluParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#s_extra_configuration}.
	 * @param ctx the parse tree
	 */
	void enterS_extra_configuration(CumulusNcluParser.S_extra_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#s_extra_configuration}.
	 * @param ctx the parse tree
	 */
	void exitS_extra_configuration(CumulusNcluParser.S_extra_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#s_net_add}.
	 * @param ctx the parse tree
	 */
	void enterS_net_add(CumulusNcluParser.S_net_addContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#s_net_add}.
	 * @param ctx the parse tree
	 */
	void exitS_net_add(CumulusNcluParser.S_net_addContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_bond}.
	 * @param ctx the parse tree
	 */
	void enterA_bond(CumulusNcluParser.A_bondContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_bond}.
	 * @param ctx the parse tree
	 */
	void exitA_bond(CumulusNcluParser.A_bondContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bond_bond}.
	 * @param ctx the parse tree
	 */
	void enterBond_bond(CumulusNcluParser.Bond_bondContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bond_bond}.
	 * @param ctx the parse tree
	 */
	void exitBond_bond(CumulusNcluParser.Bond_bondContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bobo_slaves}.
	 * @param ctx the parse tree
	 */
	void enterBobo_slaves(CumulusNcluParser.Bobo_slavesContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bobo_slaves}.
	 * @param ctx the parse tree
	 */
	void exitBobo_slaves(CumulusNcluParser.Bobo_slavesContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bond_bridge}.
	 * @param ctx the parse tree
	 */
	void enterBond_bridge(CumulusNcluParser.Bond_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bond_bridge}.
	 * @param ctx the parse tree
	 */
	void exitBond_bridge(CumulusNcluParser.Bond_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bob_access}.
	 * @param ctx the parse tree
	 */
	void enterBob_access(CumulusNcluParser.Bob_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bob_access}.
	 * @param ctx the parse tree
	 */
	void exitBob_access(CumulusNcluParser.Bob_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bob_pvid}.
	 * @param ctx the parse tree
	 */
	void enterBob_pvid(CumulusNcluParser.Bob_pvidContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bob_pvid}.
	 * @param ctx the parse tree
	 */
	void exitBob_pvid(CumulusNcluParser.Bob_pvidContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bob_vids}.
	 * @param ctx the parse tree
	 */
	void enterBob_vids(CumulusNcluParser.Bob_vidsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bob_vids}.
	 * @param ctx the parse tree
	 */
	void exitBob_vids(CumulusNcluParser.Bob_vidsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bond_clag_id}.
	 * @param ctx the parse tree
	 */
	void enterBond_clag_id(CumulusNcluParser.Bond_clag_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bond_clag_id}.
	 * @param ctx the parse tree
	 */
	void exitBond_clag_id(CumulusNcluParser.Bond_clag_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bond_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterBond_ip_address(CumulusNcluParser.Bond_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bond_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitBond_ip_address(CumulusNcluParser.Bond_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bond_vrf}.
	 * @param ctx the parse tree
	 */
	void enterBond_vrf(CumulusNcluParser.Bond_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bond_vrf}.
	 * @param ctx the parse tree
	 */
	void exitBond_vrf(CumulusNcluParser.Bond_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_bridge}.
	 * @param ctx the parse tree
	 */
	void enterA_bridge(CumulusNcluParser.A_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_bridge}.
	 * @param ctx the parse tree
	 */
	void exitA_bridge(CumulusNcluParser.A_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bridge_bridge}.
	 * @param ctx the parse tree
	 */
	void enterBridge_bridge(CumulusNcluParser.Bridge_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bridge_bridge}.
	 * @param ctx the parse tree
	 */
	void exitBridge_bridge(CumulusNcluParser.Bridge_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#brbr_ports}.
	 * @param ctx the parse tree
	 */
	void enterBrbr_ports(CumulusNcluParser.Brbr_portsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#brbr_ports}.
	 * @param ctx the parse tree
	 */
	void exitBrbr_ports(CumulusNcluParser.Brbr_portsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#brbr_pvid}.
	 * @param ctx the parse tree
	 */
	void enterBrbr_pvid(CumulusNcluParser.Brbr_pvidContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#brbr_pvid}.
	 * @param ctx the parse tree
	 */
	void exitBrbr_pvid(CumulusNcluParser.Brbr_pvidContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#brbr_vids}.
	 * @param ctx the parse tree
	 */
	void enterBrbr_vids(CumulusNcluParser.Brbr_vidsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#brbr_vids}.
	 * @param ctx the parse tree
	 */
	void exitBrbr_vids(CumulusNcluParser.Brbr_vidsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#brbr_vlan_aware}.
	 * @param ctx the parse tree
	 */
	void enterBrbr_vlan_aware(CumulusNcluParser.Brbr_vlan_awareContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#brbr_vlan_aware}.
	 * @param ctx the parse tree
	 */
	void exitBrbr_vlan_aware(CumulusNcluParser.Brbr_vlan_awareContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_dns}.
	 * @param ctx the parse tree
	 */
	void enterA_dns(CumulusNcluParser.A_dnsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_dns}.
	 * @param ctx the parse tree
	 */
	void exitA_dns(CumulusNcluParser.A_dnsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#dns_nameserver}.
	 * @param ctx the parse tree
	 */
	void enterDns_nameserver(CumulusNcluParser.Dns_nameserverContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#dns_nameserver}.
	 * @param ctx the parse tree
	 */
	void exitDns_nameserver(CumulusNcluParser.Dns_nameserverContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#dn4}.
	 * @param ctx the parse tree
	 */
	void enterDn4(CumulusNcluParser.Dn4Context ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#dn4}.
	 * @param ctx the parse tree
	 */
	void exitDn4(CumulusNcluParser.Dn4Context ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#dn6}.
	 * @param ctx the parse tree
	 */
	void enterDn6(CumulusNcluParser.Dn6Context ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#dn6}.
	 * @param ctx the parse tree
	 */
	void exitDn6(CumulusNcluParser.Dn6Context ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_dot1x}.
	 * @param ctx the parse tree
	 */
	void enterA_dot1x(CumulusNcluParser.A_dot1xContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_dot1x}.
	 * @param ctx the parse tree
	 */
	void exitA_dot1x(CumulusNcluParser.A_dot1xContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_hostname}.
	 * @param ctx the parse tree
	 */
	void enterA_hostname(CumulusNcluParser.A_hostnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_hostname}.
	 * @param ctx the parse tree
	 */
	void exitA_hostname(CumulusNcluParser.A_hostnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_loopback}.
	 * @param ctx the parse tree
	 */
	void enterA_loopback(CumulusNcluParser.A_loopbackContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_loopback}.
	 * @param ctx the parse tree
	 */
	void exitA_loopback(CumulusNcluParser.A_loopbackContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#l_clag}.
	 * @param ctx the parse tree
	 */
	void enterL_clag(CumulusNcluParser.L_clagContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#l_clag}.
	 * @param ctx the parse tree
	 */
	void exitL_clag(CumulusNcluParser.L_clagContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#lc_vxlan_anycast_ip}.
	 * @param ctx the parse tree
	 */
	void enterLc_vxlan_anycast_ip(CumulusNcluParser.Lc_vxlan_anycast_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#lc_vxlan_anycast_ip}.
	 * @param ctx the parse tree
	 */
	void exitLc_vxlan_anycast_ip(CumulusNcluParser.Lc_vxlan_anycast_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#l_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterL_ip_address(CumulusNcluParser.L_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#l_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitL_ip_address(CumulusNcluParser.L_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_ptp}.
	 * @param ctx the parse tree
	 */
	void enterA_ptp(CumulusNcluParser.A_ptpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_ptp}.
	 * @param ctx the parse tree
	 */
	void exitA_ptp(CumulusNcluParser.A_ptpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_snmp_server}.
	 * @param ctx the parse tree
	 */
	void enterA_snmp_server(CumulusNcluParser.A_snmp_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_snmp_server}.
	 * @param ctx the parse tree
	 */
	void exitA_snmp_server(CumulusNcluParser.A_snmp_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_time}.
	 * @param ctx the parse tree
	 */
	void enterA_time(CumulusNcluParser.A_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_time}.
	 * @param ctx the parse tree
	 */
	void exitA_time(CumulusNcluParser.A_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#t_ntp}.
	 * @param ctx the parse tree
	 */
	void enterT_ntp(CumulusNcluParser.T_ntpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#t_ntp}.
	 * @param ctx the parse tree
	 */
	void exitT_ntp(CumulusNcluParser.T_ntpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#tn_server}.
	 * @param ctx the parse tree
	 */
	void enterTn_server(CumulusNcluParser.Tn_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#tn_server}.
	 * @param ctx the parse tree
	 */
	void exitTn_server(CumulusNcluParser.Tn_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#tn_source}.
	 * @param ctx the parse tree
	 */
	void enterTn_source(CumulusNcluParser.Tn_sourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#tn_source}.
	 * @param ctx the parse tree
	 */
	void exitTn_source(CumulusNcluParser.Tn_sourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#t_zone}.
	 * @param ctx the parse tree
	 */
	void enterT_zone(CumulusNcluParser.T_zoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#t_zone}.
	 * @param ctx the parse tree
	 */
	void exitT_zone(CumulusNcluParser.T_zoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_vlan}.
	 * @param ctx the parse tree
	 */
	void enterA_vlan(CumulusNcluParser.A_vlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_vlan}.
	 * @param ctx the parse tree
	 */
	void exitA_vlan(CumulusNcluParser.A_vlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#v_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterV_ip_address(CumulusNcluParser.V_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#v_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitV_ip_address(CumulusNcluParser.V_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#v_ip_address_virtual}.
	 * @param ctx the parse tree
	 */
	void enterV_ip_address_virtual(CumulusNcluParser.V_ip_address_virtualContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#v_ip_address_virtual}.
	 * @param ctx the parse tree
	 */
	void exitV_ip_address_virtual(CumulusNcluParser.V_ip_address_virtualContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#v_vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterV_vlan_id(CumulusNcluParser.V_vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#v_vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitV_vlan_id(CumulusNcluParser.V_vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#v_vlan_raw_device}.
	 * @param ctx the parse tree
	 */
	void enterV_vlan_raw_device(CumulusNcluParser.V_vlan_raw_deviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#v_vlan_raw_device}.
	 * @param ctx the parse tree
	 */
	void exitV_vlan_raw_device(CumulusNcluParser.V_vlan_raw_deviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#v_vrf}.
	 * @param ctx the parse tree
	 */
	void enterV_vrf(CumulusNcluParser.V_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#v_vrf}.
	 * @param ctx the parse tree
	 */
	void exitV_vrf(CumulusNcluParser.V_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_vrf}.
	 * @param ctx the parse tree
	 */
	void enterA_vrf(CumulusNcluParser.A_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_vrf}.
	 * @param ctx the parse tree
	 */
	void exitA_vrf(CumulusNcluParser.A_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vrf_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterVrf_ip_address(CumulusNcluParser.Vrf_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vrf_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitVrf_ip_address(CumulusNcluParser.Vrf_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vrf_vni}.
	 * @param ctx the parse tree
	 */
	void enterVrf_vni(CumulusNcluParser.Vrf_vniContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vrf_vni}.
	 * @param ctx the parse tree
	 */
	void exitVrf_vni(CumulusNcluParser.Vrf_vniContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vrf_vrf_table}.
	 * @param ctx the parse tree
	 */
	void enterVrf_vrf_table(CumulusNcluParser.Vrf_vrf_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vrf_vrf_table}.
	 * @param ctx the parse tree
	 */
	void exitVrf_vrf_table(CumulusNcluParser.Vrf_vrf_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_vxlan}.
	 * @param ctx the parse tree
	 */
	void enterA_vxlan(CumulusNcluParser.A_vxlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_vxlan}.
	 * @param ctx the parse tree
	 */
	void exitA_vxlan(CumulusNcluParser.A_vxlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vx_bridge}.
	 * @param ctx the parse tree
	 */
	void enterVx_bridge(CumulusNcluParser.Vx_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vx_bridge}.
	 * @param ctx the parse tree
	 */
	void exitVx_bridge(CumulusNcluParser.Vx_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxb_access}.
	 * @param ctx the parse tree
	 */
	void enterVxb_access(CumulusNcluParser.Vxb_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxb_access}.
	 * @param ctx the parse tree
	 */
	void exitVxb_access(CumulusNcluParser.Vxb_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxb_arp_nd_suppress}.
	 * @param ctx the parse tree
	 */
	void enterVxb_arp_nd_suppress(CumulusNcluParser.Vxb_arp_nd_suppressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxb_arp_nd_suppress}.
	 * @param ctx the parse tree
	 */
	void exitVxb_arp_nd_suppress(CumulusNcluParser.Vxb_arp_nd_suppressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxb_learning}.
	 * @param ctx the parse tree
	 */
	void enterVxb_learning(CumulusNcluParser.Vxb_learningContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxb_learning}.
	 * @param ctx the parse tree
	 */
	void exitVxb_learning(CumulusNcluParser.Vxb_learningContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vx_stp}.
	 * @param ctx the parse tree
	 */
	void enterVx_stp(CumulusNcluParser.Vx_stpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vx_stp}.
	 * @param ctx the parse tree
	 */
	void exitVx_stp(CumulusNcluParser.Vx_stpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxs_bpduguard}.
	 * @param ctx the parse tree
	 */
	void enterVxs_bpduguard(CumulusNcluParser.Vxs_bpduguardContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxs_bpduguard}.
	 * @param ctx the parse tree
	 */
	void exitVxs_bpduguard(CumulusNcluParser.Vxs_bpduguardContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxs_portbpdufilter}.
	 * @param ctx the parse tree
	 */
	void enterVxs_portbpdufilter(CumulusNcluParser.Vxs_portbpdufilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxs_portbpdufilter}.
	 * @param ctx the parse tree
	 */
	void exitVxs_portbpdufilter(CumulusNcluParser.Vxs_portbpdufilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vx_vxlan}.
	 * @param ctx the parse tree
	 */
	void enterVx_vxlan(CumulusNcluParser.Vx_vxlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vx_vxlan}.
	 * @param ctx the parse tree
	 */
	void exitVx_vxlan(CumulusNcluParser.Vx_vxlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxv_id}.
	 * @param ctx the parse tree
	 */
	void enterVxv_id(CumulusNcluParser.Vxv_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxv_id}.
	 * @param ctx the parse tree
	 */
	void exitVxv_id(CumulusNcluParser.Vxv_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vxv_local_tunnelip}.
	 * @param ctx the parse tree
	 */
	void enterVxv_local_tunnelip(CumulusNcluParser.Vxv_local_tunnelipContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vxv_local_tunnelip}.
	 * @param ctx the parse tree
	 */
	void exitVxv_local_tunnelip(CumulusNcluParser.Vxv_local_tunnelipContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#s_net_add_unrecognized}.
	 * @param ctx the parse tree
	 */
	void enterS_net_add_unrecognized(CumulusNcluParser.S_net_add_unrecognizedContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#s_net_add_unrecognized}.
	 * @param ctx the parse tree
	 */
	void exitS_net_add_unrecognized(CumulusNcluParser.S_net_add_unrecognizedContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#s_null}.
	 * @param ctx the parse tree
	 */
	void enterS_null(CumulusNcluParser.S_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#s_null}.
	 * @param ctx the parse tree
	 */
	void exitS_null(CumulusNcluParser.S_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#glob}.
	 * @param ctx the parse tree
	 */
	void enterGlob(CumulusNcluParser.GlobContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#glob}.
	 * @param ctx the parse tree
	 */
	void exitGlob(CumulusNcluParser.GlobContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#glob_range_set}.
	 * @param ctx the parse tree
	 */
	void enterGlob_range_set(CumulusNcluParser.Glob_range_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#glob_range_set}.
	 * @param ctx the parse tree
	 */
	void exitGlob_range_set(CumulusNcluParser.Glob_range_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#glob_word}.
	 * @param ctx the parse tree
	 */
	void enterGlob_word(CumulusNcluParser.Glob_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#glob_word}.
	 * @param ctx the parse tree
	 */
	void exitGlob_word(CumulusNcluParser.Glob_wordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#interface_address}.
	 * @param ctx the parse tree
	 */
	void enterInterface_address(CumulusNcluParser.Interface_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#interface_address}.
	 * @param ctx the parse tree
	 */
	void exitInterface_address(CumulusNcluParser.Interface_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ip_address}.
	 * @param ctx the parse tree
	 */
	void enterIp_address(CumulusNcluParser.Ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ip_address}.
	 * @param ctx the parse tree
	 */
	void exitIp_address(CumulusNcluParser.Ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ip_prefix}.
	 * @param ctx the parse tree
	 */
	void enterIp_prefix(CumulusNcluParser.Ip_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ip_prefix}.
	 * @param ctx the parse tree
	 */
	void exitIp_prefix(CumulusNcluParser.Ip_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ipv6_address}.
	 * @param ctx the parse tree
	 */
	void enterIpv6_address(CumulusNcluParser.Ipv6_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ipv6_address}.
	 * @param ctx the parse tree
	 */
	void exitIpv6_address(CumulusNcluParser.Ipv6_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#line_action}.
	 * @param ctx the parse tree
	 */
	void enterLine_action(CumulusNcluParser.Line_actionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#line_action}.
	 * @param ctx the parse tree
	 */
	void exitLine_action(CumulusNcluParser.Line_actionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#mac_address}.
	 * @param ctx the parse tree
	 */
	void enterMac_address(CumulusNcluParser.Mac_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#mac_address}.
	 * @param ctx the parse tree
	 */
	void exitMac_address(CumulusNcluParser.Mac_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void enterNull_rest_of_line(CumulusNcluParser.Null_rest_of_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void exitNull_rest_of_line(CumulusNcluParser.Null_rest_of_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#numbered_word}.
	 * @param ctx the parse tree
	 */
	void enterNumbered_word(CumulusNcluParser.Numbered_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#numbered_word}.
	 * @param ctx the parse tree
	 */
	void exitNumbered_word(CumulusNcluParser.Numbered_wordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(CumulusNcluParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(CumulusNcluParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#range_set}.
	 * @param ctx the parse tree
	 */
	void enterRange_set(CumulusNcluParser.Range_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#range_set}.
	 * @param ctx the parse tree
	 */
	void exitRange_set(CumulusNcluParser.Range_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#uint16}.
	 * @param ctx the parse tree
	 */
	void enterUint16(CumulusNcluParser.Uint16Context ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#uint16}.
	 * @param ctx the parse tree
	 */
	void exitUint16(CumulusNcluParser.Uint16Context ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#uint32}.
	 * @param ctx the parse tree
	 */
	void enterUint32(CumulusNcluParser.Uint32Context ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#uint32}.
	 * @param ctx the parse tree
	 */
	void exitUint32(CumulusNcluParser.Uint32Context ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterVlan_id(CumulusNcluParser.Vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitVlan_id(CumulusNcluParser.Vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vlan_range}.
	 * @param ctx the parse tree
	 */
	void enterVlan_range(CumulusNcluParser.Vlan_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vlan_range}.
	 * @param ctx the parse tree
	 */
	void exitVlan_range(CumulusNcluParser.Vlan_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vlan_range_set}.
	 * @param ctx the parse tree
	 */
	void enterVlan_range_set(CumulusNcluParser.Vlan_range_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vlan_range_set}.
	 * @param ctx the parse tree
	 */
	void exitVlan_range_set(CumulusNcluParser.Vlan_range_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#vni_number}.
	 * @param ctx the parse tree
	 */
	void enterVni_number(CumulusNcluParser.Vni_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#vni_number}.
	 * @param ctx the parse tree
	 */
	void exitVni_number(CumulusNcluParser.Vni_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(CumulusNcluParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(CumulusNcluParser.WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_bgp}.
	 * @param ctx the parse tree
	 */
	void enterA_bgp(CumulusNcluParser.A_bgpContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_bgp}.
	 * @param ctx the parse tree
	 */
	void exitA_bgp(CumulusNcluParser.A_bgpContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_common}.
	 * @param ctx the parse tree
	 */
	void enterB_common(CumulusNcluParser.B_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_common}.
	 * @param ctx the parse tree
	 */
	void exitB_common(CumulusNcluParser.B_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_autonomous_system}.
	 * @param ctx the parse tree
	 */
	void enterB_autonomous_system(CumulusNcluParser.B_autonomous_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_autonomous_system}.
	 * @param ctx the parse tree
	 */
	void exitB_autonomous_system(CumulusNcluParser.B_autonomous_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_ipv4_unicast}.
	 * @param ctx the parse tree
	 */
	void enterB_ipv4_unicast(CumulusNcluParser.B_ipv4_unicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_ipv4_unicast}.
	 * @param ctx the parse tree
	 */
	void exitB_ipv4_unicast(CumulusNcluParser.B_ipv4_unicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bi4_network}.
	 * @param ctx the parse tree
	 */
	void enterBi4_network(CumulusNcluParser.Bi4_networkContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bi4_network}.
	 * @param ctx the parse tree
	 */
	void exitBi4_network(CumulusNcluParser.Bi4_networkContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bi4_redistribute_connected}.
	 * @param ctx the parse tree
	 */
	void enterBi4_redistribute_connected(CumulusNcluParser.Bi4_redistribute_connectedContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bi4_redistribute_connected}.
	 * @param ctx the parse tree
	 */
	void exitBi4_redistribute_connected(CumulusNcluParser.Bi4_redistribute_connectedContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bi4_redistribute_static}.
	 * @param ctx the parse tree
	 */
	void enterBi4_redistribute_static(CumulusNcluParser.Bi4_redistribute_staticContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bi4_redistribute_static}.
	 * @param ctx the parse tree
	 */
	void exitBi4_redistribute_static(CumulusNcluParser.Bi4_redistribute_staticContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_l2vpn}.
	 * @param ctx the parse tree
	 */
	void enterB_l2vpn(CumulusNcluParser.B_l2vpnContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_l2vpn}.
	 * @param ctx the parse tree
	 */
	void exitB_l2vpn(CumulusNcluParser.B_l2vpnContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ble_advertise_all_vni}.
	 * @param ctx the parse tree
	 */
	void enterBle_advertise_all_vni(CumulusNcluParser.Ble_advertise_all_vniContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ble_advertise_all_vni}.
	 * @param ctx the parse tree
	 */
	void exitBle_advertise_all_vni(CumulusNcluParser.Ble_advertise_all_vniContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ble_advertise_default_gw}.
	 * @param ctx the parse tree
	 */
	void enterBle_advertise_default_gw(CumulusNcluParser.Ble_advertise_default_gwContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ble_advertise_default_gw}.
	 * @param ctx the parse tree
	 */
	void exitBle_advertise_default_gw(CumulusNcluParser.Ble_advertise_default_gwContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ble_advertise_ipv4_unicast}.
	 * @param ctx the parse tree
	 */
	void enterBle_advertise_ipv4_unicast(CumulusNcluParser.Ble_advertise_ipv4_unicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ble_advertise_ipv4_unicast}.
	 * @param ctx the parse tree
	 */
	void exitBle_advertise_ipv4_unicast(CumulusNcluParser.Ble_advertise_ipv4_unicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ble_neighbor}.
	 * @param ctx the parse tree
	 */
	void enterBle_neighbor(CumulusNcluParser.Ble_neighborContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ble_neighbor}.
	 * @param ctx the parse tree
	 */
	void exitBle_neighbor(CumulusNcluParser.Ble_neighborContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#blen_activate}.
	 * @param ctx the parse tree
	 */
	void enterBlen_activate(CumulusNcluParser.Blen_activateContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#blen_activate}.
	 * @param ctx the parse tree
	 */
	void exitBlen_activate(CumulusNcluParser.Blen_activateContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_neighbor}.
	 * @param ctx the parse tree
	 */
	void enterB_neighbor(CumulusNcluParser.B_neighborContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_neighbor}.
	 * @param ctx the parse tree
	 */
	void exitB_neighbor(CumulusNcluParser.B_neighborContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bn_interface}.
	 * @param ctx the parse tree
	 */
	void enterBn_interface(CumulusNcluParser.Bn_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bn_interface}.
	 * @param ctx the parse tree
	 */
	void exitBn_interface(CumulusNcluParser.Bn_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bni_remote_as_external}.
	 * @param ctx the parse tree
	 */
	void enterBni_remote_as_external(CumulusNcluParser.Bni_remote_as_externalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bni_remote_as_external}.
	 * @param ctx the parse tree
	 */
	void exitBni_remote_as_external(CumulusNcluParser.Bni_remote_as_externalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bni_remote_as_internal}.
	 * @param ctx the parse tree
	 */
	void enterBni_remote_as_internal(CumulusNcluParser.Bni_remote_as_internalContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bni_remote_as_internal}.
	 * @param ctx the parse tree
	 */
	void exitBni_remote_as_internal(CumulusNcluParser.Bni_remote_as_internalContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#bni_remote_as_number}.
	 * @param ctx the parse tree
	 */
	void enterBni_remote_as_number(CumulusNcluParser.Bni_remote_as_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#bni_remote_as_number}.
	 * @param ctx the parse tree
	 */
	void exitBni_remote_as_number(CumulusNcluParser.Bni_remote_as_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_router_id}.
	 * @param ctx the parse tree
	 */
	void enterB_router_id(CumulusNcluParser.B_router_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_router_id}.
	 * @param ctx the parse tree
	 */
	void exitB_router_id(CumulusNcluParser.B_router_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#b_vrf}.
	 * @param ctx the parse tree
	 */
	void enterB_vrf(CumulusNcluParser.B_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#b_vrf}.
	 * @param ctx the parse tree
	 */
	void exitB_vrf(CumulusNcluParser.B_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#frr_vrf}.
	 * @param ctx the parse tree
	 */
	void enterFrr_vrf(CumulusNcluParser.Frr_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#frr_vrf}.
	 * @param ctx the parse tree
	 */
	void exitFrr_vrf(CumulusNcluParser.Frr_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#frrv_ip_route}.
	 * @param ctx the parse tree
	 */
	void enterFrrv_ip_route(CumulusNcluParser.Frrv_ip_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#frrv_ip_route}.
	 * @param ctx the parse tree
	 */
	void exitFrrv_ip_route(CumulusNcluParser.Frrv_ip_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#frr_username}.
	 * @param ctx the parse tree
	 */
	void enterFrr_username(CumulusNcluParser.Frr_usernameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#frr_username}.
	 * @param ctx the parse tree
	 */
	void exitFrr_username(CumulusNcluParser.Frr_usernameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#frr_null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void enterFrr_null_rest_of_line(CumulusNcluParser.Frr_null_rest_of_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#frr_null_rest_of_line}.
	 * @param ctx the parse tree
	 */
	void exitFrr_null_rest_of_line(CumulusNcluParser.Frr_null_rest_of_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#frr_unrecognized}.
	 * @param ctx the parse tree
	 */
	void enterFrr_unrecognized(CumulusNcluParser.Frr_unrecognizedContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#frr_unrecognized}.
	 * @param ctx the parse tree
	 */
	void exitFrr_unrecognized(CumulusNcluParser.Frr_unrecognizedContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_interface}.
	 * @param ctx the parse tree
	 */
	void enterA_interface(CumulusNcluParser.A_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_interface}.
	 * @param ctx the parse tree
	 */
	void exitA_interface(CumulusNcluParser.A_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#i_bridge}.
	 * @param ctx the parse tree
	 */
	void enterI_bridge(CumulusNcluParser.I_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#i_bridge}.
	 * @param ctx the parse tree
	 */
	void exitI_bridge(CumulusNcluParser.I_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ib_access}.
	 * @param ctx the parse tree
	 */
	void enterIb_access(CumulusNcluParser.Ib_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ib_access}.
	 * @param ctx the parse tree
	 */
	void exitIb_access(CumulusNcluParser.Ib_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ib_pvid}.
	 * @param ctx the parse tree
	 */
	void enterIb_pvid(CumulusNcluParser.Ib_pvidContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ib_pvid}.
	 * @param ctx the parse tree
	 */
	void exitIb_pvid(CumulusNcluParser.Ib_pvidContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ib_vids}.
	 * @param ctx the parse tree
	 */
	void enterIb_vids(CumulusNcluParser.Ib_vidsContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ib_vids}.
	 * @param ctx the parse tree
	 */
	void exitIb_vids(CumulusNcluParser.Ib_vidsContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#i_clag}.
	 * @param ctx the parse tree
	 */
	void enterI_clag(CumulusNcluParser.I_clagContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#i_clag}.
	 * @param ctx the parse tree
	 */
	void exitI_clag(CumulusNcluParser.I_clagContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ic_backup_ip}.
	 * @param ctx the parse tree
	 */
	void enterIc_backup_ip(CumulusNcluParser.Ic_backup_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ic_backup_ip}.
	 * @param ctx the parse tree
	 */
	void exitIc_backup_ip(CumulusNcluParser.Ic_backup_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ic_peer_ip}.
	 * @param ctx the parse tree
	 */
	void enterIc_peer_ip(CumulusNcluParser.Ic_peer_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ic_peer_ip}.
	 * @param ctx the parse tree
	 */
	void exitIc_peer_ip(CumulusNcluParser.Ic_peer_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ic_priority}.
	 * @param ctx the parse tree
	 */
	void enterIc_priority(CumulusNcluParser.Ic_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ic_priority}.
	 * @param ctx the parse tree
	 */
	void exitIc_priority(CumulusNcluParser.Ic_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#ic_sys_mac}.
	 * @param ctx the parse tree
	 */
	void enterIc_sys_mac(CumulusNcluParser.Ic_sys_macContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#ic_sys_mac}.
	 * @param ctx the parse tree
	 */
	void exitIc_sys_mac(CumulusNcluParser.Ic_sys_macContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#i_ip_address}.
	 * @param ctx the parse tree
	 */
	void enterI_ip_address(CumulusNcluParser.I_ip_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#i_ip_address}.
	 * @param ctx the parse tree
	 */
	void exitI_ip_address(CumulusNcluParser.I_ip_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#i_vrf}.
	 * @param ctx the parse tree
	 */
	void enterI_vrf(CumulusNcluParser.I_vrfContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#i_vrf}.
	 * @param ctx the parse tree
	 */
	void exitI_vrf(CumulusNcluParser.I_vrfContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#a_routing}.
	 * @param ctx the parse tree
	 */
	void enterA_routing(CumulusNcluParser.A_routingContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#a_routing}.
	 * @param ctx the parse tree
	 */
	void exitA_routing(CumulusNcluParser.A_routingContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#r_defaults_datacenter}.
	 * @param ctx the parse tree
	 */
	void enterR_defaults_datacenter(CumulusNcluParser.R_defaults_datacenterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#r_defaults_datacenter}.
	 * @param ctx the parse tree
	 */
	void exitR_defaults_datacenter(CumulusNcluParser.R_defaults_datacenterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#r_log}.
	 * @param ctx the parse tree
	 */
	void enterR_log(CumulusNcluParser.R_logContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#r_log}.
	 * @param ctx the parse tree
	 */
	void exitR_log(CumulusNcluParser.R_logContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#rl_syslog}.
	 * @param ctx the parse tree
	 */
	void enterRl_syslog(CumulusNcluParser.Rl_syslogContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#rl_syslog}.
	 * @param ctx the parse tree
	 */
	void exitRl_syslog(CumulusNcluParser.Rl_syslogContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#r_route}.
	 * @param ctx the parse tree
	 */
	void enterR_route(CumulusNcluParser.R_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#r_route}.
	 * @param ctx the parse tree
	 */
	void exitR_route(CumulusNcluParser.R_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#r_route_map}.
	 * @param ctx the parse tree
	 */
	void enterR_route_map(CumulusNcluParser.R_route_mapContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#r_route_map}.
	 * @param ctx the parse tree
	 */
	void exitR_route_map(CumulusNcluParser.R_route_mapContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#rm_match}.
	 * @param ctx the parse tree
	 */
	void enterRm_match(CumulusNcluParser.Rm_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#rm_match}.
	 * @param ctx the parse tree
	 */
	void exitRm_match(CumulusNcluParser.Rm_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#rmm_interface}.
	 * @param ctx the parse tree
	 */
	void enterRmm_interface(CumulusNcluParser.Rmm_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#rmm_interface}.
	 * @param ctx the parse tree
	 */
	void exitRmm_interface(CumulusNcluParser.Rmm_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link CumulusNcluParser#r_service_integrated_vtysh_config}.
	 * @param ctx the parse tree
	 */
	void enterR_service_integrated_vtysh_config(CumulusNcluParser.R_service_integrated_vtysh_configContext ctx);
	/**
	 * Exit a parse tree produced by {@link CumulusNcluParser#r_service_integrated_vtysh_config}.
	 * @param ctx the parse tree
	 */
	void exitR_service_integrated_vtysh_config(CumulusNcluParser.R_service_integrated_vtysh_configContext ctx);
}