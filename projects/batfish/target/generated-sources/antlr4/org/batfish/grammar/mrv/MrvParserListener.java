// Generated from org/batfish/grammar/mrv/MrvParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.mrv;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MrvParser}.
 */
public interface MrvParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MrvParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(MrvParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(MrvParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#mrv_configuration}.
	 * @param ctx the parse tree
	 */
	void enterMrv_configuration(MrvParser.Mrv_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#mrv_configuration}.
	 * @param ctx the parse tree
	 */
	void exitMrv_configuration(MrvParser.Mrv_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async}.
	 * @param ctx the parse tree
	 */
	void enterA_async(MrvParser.A_asyncContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async}.
	 * @param ctx the parse tree
	 */
	void exitA_async(MrvParser.A_asyncContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_access}.
	 * @param ctx the parse tree
	 */
	void enterA_async_access(MrvParser.A_async_accessContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_access}.
	 * @param ctx the parse tree
	 */
	void exitA_async_access(MrvParser.A_async_accessContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_autohang}.
	 * @param ctx the parse tree
	 */
	void enterA_async_autohang(MrvParser.A_async_autohangContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_autohang}.
	 * @param ctx the parse tree
	 */
	void exitA_async_autohang(MrvParser.A_async_autohangContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_dsrwait}.
	 * @param ctx the parse tree
	 */
	void enterA_async_dsrwait(MrvParser.A_async_dsrwaitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_dsrwait}.
	 * @param ctx the parse tree
	 */
	void exitA_async_dsrwait(MrvParser.A_async_dsrwaitContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_flowcont}.
	 * @param ctx the parse tree
	 */
	void enterA_async_flowcont(MrvParser.A_async_flowcontContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_flowcont}.
	 * @param ctx the parse tree
	 */
	void exitA_async_flowcont(MrvParser.A_async_flowcontContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_maxconnections}.
	 * @param ctx the parse tree
	 */
	void enterA_async_maxconnections(MrvParser.A_async_maxconnectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_maxconnections}.
	 * @param ctx the parse tree
	 */
	void exitA_async_maxconnections(MrvParser.A_async_maxconnectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_name}.
	 * @param ctx the parse tree
	 */
	void enterA_async_name(MrvParser.A_async_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_name}.
	 * @param ctx the parse tree
	 */
	void exitA_async_name(MrvParser.A_async_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_speed}.
	 * @param ctx the parse tree
	 */
	void enterA_async_speed(MrvParser.A_async_speedContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_speed}.
	 * @param ctx the parse tree
	 */
	void exitA_async_speed(MrvParser.A_async_speedContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_async_outauthtype}.
	 * @param ctx the parse tree
	 */
	void enterA_async_outauthtype(MrvParser.A_async_outauthtypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_async_outauthtype}.
	 * @param ctx the parse tree
	 */
	void exitA_async_outauthtype(MrvParser.A_async_outauthtypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nbdecl}.
	 * @param ctx the parse tree
	 */
	void enterNbdecl(MrvParser.NbdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nbdecl}.
	 * @param ctx the parse tree
	 */
	void exitNbdecl(MrvParser.NbdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nfdecl}.
	 * @param ctx the parse tree
	 */
	void enterNfdecl(MrvParser.NfdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nfdecl}.
	 * @param ctx the parse tree
	 */
	void exitNfdecl(MrvParser.NfdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nidecl}.
	 * @param ctx the parse tree
	 */
	void enterNidecl(MrvParser.NideclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nidecl}.
	 * @param ctx the parse tree
	 */
	void exitNidecl(MrvParser.NideclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nipdecl}.
	 * @param ctx the parse tree
	 */
	void enterNipdecl(MrvParser.NipdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nipdecl}.
	 * @param ctx the parse tree
	 */
	void exitNipdecl(MrvParser.NipdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nodecl}.
	 * @param ctx the parse tree
	 */
	void enterNodecl(MrvParser.NodeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nodecl}.
	 * @param ctx the parse tree
	 */
	void exitNodecl(MrvParser.NodeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nosdecl}.
	 * @param ctx the parse tree
	 */
	void enterNosdecl(MrvParser.NosdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nosdecl}.
	 * @param ctx the parse tree
	 */
	void exitNosdecl(MrvParser.NosdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#npdecl}.
	 * @param ctx the parse tree
	 */
	void enterNpdecl(MrvParser.NpdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#npdecl}.
	 * @param ctx the parse tree
	 */
	void exitNpdecl(MrvParser.NpdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nprdecl}.
	 * @param ctx the parse tree
	 */
	void enterNprdecl(MrvParser.NprdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nprdecl}.
	 * @param ctx the parse tree
	 */
	void exitNprdecl(MrvParser.NprdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nsdecl}.
	 * @param ctx the parse tree
	 */
	void enterNsdecl(MrvParser.NsdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nsdecl}.
	 * @param ctx the parse tree
	 */
	void exitNsdecl(MrvParser.NsdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nshdecl}.
	 * @param ctx the parse tree
	 */
	void enterNshdecl(MrvParser.NshdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nshdecl}.
	 * @param ctx the parse tree
	 */
	void exitNshdecl(MrvParser.NshdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nspdecl}.
	 * @param ctx the parse tree
	 */
	void enterNspdecl(MrvParser.NspdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nspdecl}.
	 * @param ctx the parse tree
	 */
	void exitNspdecl(MrvParser.NspdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#nssdecl}.
	 * @param ctx the parse tree
	 */
	void enterNssdecl(MrvParser.NssdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#nssdecl}.
	 * @param ctx the parse tree
	 */
	void exitNssdecl(MrvParser.NssdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#quoted_string}.
	 * @param ctx the parse tree
	 */
	void enterQuoted_string(MrvParser.Quoted_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#quoted_string}.
	 * @param ctx the parse tree
	 */
	void exitQuoted_string(MrvParser.Quoted_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(MrvParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(MrvParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#type_declaration}.
	 * @param ctx the parse tree
	 */
	void enterType_declaration(MrvParser.Type_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#type_declaration}.
	 * @param ctx the parse tree
	 */
	void exitType_declaration(MrvParser.Type_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface}.
	 * @param ctx the parse tree
	 */
	void enterA_interface(MrvParser.A_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface}.
	 * @param ctx the parse tree
	 */
	void exitA_interface(MrvParser.A_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_authtype}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_authtype(MrvParser.A_interface_authtypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_authtype}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_authtype(MrvParser.A_interface_authtypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_banner}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_banner(MrvParser.A_interface_bannerContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_banner}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_banner(MrvParser.A_interface_bannerContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_bonddevs}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_bonddevs(MrvParser.A_interface_bonddevsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_bonddevs}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_bonddevs(MrvParser.A_interface_bonddevsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_bondmiimon}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_bondmiimon(MrvParser.A_interface_bondmiimonContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_bondmiimon}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_bondmiimon(MrvParser.A_interface_bondmiimonContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_bondmode}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_bondmode(MrvParser.A_interface_bondmodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_bondmode}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_bondmode(MrvParser.A_interface_bondmodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_dhcp}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_dhcp(MrvParser.A_interface_dhcpContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_dhcp}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_dhcp(MrvParser.A_interface_dhcpContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_ifname}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_ifname(MrvParser.A_interface_ifnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_ifname}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_ifname(MrvParser.A_interface_ifnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_ipaddress}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_ipaddress(MrvParser.A_interface_ipaddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_ipaddress}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_ipaddress(MrvParser.A_interface_ipaddressContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_ipbroadcast}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_ipbroadcast(MrvParser.A_interface_ipbroadcastContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_ipbroadcast}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_ipbroadcast(MrvParser.A_interface_ipbroadcastContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_ipmask}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_ipmask(MrvParser.A_interface_ipmaskContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_ipmask}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_ipmask(MrvParser.A_interface_ipmaskContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_sshportlist}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_sshportlist(MrvParser.A_interface_sshportlistContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_sshportlist}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_sshportlist(MrvParser.A_interface_sshportlistContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_interface_stat}.
	 * @param ctx the parse tree
	 */
	void enterA_interface_stat(MrvParser.A_interface_statContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_interface_stat}.
	 * @param ctx the parse tree
	 */
	void exitA_interface_stat(MrvParser.A_interface_statContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber(MrvParser.A_subscriberContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber(MrvParser.A_subscriberContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_despassword}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_despassword(MrvParser.A_subscriber_despasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_despassword}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_despassword(MrvParser.A_subscriber_despasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_guimenuname}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_guimenuname(MrvParser.A_subscriber_guimenunameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_guimenuname}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_guimenuname(MrvParser.A_subscriber_guimenunameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_idletimeout}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_idletimeout(MrvParser.A_subscriber_idletimeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_idletimeout}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_idletimeout(MrvParser.A_subscriber_idletimeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_maxsubs}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_maxsubs(MrvParser.A_subscriber_maxsubsContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_maxsubs}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_maxsubs(MrvParser.A_subscriber_maxsubsContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_menuname}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_menuname(MrvParser.A_subscriber_menunameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_menuname}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_menuname(MrvParser.A_subscriber_menunameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_name}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_name(MrvParser.A_subscriber_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_name}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_name(MrvParser.A_subscriber_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_prompt}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_prompt(MrvParser.A_subscriber_promptContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_prompt}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_prompt(MrvParser.A_subscriber_promptContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_remoteaccesslist}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_remoteaccesslist(MrvParser.A_subscriber_remoteaccesslistContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_remoteaccesslist}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_remoteaccesslist(MrvParser.A_subscriber_remoteaccesslistContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_securityv3}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_securityv3(MrvParser.A_subscriber_securityv3Context ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_securityv3}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_securityv3(MrvParser.A_subscriber_securityv3Context ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_shapassword}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_shapassword(MrvParser.A_subscriber_shapasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_shapassword}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_shapassword(MrvParser.A_subscriber_shapasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_substat}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_substat(MrvParser.A_subscriber_substatContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_substat}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_substat(MrvParser.A_subscriber_substatContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subscriber_superpassword}.
	 * @param ctx the parse tree
	 */
	void enterA_subscriber_superpassword(MrvParser.A_subscriber_superpasswordContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subscriber_superpassword}.
	 * @param ctx the parse tree
	 */
	void exitA_subscriber_superpassword(MrvParser.A_subscriber_superpasswordContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subtemplate}.
	 * @param ctx the parse tree
	 */
	void enterA_subtemplate(MrvParser.A_subtemplateContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subtemplate}.
	 * @param ctx the parse tree
	 */
	void exitA_subtemplate(MrvParser.A_subtemplateContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subtemplate_idletimeout}.
	 * @param ctx the parse tree
	 */
	void enterA_subtemplate_idletimeout(MrvParser.A_subtemplate_idletimeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subtemplate_idletimeout}.
	 * @param ctx the parse tree
	 */
	void exitA_subtemplate_idletimeout(MrvParser.A_subtemplate_idletimeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subtemplate_sercurityv3}.
	 * @param ctx the parse tree
	 */
	void enterA_subtemplate_sercurityv3(MrvParser.A_subtemplate_sercurityv3Context ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subtemplate_sercurityv3}.
	 * @param ctx the parse tree
	 */
	void exitA_subtemplate_sercurityv3(MrvParser.A_subtemplate_sercurityv3Context ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_subtemplate_prompt}.
	 * @param ctx the parse tree
	 */
	void enterA_subtemplate_prompt(MrvParser.A_subtemplate_promptContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_subtemplate_prompt}.
	 * @param ctx the parse tree
	 */
	void exitA_subtemplate_prompt(MrvParser.A_subtemplate_promptContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system}.
	 * @param ctx the parse tree
	 */
	void enterA_system(MrvParser.A_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system}.
	 * @param ctx the parse tree
	 */
	void exitA_system(MrvParser.A_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_configversion}.
	 * @param ctx the parse tree
	 */
	void enterA_system_configversion(MrvParser.A_system_configversionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_configversion}.
	 * @param ctx the parse tree
	 */
	void exitA_system_configversion(MrvParser.A_system_configversionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_dns1}.
	 * @param ctx the parse tree
	 */
	void enterA_system_dns1(MrvParser.A_system_dns1Context ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_dns1}.
	 * @param ctx the parse tree
	 */
	void exitA_system_dns1(MrvParser.A_system_dns1Context ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_dns2}.
	 * @param ctx the parse tree
	 */
	void enterA_system_dns2(MrvParser.A_system_dns2Context ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_dns2}.
	 * @param ctx the parse tree
	 */
	void exitA_system_dns2(MrvParser.A_system_dns2Context ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_gateway1}.
	 * @param ctx the parse tree
	 */
	void enterA_system_gateway1(MrvParser.A_system_gateway1Context ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_gateway1}.
	 * @param ctx the parse tree
	 */
	void exitA_system_gateway1(MrvParser.A_system_gateway1Context ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_gui}.
	 * @param ctx the parse tree
	 */
	void enterA_system_gui(MrvParser.A_system_guiContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_gui}.
	 * @param ctx the parse tree
	 */
	void exitA_system_gui(MrvParser.A_system_guiContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notiffacility}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notiffacility(MrvParser.A_system_notiffacilityContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notiffacility}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notiffacility(MrvParser.A_system_notiffacilityContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifpriority}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifpriority(MrvParser.A_system_notifpriorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifpriority}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifpriority(MrvParser.A_system_notifpriorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyaddressname}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyaddressname(MrvParser.A_system_notifyaddressnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyaddressname}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyaddressname(MrvParser.A_system_notifyaddressnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyaddressservice}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyaddressservice(MrvParser.A_system_notifyaddressserviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyaddressservice}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyaddressservice(MrvParser.A_system_notifyaddressserviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyaddressstate}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyaddressstate(MrvParser.A_system_notifyaddressstateContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyaddressstate}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyaddressstate(MrvParser.A_system_notifyaddressstateContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyservicename}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyservicename(MrvParser.A_system_notifyservicenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyservicename}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyservicename(MrvParser.A_system_notifyservicenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyserviceprotocol}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyserviceprotocol(MrvParser.A_system_notifyserviceprotocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyserviceprotocol}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyserviceprotocol(MrvParser.A_system_notifyserviceprotocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_notifyserviceraw}.
	 * @param ctx the parse tree
	 */
	void enterA_system_notifyserviceraw(MrvParser.A_system_notifyservicerawContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_notifyserviceraw}.
	 * @param ctx the parse tree
	 */
	void exitA_system_notifyserviceraw(MrvParser.A_system_notifyservicerawContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_ntp}.
	 * @param ctx the parse tree
	 */
	void enterA_system_ntp(MrvParser.A_system_ntpContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_ntp}.
	 * @param ctx the parse tree
	 */
	void exitA_system_ntp(MrvParser.A_system_ntpContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_ntpaddress}.
	 * @param ctx the parse tree
	 */
	void enterA_system_ntpaddress(MrvParser.A_system_ntpaddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_ntpaddress}.
	 * @param ctx the parse tree
	 */
	void exitA_system_ntpaddress(MrvParser.A_system_ntpaddressContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_ntpaltaddress}.
	 * @param ctx the parse tree
	 */
	void enterA_system_ntpaltaddress(MrvParser.A_system_ntpaltaddressContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_ntpaltaddress}.
	 * @param ctx the parse tree
	 */
	void exitA_system_ntpaltaddress(MrvParser.A_system_ntpaltaddressContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_ntpsourceinterface}.
	 * @param ctx the parse tree
	 */
	void enterA_system_ntpsourceinterface(MrvParser.A_system_ntpsourceinterfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_ntpsourceinterface}.
	 * @param ctx the parse tree
	 */
	void exitA_system_ntpsourceinterface(MrvParser.A_system_ntpsourceinterfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_radprimacctsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_radprimacctsecret(MrvParser.A_system_radprimacctsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_radprimacctsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_radprimacctsecret(MrvParser.A_system_radprimacctsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_radprimsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_radprimsecret(MrvParser.A_system_radprimsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_radprimsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_radprimsecret(MrvParser.A_system_radprimsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_radsecacctsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_radsecacctsecret(MrvParser.A_system_radsecacctsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_radsecacctsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_radsecacctsecret(MrvParser.A_system_radsecacctsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_radsecsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_radsecsecret(MrvParser.A_system_radsecsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_radsecsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_radsecsecret(MrvParser.A_system_radsecsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmp}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmp(MrvParser.A_system_snmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmp}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmp(MrvParser.A_system_snmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmpgetclient}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmpgetclient(MrvParser.A_system_snmpgetclientContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmpgetclient}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmpgetclient(MrvParser.A_system_snmpgetclientContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmpgetcommunity}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmpgetcommunity(MrvParser.A_system_snmpgetcommunityContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmpgetcommunity}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmpgetcommunity(MrvParser.A_system_snmpgetcommunityContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmpsourceinterface}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmpsourceinterface(MrvParser.A_system_snmpsourceinterfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmpsourceinterface}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmpsourceinterface(MrvParser.A_system_snmpsourceinterfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmptrapclient}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmptrapclient(MrvParser.A_system_snmptrapclientContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmptrapclient}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmptrapclient(MrvParser.A_system_snmptrapclientContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_snmptrapcommunity}.
	 * @param ctx the parse tree
	 */
	void enterA_system_snmptrapcommunity(MrvParser.A_system_snmptrapcommunityContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_snmptrapcommunity}.
	 * @param ctx the parse tree
	 */
	void exitA_system_snmptrapcommunity(MrvParser.A_system_snmptrapcommunityContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_ssh}.
	 * @param ctx the parse tree
	 */
	void enterA_system_ssh(MrvParser.A_system_sshContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_ssh}.
	 * @param ctx the parse tree
	 */
	void exitA_system_ssh(MrvParser.A_system_sshContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_systemname}.
	 * @param ctx the parse tree
	 */
	void enterA_system_systemname(MrvParser.A_system_systemnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_systemname}.
	 * @param ctx the parse tree
	 */
	void exitA_system_systemname(MrvParser.A_system_systemnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplusprimaddr}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplusprimaddr(MrvParser.A_system_tacplusprimaddrContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplusprimaddr}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplusprimaddr(MrvParser.A_system_tacplusprimaddrContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplusprimacctsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplusprimacctsecret(MrvParser.A_system_tacplusprimacctsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplusprimacctsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplusprimacctsecret(MrvParser.A_system_tacplusprimacctsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplusprimauthorsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplusprimauthorsecret(MrvParser.A_system_tacplusprimauthorsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplusprimauthorsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplusprimauthorsecret(MrvParser.A_system_tacplusprimauthorsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplusprimsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplusprimsecret(MrvParser.A_system_tacplusprimsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplusprimsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplusprimsecret(MrvParser.A_system_tacplusprimsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplussecaddr}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplussecaddr(MrvParser.A_system_tacplussecaddrContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplussecaddr}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplussecaddr(MrvParser.A_system_tacplussecaddrContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplussecacctsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplussecacctsecret(MrvParser.A_system_tacplussecacctsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplussecacctsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplussecacctsecret(MrvParser.A_system_tacplussecacctsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplussecauthorsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplussecauthorsecret(MrvParser.A_system_tacplussecauthorsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplussecauthorsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplussecauthorsecret(MrvParser.A_system_tacplussecauthorsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplussecsecret}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplussecsecret(MrvParser.A_system_tacplussecsecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplussecsecret}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplussecsecret(MrvParser.A_system_tacplussecsecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_tacplususesub}.
	 * @param ctx the parse tree
	 */
	void enterA_system_tacplususesub(MrvParser.A_system_tacplususesubContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_tacplususesub}.
	 * @param ctx the parse tree
	 */
	void exitA_system_tacplususesub(MrvParser.A_system_tacplususesubContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_telnet}.
	 * @param ctx the parse tree
	 */
	void enterA_system_telnet(MrvParser.A_system_telnetContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_telnet}.
	 * @param ctx the parse tree
	 */
	void exitA_system_telnet(MrvParser.A_system_telnetContext ctx);
	/**
	 * Enter a parse tree produced by {@link MrvParser#a_system_telnetclient}.
	 * @param ctx the parse tree
	 */
	void enterA_system_telnetclient(MrvParser.A_system_telnetclientContext ctx);
	/**
	 * Exit a parse tree produced by {@link MrvParser#a_system_telnetclient}.
	 * @param ctx the parse tree
	 */
	void exitA_system_telnetclient(MrvParser.A_system_telnetclientContext ctx);
}