// Generated from org/batfish/grammar/flatjuniper/FlatJuniperParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.flatjuniper;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FlatJuniperParser}.
 */
public interface FlatJuniperParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#deactivate_line}.
	 * @param ctx the parse tree
	 */
	void enterDeactivate_line(FlatJuniperParser.Deactivate_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#deactivate_line}.
	 * @param ctx the parse tree
	 */
	void exitDeactivate_line(FlatJuniperParser.Deactivate_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#deactivate_line_tail}.
	 * @param ctx the parse tree
	 */
	void enterDeactivate_line_tail(FlatJuniperParser.Deactivate_line_tailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#deactivate_line_tail}.
	 * @param ctx the parse tree
	 */
	void exitDeactivate_line_tail(FlatJuniperParser.Deactivate_line_tailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#flat_juniper_configuration}.
	 * @param ctx the parse tree
	 */
	void enterFlat_juniper_configuration(FlatJuniperParser.Flat_juniper_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#flat_juniper_configuration}.
	 * @param ctx the parse tree
	 */
	void exitFlat_juniper_configuration(FlatJuniperParser.Flat_juniper_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#newline}.
	 * @param ctx the parse tree
	 */
	void enterNewline(FlatJuniperParser.NewlineContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#newline}.
	 * @param ctx the parse tree
	 */
	void exitNewline(FlatJuniperParser.NewlineContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#protect_line}.
	 * @param ctx the parse tree
	 */
	void enterProtect_line(FlatJuniperParser.Protect_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#protect_line}.
	 * @param ctx the parse tree
	 */
	void exitProtect_line(FlatJuniperParser.Protect_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(FlatJuniperParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(FlatJuniperParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_common}.
	 * @param ctx the parse tree
	 */
	void enterS_common(FlatJuniperParser.S_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_common}.
	 * @param ctx the parse tree
	 */
	void exitS_common(FlatJuniperParser.S_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_groups}.
	 * @param ctx the parse tree
	 */
	void enterS_groups(FlatJuniperParser.S_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_groups}.
	 * @param ctx the parse tree
	 */
	void exitS_groups(FlatJuniperParser.S_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_groups_named}.
	 * @param ctx the parse tree
	 */
	void enterS_groups_named(FlatJuniperParser.S_groups_namedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_groups_named}.
	 * @param ctx the parse tree
	 */
	void exitS_groups_named(FlatJuniperParser.S_groups_namedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_groups_tail}.
	 * @param ctx the parse tree
	 */
	void enterS_groups_tail(FlatJuniperParser.S_groups_tailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_groups_tail}.
	 * @param ctx the parse tree
	 */
	void exitS_groups_tail(FlatJuniperParser.S_groups_tailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_logical_systems}.
	 * @param ctx the parse tree
	 */
	void enterS_logical_systems(FlatJuniperParser.S_logical_systemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_logical_systems}.
	 * @param ctx the parse tree
	 */
	void exitS_logical_systems(FlatJuniperParser.S_logical_systemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_logical_systems_tail}.
	 * @param ctx the parse tree
	 */
	void enterS_logical_systems_tail(FlatJuniperParser.S_logical_systems_tailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_logical_systems_tail}.
	 * @param ctx the parse tree
	 */
	void exitS_logical_systems_tail(FlatJuniperParser.S_logical_systems_tailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_null}.
	 * @param ctx the parse tree
	 */
	void enterS_null(FlatJuniperParser.S_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_null}.
	 * @param ctx the parse tree
	 */
	void exitS_null(FlatJuniperParser.S_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_version}.
	 * @param ctx the parse tree
	 */
	void enterS_version(FlatJuniperParser.S_versionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_version}.
	 * @param ctx the parse tree
	 */
	void exitS_version(FlatJuniperParser.S_versionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_vlans}.
	 * @param ctx the parse tree
	 */
	void enterS_vlans(FlatJuniperParser.S_vlansContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_vlans}.
	 * @param ctx the parse tree
	 */
	void exitS_vlans(FlatJuniperParser.S_vlansContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_vlans_named}.
	 * @param ctx the parse tree
	 */
	void enterS_vlans_named(FlatJuniperParser.S_vlans_namedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_vlans_named}.
	 * @param ctx the parse tree
	 */
	void exitS_vlans_named(FlatJuniperParser.S_vlans_namedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#set_line}.
	 * @param ctx the parse tree
	 */
	void enterSet_line(FlatJuniperParser.Set_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#set_line}.
	 * @param ctx the parse tree
	 */
	void exitSet_line(FlatJuniperParser.Set_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#set_line_tail}.
	 * @param ctx the parse tree
	 */
	void enterSet_line_tail(FlatJuniperParser.Set_line_tailContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#set_line_tail}.
	 * @param ctx the parse tree
	 */
	void exitSet_line_tail(FlatJuniperParser.Set_line_tailContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#vlt_description}.
	 * @param ctx the parse tree
	 */
	void enterVlt_description(FlatJuniperParser.Vlt_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#vlt_description}.
	 * @param ctx the parse tree
	 */
	void exitVlt_description(FlatJuniperParser.Vlt_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#vlt_filter}.
	 * @param ctx the parse tree
	 */
	void enterVlt_filter(FlatJuniperParser.Vlt_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#vlt_filter}.
	 * @param ctx the parse tree
	 */
	void exitVlt_filter(FlatJuniperParser.Vlt_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#vlt_l3_interface}.
	 * @param ctx the parse tree
	 */
	void enterVlt_l3_interface(FlatJuniperParser.Vlt_l3_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#vlt_l3_interface}.
	 * @param ctx the parse tree
	 */
	void exitVlt_l3_interface(FlatJuniperParser.Vlt_l3_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#vlt_vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterVlt_vlan_id(FlatJuniperParser.Vlt_vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#vlt_vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitVlt_vlan_id(FlatJuniperParser.Vlt_vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#a_application}.
	 * @param ctx the parse tree
	 */
	void enterA_application(FlatJuniperParser.A_applicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#a_application}.
	 * @param ctx the parse tree
	 */
	void exitA_application(FlatJuniperParser.A_applicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#a_application_set}.
	 * @param ctx the parse tree
	 */
	void enterA_application_set(FlatJuniperParser.A_application_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#a_application_set}.
	 * @param ctx the parse tree
	 */
	void exitA_application_set(FlatJuniperParser.A_application_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aa_common}.
	 * @param ctx the parse tree
	 */
	void enterAa_common(FlatJuniperParser.Aa_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aa_common}.
	 * @param ctx the parse tree
	 */
	void exitAa_common(FlatJuniperParser.Aa_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aa_description}.
	 * @param ctx the parse tree
	 */
	void enterAa_description(FlatJuniperParser.Aa_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aa_description}.
	 * @param ctx the parse tree
	 */
	void exitAa_description(FlatJuniperParser.Aa_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aa_term}.
	 * @param ctx the parse tree
	 */
	void enterAa_term(FlatJuniperParser.Aa_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aa_term}.
	 * @param ctx the parse tree
	 */
	void exitAa_term(FlatJuniperParser.Aa_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aas_application}.
	 * @param ctx the parse tree
	 */
	void enterAas_application(FlatJuniperParser.Aas_applicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aas_application}.
	 * @param ctx the parse tree
	 */
	void exitAas_application(FlatJuniperParser.Aas_applicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aas_application_set}.
	 * @param ctx the parse tree
	 */
	void enterAas_application_set(FlatJuniperParser.Aas_application_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aas_application_set}.
	 * @param ctx the parse tree
	 */
	void exitAas_application_set(FlatJuniperParser.Aas_application_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_alg}.
	 * @param ctx the parse tree
	 */
	void enterAat_alg(FlatJuniperParser.Aat_algContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_alg}.
	 * @param ctx the parse tree
	 */
	void exitAat_alg(FlatJuniperParser.Aat_algContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_application_protocol}.
	 * @param ctx the parse tree
	 */
	void enterAat_application_protocol(FlatJuniperParser.Aat_application_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_application_protocol}.
	 * @param ctx the parse tree
	 */
	void exitAat_application_protocol(FlatJuniperParser.Aat_application_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_destination_port}.
	 * @param ctx the parse tree
	 */
	void enterAat_destination_port(FlatJuniperParser.Aat_destination_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_destination_port}.
	 * @param ctx the parse tree
	 */
	void exitAat_destination_port(FlatJuniperParser.Aat_destination_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_icmp_code}.
	 * @param ctx the parse tree
	 */
	void enterAat_icmp_code(FlatJuniperParser.Aat_icmp_codeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_icmp_code}.
	 * @param ctx the parse tree
	 */
	void exitAat_icmp_code(FlatJuniperParser.Aat_icmp_codeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_icmp_type}.
	 * @param ctx the parse tree
	 */
	void enterAat_icmp_type(FlatJuniperParser.Aat_icmp_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_icmp_type}.
	 * @param ctx the parse tree
	 */
	void exitAat_icmp_type(FlatJuniperParser.Aat_icmp_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_icmp6_code}.
	 * @param ctx the parse tree
	 */
	void enterAat_icmp6_code(FlatJuniperParser.Aat_icmp6_codeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_icmp6_code}.
	 * @param ctx the parse tree
	 */
	void exitAat_icmp6_code(FlatJuniperParser.Aat_icmp6_codeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_icmp6_type}.
	 * @param ctx the parse tree
	 */
	void enterAat_icmp6_type(FlatJuniperParser.Aat_icmp6_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_icmp6_type}.
	 * @param ctx the parse tree
	 */
	void exitAat_icmp6_type(FlatJuniperParser.Aat_icmp6_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_inactivity_timeout}.
	 * @param ctx the parse tree
	 */
	void enterAat_inactivity_timeout(FlatJuniperParser.Aat_inactivity_timeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_inactivity_timeout}.
	 * @param ctx the parse tree
	 */
	void exitAat_inactivity_timeout(FlatJuniperParser.Aat_inactivity_timeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_protocol}.
	 * @param ctx the parse tree
	 */
	void enterAat_protocol(FlatJuniperParser.Aat_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_protocol}.
	 * @param ctx the parse tree
	 */
	void exitAat_protocol(FlatJuniperParser.Aat_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_rpc_program_number}.
	 * @param ctx the parse tree
	 */
	void enterAat_rpc_program_number(FlatJuniperParser.Aat_rpc_program_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_rpc_program_number}.
	 * @param ctx the parse tree
	 */
	void exitAat_rpc_program_number(FlatJuniperParser.Aat_rpc_program_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_source_port}.
	 * @param ctx the parse tree
	 */
	void enterAat_source_port(FlatJuniperParser.Aat_source_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_source_port}.
	 * @param ctx the parse tree
	 */
	void exitAat_source_port(FlatJuniperParser.Aat_source_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#aat_uuid}.
	 * @param ctx the parse tree
	 */
	void enterAat_uuid(FlatJuniperParser.Aat_uuidContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#aat_uuid}.
	 * @param ctx the parse tree
	 */
	void exitAat_uuid(FlatJuniperParser.Aat_uuidContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#application_protocol}.
	 * @param ctx the parse tree
	 */
	void enterApplication_protocol(FlatJuniperParser.Application_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#application_protocol}.
	 * @param ctx the parse tree
	 */
	void exitApplication_protocol(FlatJuniperParser.Application_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_applications}.
	 * @param ctx the parse tree
	 */
	void enterS_applications(FlatJuniperParser.S_applicationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_applications}.
	 * @param ctx the parse tree
	 */
	void exitS_applications(FlatJuniperParser.S_applicationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#administrator_as}.
	 * @param ctx the parse tree
	 */
	void enterAdministrator_as(FlatJuniperParser.Administrator_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#administrator_as}.
	 * @param ctx the parse tree
	 */
	void exitAdministrator_as(FlatJuniperParser.Administrator_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#administrator_dec}.
	 * @param ctx the parse tree
	 */
	void enterAdministrator_dec(FlatJuniperParser.Administrator_decContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#administrator_dec}.
	 * @param ctx the parse tree
	 */
	void exitAdministrator_dec(FlatJuniperParser.Administrator_decContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#administrator_dotted_as}.
	 * @param ctx the parse tree
	 */
	void enterAdministrator_dotted_as(FlatJuniperParser.Administrator_dotted_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#administrator_dotted_as}.
	 * @param ctx the parse tree
	 */
	void exitAdministrator_dotted_as(FlatJuniperParser.Administrator_dotted_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#administrator_ip}.
	 * @param ctx the parse tree
	 */
	void enterAdministrator_ip(FlatJuniperParser.Administrator_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#administrator_ip}.
	 * @param ctx the parse tree
	 */
	void exitAdministrator_ip(FlatJuniperParser.Administrator_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#apply}.
	 * @param ctx the parse tree
	 */
	void enterApply(FlatJuniperParser.ApplyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#apply}.
	 * @param ctx the parse tree
	 */
	void exitApply(FlatJuniperParser.ApplyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#apply_groups}.
	 * @param ctx the parse tree
	 */
	void enterApply_groups(FlatJuniperParser.Apply_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#apply_groups}.
	 * @param ctx the parse tree
	 */
	void exitApply_groups(FlatJuniperParser.Apply_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#apply_groups_except}.
	 * @param ctx the parse tree
	 */
	void enterApply_groups_except(FlatJuniperParser.Apply_groups_exceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#apply_groups_except}.
	 * @param ctx the parse tree
	 */
	void exitApply_groups_except(FlatJuniperParser.Apply_groups_exceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#as_path_expr}.
	 * @param ctx the parse tree
	 */
	void enterAs_path_expr(FlatJuniperParser.As_path_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#as_path_expr}.
	 * @param ctx the parse tree
	 */
	void exitAs_path_expr(FlatJuniperParser.As_path_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#as_set}.
	 * @param ctx the parse tree
	 */
	void enterAs_set(FlatJuniperParser.As_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#as_set}.
	 * @param ctx the parse tree
	 */
	void exitAs_set(FlatJuniperParser.As_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#as_unit}.
	 * @param ctx the parse tree
	 */
	void enterAs_unit(FlatJuniperParser.As_unitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#as_unit}.
	 * @param ctx the parse tree
	 */
	void exitAs_unit(FlatJuniperParser.As_unitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bgp_asn}.
	 * @param ctx the parse tree
	 */
	void enterBgp_asn(FlatJuniperParser.Bgp_asnContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bgp_asn}.
	 * @param ctx the parse tree
	 */
	void exitBgp_asn(FlatJuniperParser.Bgp_asnContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#description}.
	 * @param ctx the parse tree
	 */
	void enterDescription(FlatJuniperParser.DescriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#description}.
	 * @param ctx the parse tree
	 */
	void exitDescription(FlatJuniperParser.DescriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ec_administrator}.
	 * @param ctx the parse tree
	 */
	void enterEc_administrator(FlatJuniperParser.Ec_administratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ec_administrator}.
	 * @param ctx the parse tree
	 */
	void exitEc_administrator(FlatJuniperParser.Ec_administratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ec_literal}.
	 * @param ctx the parse tree
	 */
	void enterEc_literal(FlatJuniperParser.Ec_literalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ec_literal}.
	 * @param ctx the parse tree
	 */
	void exitEc_literal(FlatJuniperParser.Ec_literalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ec_named}.
	 * @param ctx the parse tree
	 */
	void enterEc_named(FlatJuniperParser.Ec_namedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ec_named}.
	 * @param ctx the parse tree
	 */
	void exitEc_named(FlatJuniperParser.Ec_namedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ec_type}.
	 * @param ctx the parse tree
	 */
	void enterEc_type(FlatJuniperParser.Ec_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ec_type}.
	 * @param ctx the parse tree
	 */
	void exitEc_type(FlatJuniperParser.Ec_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#extended_community}.
	 * @param ctx the parse tree
	 */
	void enterExtended_community(FlatJuniperParser.Extended_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#extended_community}.
	 * @param ctx the parse tree
	 */
	void exitExtended_community(FlatJuniperParser.Extended_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#icmp_code}.
	 * @param ctx the parse tree
	 */
	void enterIcmp_code(FlatJuniperParser.Icmp_codeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#icmp_code}.
	 * @param ctx the parse tree
	 */
	void exitIcmp_code(FlatJuniperParser.Icmp_codeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#icmp_type}.
	 * @param ctx the parse tree
	 */
	void enterIcmp_type(FlatJuniperParser.Icmp_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#icmp_type}.
	 * @param ctx the parse tree
	 */
	void exitIcmp_type(FlatJuniperParser.Icmp_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#icmp6_only_type}.
	 * @param ctx the parse tree
	 */
	void enterIcmp6_only_type(FlatJuniperParser.Icmp6_only_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#icmp6_only_type}.
	 * @param ctx the parse tree
	 */
	void exitIcmp6_only_type(FlatJuniperParser.Icmp6_only_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#interface_id}.
	 * @param ctx the parse tree
	 */
	void enterInterface_id(FlatJuniperParser.Interface_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#interface_id}.
	 * @param ctx the parse tree
	 */
	void exitInterface_id(FlatJuniperParser.Interface_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ip_option}.
	 * @param ctx the parse tree
	 */
	void enterIp_option(FlatJuniperParser.Ip_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ip_option}.
	 * @param ctx the parse tree
	 */
	void exitIp_option(FlatJuniperParser.Ip_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ip_protocol}.
	 * @param ctx the parse tree
	 */
	void enterIp_protocol(FlatJuniperParser.Ip_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ip_protocol}.
	 * @param ctx the parse tree
	 */
	void exitIp_protocol(FlatJuniperParser.Ip_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#junos_application}.
	 * @param ctx the parse tree
	 */
	void enterJunos_application(FlatJuniperParser.Junos_applicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#junos_application}.
	 * @param ctx the parse tree
	 */
	void exitJunos_application(FlatJuniperParser.Junos_applicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#junos_application_set}.
	 * @param ctx the parse tree
	 */
	void enterJunos_application_set(FlatJuniperParser.Junos_application_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#junos_application_set}.
	 * @param ctx the parse tree
	 */
	void exitJunos_application_set(FlatJuniperParser.Junos_application_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#null_filler}.
	 * @param ctx the parse tree
	 */
	void enterNull_filler(FlatJuniperParser.Null_fillerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#null_filler}.
	 * @param ctx the parse tree
	 */
	void exitNull_filler(FlatJuniperParser.Null_fillerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#origin_type}.
	 * @param ctx the parse tree
	 */
	void enterOrigin_type(FlatJuniperParser.Origin_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#origin_type}.
	 * @param ctx the parse tree
	 */
	void exitOrigin_type(FlatJuniperParser.Origin_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pe_conjunction}.
	 * @param ctx the parse tree
	 */
	void enterPe_conjunction(FlatJuniperParser.Pe_conjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pe_conjunction}.
	 * @param ctx the parse tree
	 */
	void exitPe_conjunction(FlatJuniperParser.Pe_conjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pe_disjunction}.
	 * @param ctx the parse tree
	 */
	void enterPe_disjunction(FlatJuniperParser.Pe_disjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pe_disjunction}.
	 * @param ctx the parse tree
	 */
	void exitPe_disjunction(FlatJuniperParser.Pe_disjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pe_nested}.
	 * @param ctx the parse tree
	 */
	void enterPe_nested(FlatJuniperParser.Pe_nestedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pe_nested}.
	 * @param ctx the parse tree
	 */
	void exitPe_nested(FlatJuniperParser.Pe_nestedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#policy_expression}.
	 * @param ctx the parse tree
	 */
	void enterPolicy_expression(FlatJuniperParser.Policy_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#policy_expression}.
	 * @param ctx the parse tree
	 */
	void exitPolicy_expression(FlatJuniperParser.Policy_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#port}.
	 * @param ctx the parse tree
	 */
	void enterPort(FlatJuniperParser.PortContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#port}.
	 * @param ctx the parse tree
	 */
	void exitPort(FlatJuniperParser.PortContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(FlatJuniperParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(FlatJuniperParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bandwidth}.
	 * @param ctx the parse tree
	 */
	void enterBandwidth(FlatJuniperParser.BandwidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bandwidth}.
	 * @param ctx the parse tree
	 */
	void exitBandwidth(FlatJuniperParser.BandwidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#routing_protocol}.
	 * @param ctx the parse tree
	 */
	void enterRouting_protocol(FlatJuniperParser.Routing_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#routing_protocol}.
	 * @param ctx the parse tree
	 */
	void exitRouting_protocol(FlatJuniperParser.Routing_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sc_literal}.
	 * @param ctx the parse tree
	 */
	void enterSc_literal(FlatJuniperParser.Sc_literalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sc_literal}.
	 * @param ctx the parse tree
	 */
	void exitSc_literal(FlatJuniperParser.Sc_literalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sc_named}.
	 * @param ctx the parse tree
	 */
	void enterSc_named(FlatJuniperParser.Sc_namedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sc_named}.
	 * @param ctx the parse tree
	 */
	void exitSc_named(FlatJuniperParser.Sc_namedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#secret}.
	 * @param ctx the parse tree
	 */
	void enterSecret(FlatJuniperParser.SecretContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#secret}.
	 * @param ctx the parse tree
	 */
	void exitSecret(FlatJuniperParser.SecretContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#standard_community}.
	 * @param ctx the parse tree
	 */
	void enterStandard_community(FlatJuniperParser.Standard_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#standard_community}.
	 * @param ctx the parse tree
	 */
	void exitStandard_community(FlatJuniperParser.Standard_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#string}.
	 * @param ctx the parse tree
	 */
	void enterString(FlatJuniperParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#string}.
	 * @param ctx the parse tree
	 */
	void exitString(FlatJuniperParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#subrange}.
	 * @param ctx the parse tree
	 */
	void enterSubrange(FlatJuniperParser.SubrangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#subrange}.
	 * @param ctx the parse tree
	 */
	void exitSubrange(FlatJuniperParser.SubrangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#threshold}.
	 * @param ctx the parse tree
	 */
	void enterThreshold(FlatJuniperParser.ThresholdContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#threshold}.
	 * @param ctx the parse tree
	 */
	void exitThreshold(FlatJuniperParser.ThresholdContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(FlatJuniperParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(FlatJuniperParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#variable_permissive}.
	 * @param ctx the parse tree
	 */
	void enterVariable_permissive(FlatJuniperParser.Variable_permissiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#variable_permissive}.
	 * @param ctx the parse tree
	 */
	void exitVariable_permissive(FlatJuniperParser.Variable_permissiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#variable_policy}.
	 * @param ctx the parse tree
	 */
	void enterVariable_policy(FlatJuniperParser.Variable_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#variable_policy}.
	 * @param ctx the parse tree
	 */
	void exitVariable_policy(FlatJuniperParser.Variable_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#wildcard}.
	 * @param ctx the parse tree
	 */
	void enterWildcard(FlatJuniperParser.WildcardContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#wildcard}.
	 * @param ctx the parse tree
	 */
	void exitWildcard(FlatJuniperParser.WildcardContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#wildcard_address}.
	 * @param ctx the parse tree
	 */
	void enterWildcard_address(FlatJuniperParser.Wildcard_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#wildcard_address}.
	 * @param ctx the parse tree
	 */
	void exitWildcard_address(FlatJuniperParser.Wildcard_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_protocols}.
	 * @param ctx the parse tree
	 */
	void enterS_protocols(FlatJuniperParser.S_protocolsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_protocols}.
	 * @param ctx the parse tree
	 */
	void exitS_protocols(FlatJuniperParser.S_protocolsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_null}.
	 * @param ctx the parse tree
	 */
	void enterP_null(FlatJuniperParser.P_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_null}.
	 * @param ctx the parse tree
	 */
	void exitP_null(FlatJuniperParser.P_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_advertise_external}.
	 * @param ctx the parse tree
	 */
	void enterB_advertise_external(FlatJuniperParser.B_advertise_externalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_advertise_external}.
	 * @param ctx the parse tree
	 */
	void exitB_advertise_external(FlatJuniperParser.B_advertise_externalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_advertise_inactive}.
	 * @param ctx the parse tree
	 */
	void enterB_advertise_inactive(FlatJuniperParser.B_advertise_inactiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_advertise_inactive}.
	 * @param ctx the parse tree
	 */
	void exitB_advertise_inactive(FlatJuniperParser.B_advertise_inactiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_advertise_peer_as}.
	 * @param ctx the parse tree
	 */
	void enterB_advertise_peer_as(FlatJuniperParser.B_advertise_peer_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_advertise_peer_as}.
	 * @param ctx the parse tree
	 */
	void exitB_advertise_peer_as(FlatJuniperParser.B_advertise_peer_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterB_authentication_algorithm(FlatJuniperParser.B_authentication_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitB_authentication_algorithm(FlatJuniperParser.B_authentication_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_authentication_key}.
	 * @param ctx the parse tree
	 */
	void enterB_authentication_key(FlatJuniperParser.B_authentication_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_authentication_key}.
	 * @param ctx the parse tree
	 */
	void exitB_authentication_key(FlatJuniperParser.B_authentication_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_authentication_key_chain}.
	 * @param ctx the parse tree
	 */
	void enterB_authentication_key_chain(FlatJuniperParser.B_authentication_key_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_authentication_key_chain}.
	 * @param ctx the parse tree
	 */
	void exitB_authentication_key_chain(FlatJuniperParser.B_authentication_key_chainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_allow}.
	 * @param ctx the parse tree
	 */
	void enterB_allow(FlatJuniperParser.B_allowContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_allow}.
	 * @param ctx the parse tree
	 */
	void exitB_allow(FlatJuniperParser.B_allowContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_as_override}.
	 * @param ctx the parse tree
	 */
	void enterB_as_override(FlatJuniperParser.B_as_overrideContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_as_override}.
	 * @param ctx the parse tree
	 */
	void exitB_as_override(FlatJuniperParser.B_as_overrideContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_cluster}.
	 * @param ctx the parse tree
	 */
	void enterB_cluster(FlatJuniperParser.B_clusterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_cluster}.
	 * @param ctx the parse tree
	 */
	void exitB_cluster(FlatJuniperParser.B_clusterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_common}.
	 * @param ctx the parse tree
	 */
	void enterB_common(FlatJuniperParser.B_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_common}.
	 * @param ctx the parse tree
	 */
	void exitB_common(FlatJuniperParser.B_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_damping}.
	 * @param ctx the parse tree
	 */
	void enterB_damping(FlatJuniperParser.B_dampingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_damping}.
	 * @param ctx the parse tree
	 */
	void exitB_damping(FlatJuniperParser.B_dampingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_description}.
	 * @param ctx the parse tree
	 */
	void enterB_description(FlatJuniperParser.B_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_description}.
	 * @param ctx the parse tree
	 */
	void exitB_description(FlatJuniperParser.B_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_disable}.
	 * @param ctx the parse tree
	 */
	void enterB_disable(FlatJuniperParser.B_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_disable}.
	 * @param ctx the parse tree
	 */
	void exitB_disable(FlatJuniperParser.B_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_disable_4byte_as}.
	 * @param ctx the parse tree
	 */
	void enterB_disable_4byte_as(FlatJuniperParser.B_disable_4byte_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_disable_4byte_as}.
	 * @param ctx the parse tree
	 */
	void exitB_disable_4byte_as(FlatJuniperParser.B_disable_4byte_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_drop_path_attributes}.
	 * @param ctx the parse tree
	 */
	void enterB_drop_path_attributes(FlatJuniperParser.B_drop_path_attributesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_drop_path_attributes}.
	 * @param ctx the parse tree
	 */
	void exitB_drop_path_attributes(FlatJuniperParser.B_drop_path_attributesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_enable}.
	 * @param ctx the parse tree
	 */
	void enterB_enable(FlatJuniperParser.B_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_enable}.
	 * @param ctx the parse tree
	 */
	void exitB_enable(FlatJuniperParser.B_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_enforce_first_as}.
	 * @param ctx the parse tree
	 */
	void enterB_enforce_first_as(FlatJuniperParser.B_enforce_first_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_enforce_first_as}.
	 * @param ctx the parse tree
	 */
	void exitB_enforce_first_as(FlatJuniperParser.B_enforce_first_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_export}.
	 * @param ctx the parse tree
	 */
	void enterB_export(FlatJuniperParser.B_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_export}.
	 * @param ctx the parse tree
	 */
	void exitB_export(FlatJuniperParser.B_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_family}.
	 * @param ctx the parse tree
	 */
	void enterB_family(FlatJuniperParser.B_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_family}.
	 * @param ctx the parse tree
	 */
	void exitB_family(FlatJuniperParser.B_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_group}.
	 * @param ctx the parse tree
	 */
	void enterB_group(FlatJuniperParser.B_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_group}.
	 * @param ctx the parse tree
	 */
	void exitB_group(FlatJuniperParser.B_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_import}.
	 * @param ctx the parse tree
	 */
	void enterB_import(FlatJuniperParser.B_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_import}.
	 * @param ctx the parse tree
	 */
	void exitB_import(FlatJuniperParser.B_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_local_address}.
	 * @param ctx the parse tree
	 */
	void enterB_local_address(FlatJuniperParser.B_local_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_local_address}.
	 * @param ctx the parse tree
	 */
	void exitB_local_address(FlatJuniperParser.B_local_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_local_as}.
	 * @param ctx the parse tree
	 */
	void enterB_local_as(FlatJuniperParser.B_local_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_local_as}.
	 * @param ctx the parse tree
	 */
	void exitB_local_as(FlatJuniperParser.B_local_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_multihop}.
	 * @param ctx the parse tree
	 */
	void enterB_multihop(FlatJuniperParser.B_multihopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_multihop}.
	 * @param ctx the parse tree
	 */
	void exitB_multihop(FlatJuniperParser.B_multihopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_multipath}.
	 * @param ctx the parse tree
	 */
	void enterB_multipath(FlatJuniperParser.B_multipathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_multipath}.
	 * @param ctx the parse tree
	 */
	void exitB_multipath(FlatJuniperParser.B_multipathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_neighbor}.
	 * @param ctx the parse tree
	 */
	void enterB_neighbor(FlatJuniperParser.B_neighborContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_neighbor}.
	 * @param ctx the parse tree
	 */
	void exitB_neighbor(FlatJuniperParser.B_neighborContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_no_client_reflect}.
	 * @param ctx the parse tree
	 */
	void enterB_no_client_reflect(FlatJuniperParser.B_no_client_reflectContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_no_client_reflect}.
	 * @param ctx the parse tree
	 */
	void exitB_no_client_reflect(FlatJuniperParser.B_no_client_reflectContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_null}.
	 * @param ctx the parse tree
	 */
	void enterB_null(FlatJuniperParser.B_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_null}.
	 * @param ctx the parse tree
	 */
	void exitB_null(FlatJuniperParser.B_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_passive}.
	 * @param ctx the parse tree
	 */
	void enterB_passive(FlatJuniperParser.B_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_passive}.
	 * @param ctx the parse tree
	 */
	void exitB_passive(FlatJuniperParser.B_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_path_selection}.
	 * @param ctx the parse tree
	 */
	void enterB_path_selection(FlatJuniperParser.B_path_selectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_path_selection}.
	 * @param ctx the parse tree
	 */
	void exitB_path_selection(FlatJuniperParser.B_path_selectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_peer_as}.
	 * @param ctx the parse tree
	 */
	void enterB_peer_as(FlatJuniperParser.B_peer_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_peer_as}.
	 * @param ctx the parse tree
	 */
	void exitB_peer_as(FlatJuniperParser.B_peer_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_remove_private}.
	 * @param ctx the parse tree
	 */
	void enterB_remove_private(FlatJuniperParser.B_remove_privateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_remove_private}.
	 * @param ctx the parse tree
	 */
	void exitB_remove_private(FlatJuniperParser.B_remove_privateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_tcp_mss}.
	 * @param ctx the parse tree
	 */
	void enterB_tcp_mss(FlatJuniperParser.B_tcp_mssContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_tcp_mss}.
	 * @param ctx the parse tree
	 */
	void exitB_tcp_mss(FlatJuniperParser.B_tcp_mssContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#b_type}.
	 * @param ctx the parse tree
	 */
	void enterB_type(FlatJuniperParser.B_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#b_type}.
	 * @param ctx the parse tree
	 */
	void exitB_type(FlatJuniperParser.B_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bf_accepted_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void enterBf_accepted_prefix_limit(FlatJuniperParser.Bf_accepted_prefix_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bf_accepted_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void exitBf_accepted_prefix_limit(FlatJuniperParser.Bf_accepted_prefix_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bf_evpn}.
	 * @param ctx the parse tree
	 */
	void enterBf_evpn(FlatJuniperParser.Bf_evpnContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bf_evpn}.
	 * @param ctx the parse tree
	 */
	void exitBf_evpn(FlatJuniperParser.Bf_evpnContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bf_inet}.
	 * @param ctx the parse tree
	 */
	void enterBf_inet(FlatJuniperParser.Bf_inetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bf_inet}.
	 * @param ctx the parse tree
	 */
	void exitBf_inet(FlatJuniperParser.Bf_inetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bf_inet6}.
	 * @param ctx the parse tree
	 */
	void enterBf_inet6(FlatJuniperParser.Bf_inet6Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bf_inet6}.
	 * @param ctx the parse tree
	 */
	void exitBf_inet6(FlatJuniperParser.Bf_inet6Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bf_null}.
	 * @param ctx the parse tree
	 */
	void enterBf_null(FlatJuniperParser.Bf_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bf_null}.
	 * @param ctx the parse tree
	 */
	void exitBf_null(FlatJuniperParser.Bf_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi_any}.
	 * @param ctx the parse tree
	 */
	void enterBfi_any(FlatJuniperParser.Bfi_anyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi_any}.
	 * @param ctx the parse tree
	 */
	void exitBfi_any(FlatJuniperParser.Bfi_anyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi_flow}.
	 * @param ctx the parse tree
	 */
	void enterBfi_flow(FlatJuniperParser.Bfi_flowContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi_flow}.
	 * @param ctx the parse tree
	 */
	void exitBfi_flow(FlatJuniperParser.Bfi_flowContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi_labeled_unicast}.
	 * @param ctx the parse tree
	 */
	void enterBfi_labeled_unicast(FlatJuniperParser.Bfi_labeled_unicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi_labeled_unicast}.
	 * @param ctx the parse tree
	 */
	void exitBfi_labeled_unicast(FlatJuniperParser.Bfi_labeled_unicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi_multicast}.
	 * @param ctx the parse tree
	 */
	void enterBfi_multicast(FlatJuniperParser.Bfi_multicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi_multicast}.
	 * @param ctx the parse tree
	 */
	void exitBfi_multicast(FlatJuniperParser.Bfi_multicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi_unicast}.
	 * @param ctx the parse tree
	 */
	void enterBfi_unicast(FlatJuniperParser.Bfi_unicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi_unicast}.
	 * @param ctx the parse tree
	 */
	void exitBfi_unicast(FlatJuniperParser.Bfi_unicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi6_any}.
	 * @param ctx the parse tree
	 */
	void enterBfi6_any(FlatJuniperParser.Bfi6_anyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi6_any}.
	 * @param ctx the parse tree
	 */
	void exitBfi6_any(FlatJuniperParser.Bfi6_anyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi6_null}.
	 * @param ctx the parse tree
	 */
	void enterBfi6_null(FlatJuniperParser.Bfi6_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi6_null}.
	 * @param ctx the parse tree
	 */
	void exitBfi6_null(FlatJuniperParser.Bfi6_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi6_unicast}.
	 * @param ctx the parse tree
	 */
	void enterBfi6_unicast(FlatJuniperParser.Bfi6_unicastContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi6_unicast}.
	 * @param ctx the parse tree
	 */
	void exitBfi6_unicast(FlatJuniperParser.Bfi6_unicastContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfi6u_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void enterBfi6u_prefix_limit(FlatJuniperParser.Bfi6u_prefix_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfi6u_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void exitBfi6u_prefix_limit(FlatJuniperParser.Bfi6u_prefix_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiu_add_path}.
	 * @param ctx the parse tree
	 */
	void enterBfiu_add_path(FlatJuniperParser.Bfiu_add_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiu_add_path}.
	 * @param ctx the parse tree
	 */
	void exitBfiu_add_path(FlatJuniperParser.Bfiu_add_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiu_loops}.
	 * @param ctx the parse tree
	 */
	void enterBfiu_loops(FlatJuniperParser.Bfiu_loopsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiu_loops}.
	 * @param ctx the parse tree
	 */
	void exitBfiu_loops(FlatJuniperParser.Bfiu_loopsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiu_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void enterBfiu_prefix_limit(FlatJuniperParser.Bfiu_prefix_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiu_prefix_limit}.
	 * @param ctx the parse tree
	 */
	void exitBfiu_prefix_limit(FlatJuniperParser.Bfiu_prefix_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiu_rib_group}.
	 * @param ctx the parse tree
	 */
	void enterBfiu_rib_group(FlatJuniperParser.Bfiu_rib_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiu_rib_group}.
	 * @param ctx the parse tree
	 */
	void exitBfiu_rib_group(FlatJuniperParser.Bfiu_rib_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiua_receive}.
	 * @param ctx the parse tree
	 */
	void enterBfiua_receive(FlatJuniperParser.Bfiua_receiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiua_receive}.
	 * @param ctx the parse tree
	 */
	void exitBfiua_receive(FlatJuniperParser.Bfiua_receiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiua_send}.
	 * @param ctx the parse tree
	 */
	void enterBfiua_send(FlatJuniperParser.Bfiua_sendContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiua_send}.
	 * @param ctx the parse tree
	 */
	void exitBfiua_send(FlatJuniperParser.Bfiua_sendContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiuas_path_count}.
	 * @param ctx the parse tree
	 */
	void enterBfiuas_path_count(FlatJuniperParser.Bfiuas_path_countContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiuas_path_count}.
	 * @param ctx the parse tree
	 */
	void exitBfiuas_path_count(FlatJuniperParser.Bfiuas_path_countContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bfiuas_prefix_policy}.
	 * @param ctx the parse tree
	 */
	void enterBfiuas_prefix_policy(FlatJuniperParser.Bfiuas_prefix_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bfiuas_prefix_policy}.
	 * @param ctx the parse tree
	 */
	void exitBfiuas_prefix_policy(FlatJuniperParser.Bfiuas_prefix_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_alias}.
	 * @param ctx the parse tree
	 */
	void enterBl_alias(FlatJuniperParser.Bl_aliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_alias}.
	 * @param ctx the parse tree
	 */
	void exitBl_alias(FlatJuniperParser.Bl_aliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_common}.
	 * @param ctx the parse tree
	 */
	void enterBl_common(FlatJuniperParser.Bl_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_common}.
	 * @param ctx the parse tree
	 */
	void exitBl_common(FlatJuniperParser.Bl_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_loops}.
	 * @param ctx the parse tree
	 */
	void enterBl_loops(FlatJuniperParser.Bl_loopsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_loops}.
	 * @param ctx the parse tree
	 */
	void exitBl_loops(FlatJuniperParser.Bl_loopsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_number}.
	 * @param ctx the parse tree
	 */
	void enterBl_number(FlatJuniperParser.Bl_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_number}.
	 * @param ctx the parse tree
	 */
	void exitBl_number(FlatJuniperParser.Bl_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_private}.
	 * @param ctx the parse tree
	 */
	void enterBl_private(FlatJuniperParser.Bl_privateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_private}.
	 * @param ctx the parse tree
	 */
	void exitBl_private(FlatJuniperParser.Bl_privateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bl_no_prepend_global_as}.
	 * @param ctx the parse tree
	 */
	void enterBl_no_prepend_global_as(FlatJuniperParser.Bl_no_prepend_global_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bl_no_prepend_global_as}.
	 * @param ctx the parse tree
	 */
	void exitBl_no_prepend_global_as(FlatJuniperParser.Bl_no_prepend_global_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bm_no_nexthop_change}.
	 * @param ctx the parse tree
	 */
	void enterBm_no_nexthop_change(FlatJuniperParser.Bm_no_nexthop_changeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bm_no_nexthop_change}.
	 * @param ctx the parse tree
	 */
	void exitBm_no_nexthop_change(FlatJuniperParser.Bm_no_nexthop_changeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bm_ttl}.
	 * @param ctx the parse tree
	 */
	void enterBm_ttl(FlatJuniperParser.Bm_ttlContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bm_ttl}.
	 * @param ctx the parse tree
	 */
	void exitBm_ttl(FlatJuniperParser.Bm_ttlContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bpa_as}.
	 * @param ctx the parse tree
	 */
	void enterBpa_as(FlatJuniperParser.Bpa_asContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bpa_as}.
	 * @param ctx the parse tree
	 */
	void exitBpa_as(FlatJuniperParser.Bpa_asContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bps_always_compare_med}.
	 * @param ctx the parse tree
	 */
	void enterBps_always_compare_med(FlatJuniperParser.Bps_always_compare_medContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bps_always_compare_med}.
	 * @param ctx the parse tree
	 */
	void exitBps_always_compare_med(FlatJuniperParser.Bps_always_compare_medContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#bps_external_router_id}.
	 * @param ctx the parse tree
	 */
	void enterBps_external_router_id(FlatJuniperParser.Bps_external_router_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#bps_external_router_id}.
	 * @param ctx the parse tree
	 */
	void exitBps_external_router_id(FlatJuniperParser.Bps_external_router_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_bgp}.
	 * @param ctx the parse tree
	 */
	void enterP_bgp(FlatJuniperParser.P_bgpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_bgp}.
	 * @param ctx the parse tree
	 */
	void exitP_bgp(FlatJuniperParser.P_bgpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#e_default_gateway}.
	 * @param ctx the parse tree
	 */
	void enterE_default_gateway(FlatJuniperParser.E_default_gatewayContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#e_default_gateway}.
	 * @param ctx the parse tree
	 */
	void exitE_default_gateway(FlatJuniperParser.E_default_gatewayContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#e_encapsulation}.
	 * @param ctx the parse tree
	 */
	void enterE_encapsulation(FlatJuniperParser.E_encapsulationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#e_encapsulation}.
	 * @param ctx the parse tree
	 */
	void exitE_encapsulation(FlatJuniperParser.E_encapsulationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#e_extended_vni_list}.
	 * @param ctx the parse tree
	 */
	void enterE_extended_vni_list(FlatJuniperParser.E_extended_vni_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#e_extended_vni_list}.
	 * @param ctx the parse tree
	 */
	void exitE_extended_vni_list(FlatJuniperParser.E_extended_vni_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#e_multicast_mode}.
	 * @param ctx the parse tree
	 */
	void enterE_multicast_mode(FlatJuniperParser.E_multicast_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#e_multicast_mode}.
	 * @param ctx the parse tree
	 */
	void exitE_multicast_mode(FlatJuniperParser.E_multicast_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#e_vni_options}.
	 * @param ctx the parse tree
	 */
	void enterE_vni_options(FlatJuniperParser.E_vni_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#e_vni_options}.
	 * @param ctx the parse tree
	 */
	void exitE_vni_options(FlatJuniperParser.E_vni_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evo_designated_forwarder_election_hold_time}.
	 * @param ctx the parse tree
	 */
	void enterEvo_designated_forwarder_election_hold_time(FlatJuniperParser.Evo_designated_forwarder_election_hold_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evo_designated_forwarder_election_hold_time}.
	 * @param ctx the parse tree
	 */
	void exitEvo_designated_forwarder_election_hold_time(FlatJuniperParser.Evo_designated_forwarder_election_hold_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evo_vrf_target}.
	 * @param ctx the parse tree
	 */
	void enterEvo_vrf_target(FlatJuniperParser.Evo_vrf_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evo_vrf_target}.
	 * @param ctx the parse tree
	 */
	void exitEvo_vrf_target(FlatJuniperParser.Evo_vrf_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evovt_auto}.
	 * @param ctx the parse tree
	 */
	void enterEvovt_auto(FlatJuniperParser.Evovt_autoContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evovt_auto}.
	 * @param ctx the parse tree
	 */
	void exitEvovt_auto(FlatJuniperParser.Evovt_autoContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evovt_community}.
	 * @param ctx the parse tree
	 */
	void enterEvovt_community(FlatJuniperParser.Evovt_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evovt_community}.
	 * @param ctx the parse tree
	 */
	void exitEvovt_community(FlatJuniperParser.Evovt_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evovt_export}.
	 * @param ctx the parse tree
	 */
	void enterEvovt_export(FlatJuniperParser.Evovt_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evovt_export}.
	 * @param ctx the parse tree
	 */
	void exitEvovt_export(FlatJuniperParser.Evovt_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#evovt_import}.
	 * @param ctx the parse tree
	 */
	void enterEvovt_import(FlatJuniperParser.Evovt_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#evovt_import}.
	 * @param ctx the parse tree
	 */
	void exitEvovt_import(FlatJuniperParser.Evovt_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_evpn}.
	 * @param ctx the parse tree
	 */
	void enterP_evpn(FlatJuniperParser.P_evpnContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_evpn}.
	 * @param ctx the parse tree
	 */
	void exitP_evpn(FlatJuniperParser.P_evpnContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#vt_community}.
	 * @param ctx the parse tree
	 */
	void enterVt_community(FlatJuniperParser.Vt_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#vt_community}.
	 * @param ctx the parse tree
	 */
	void exitVt_community(FlatJuniperParser.Vt_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#hello_authentication_type}.
	 * @param ctx the parse tree
	 */
	void enterHello_authentication_type(FlatJuniperParser.Hello_authentication_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#hello_authentication_type}.
	 * @param ctx the parse tree
	 */
	void exitHello_authentication_type(FlatJuniperParser.Hello_authentication_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_export}.
	 * @param ctx the parse tree
	 */
	void enterIs_export(FlatJuniperParser.Is_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_export}.
	 * @param ctx the parse tree
	 */
	void exitIs_export(FlatJuniperParser.Is_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_interface}.
	 * @param ctx the parse tree
	 */
	void enterIs_interface(FlatJuniperParser.Is_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_interface}.
	 * @param ctx the parse tree
	 */
	void exitIs_interface(FlatJuniperParser.Is_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_level}.
	 * @param ctx the parse tree
	 */
	void enterIs_level(FlatJuniperParser.Is_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_level}.
	 * @param ctx the parse tree
	 */
	void exitIs_level(FlatJuniperParser.Is_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_no_ipv4_routing}.
	 * @param ctx the parse tree
	 */
	void enterIs_no_ipv4_routing(FlatJuniperParser.Is_no_ipv4_routingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_no_ipv4_routing}.
	 * @param ctx the parse tree
	 */
	void exitIs_no_ipv4_routing(FlatJuniperParser.Is_no_ipv4_routingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_null}.
	 * @param ctx the parse tree
	 */
	void enterIs_null(FlatJuniperParser.Is_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_null}.
	 * @param ctx the parse tree
	 */
	void exitIs_null(FlatJuniperParser.Is_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_overload}.
	 * @param ctx the parse tree
	 */
	void enterIs_overload(FlatJuniperParser.Is_overloadContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_overload}.
	 * @param ctx the parse tree
	 */
	void exitIs_overload(FlatJuniperParser.Is_overloadContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_reference_bandwidth}.
	 * @param ctx the parse tree
	 */
	void enterIs_reference_bandwidth(FlatJuniperParser.Is_reference_bandwidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_reference_bandwidth}.
	 * @param ctx the parse tree
	 */
	void exitIs_reference_bandwidth(FlatJuniperParser.Is_reference_bandwidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_rib_group}.
	 * @param ctx the parse tree
	 */
	void enterIs_rib_group(FlatJuniperParser.Is_rib_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_rib_group}.
	 * @param ctx the parse tree
	 */
	void exitIs_rib_group(FlatJuniperParser.Is_rib_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#is_traffic_engineering}.
	 * @param ctx the parse tree
	 */
	void enterIs_traffic_engineering(FlatJuniperParser.Is_traffic_engineeringContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#is_traffic_engineering}.
	 * @param ctx the parse tree
	 */
	void exitIs_traffic_engineering(FlatJuniperParser.Is_traffic_engineeringContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_bfd_liveness_detection}.
	 * @param ctx the parse tree
	 */
	void enterIsi_bfd_liveness_detection(FlatJuniperParser.Isi_bfd_liveness_detectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_bfd_liveness_detection}.
	 * @param ctx the parse tree
	 */
	void exitIsi_bfd_liveness_detection(FlatJuniperParser.Isi_bfd_liveness_detectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_disable}.
	 * @param ctx the parse tree
	 */
	void enterIsi_disable(FlatJuniperParser.Isi_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_disable}.
	 * @param ctx the parse tree
	 */
	void exitIsi_disable(FlatJuniperParser.Isi_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_level}.
	 * @param ctx the parse tree
	 */
	void enterIsi_level(FlatJuniperParser.Isi_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_level}.
	 * @param ctx the parse tree
	 */
	void exitIsi_level(FlatJuniperParser.Isi_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_null}.
	 * @param ctx the parse tree
	 */
	void enterIsi_null(FlatJuniperParser.Isi_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_null}.
	 * @param ctx the parse tree
	 */
	void exitIsi_null(FlatJuniperParser.Isi_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_passive}.
	 * @param ctx the parse tree
	 */
	void enterIsi_passive(FlatJuniperParser.Isi_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_passive}.
	 * @param ctx the parse tree
	 */
	void exitIsi_passive(FlatJuniperParser.Isi_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isi_point_to_point}.
	 * @param ctx the parse tree
	 */
	void enterIsi_point_to_point(FlatJuniperParser.Isi_point_to_pointContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isi_point_to_point}.
	 * @param ctx the parse tree
	 */
	void exitIsi_point_to_point(FlatJuniperParser.Isi_point_to_pointContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isib_minimum_interval}.
	 * @param ctx the parse tree
	 */
	void enterIsib_minimum_interval(FlatJuniperParser.Isib_minimum_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isib_minimum_interval}.
	 * @param ctx the parse tree
	 */
	void exitIsib_minimum_interval(FlatJuniperParser.Isib_minimum_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isib_multiplier}.
	 * @param ctx the parse tree
	 */
	void enterIsib_multiplier(FlatJuniperParser.Isib_multiplierContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isib_multiplier}.
	 * @param ctx the parse tree
	 */
	void exitIsib_multiplier(FlatJuniperParser.Isib_multiplierContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_disable}.
	 * @param ctx the parse tree
	 */
	void enterIsil_disable(FlatJuniperParser.Isil_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_disable}.
	 * @param ctx the parse tree
	 */
	void exitIsil_disable(FlatJuniperParser.Isil_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_enable}.
	 * @param ctx the parse tree
	 */
	void enterIsil_enable(FlatJuniperParser.Isil_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_enable}.
	 * @param ctx the parse tree
	 */
	void exitIsil_enable(FlatJuniperParser.Isil_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_hello_authentication_key}.
	 * @param ctx the parse tree
	 */
	void enterIsil_hello_authentication_key(FlatJuniperParser.Isil_hello_authentication_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_hello_authentication_key}.
	 * @param ctx the parse tree
	 */
	void exitIsil_hello_authentication_key(FlatJuniperParser.Isil_hello_authentication_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_hello_authentication_type}.
	 * @param ctx the parse tree
	 */
	void enterIsil_hello_authentication_type(FlatJuniperParser.Isil_hello_authentication_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_hello_authentication_type}.
	 * @param ctx the parse tree
	 */
	void exitIsil_hello_authentication_type(FlatJuniperParser.Isil_hello_authentication_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_hello_interval}.
	 * @param ctx the parse tree
	 */
	void enterIsil_hello_interval(FlatJuniperParser.Isil_hello_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_hello_interval}.
	 * @param ctx the parse tree
	 */
	void exitIsil_hello_interval(FlatJuniperParser.Isil_hello_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_hold_time}.
	 * @param ctx the parse tree
	 */
	void enterIsil_hold_time(FlatJuniperParser.Isil_hold_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_hold_time}.
	 * @param ctx the parse tree
	 */
	void exitIsil_hold_time(FlatJuniperParser.Isil_hold_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_metric}.
	 * @param ctx the parse tree
	 */
	void enterIsil_metric(FlatJuniperParser.Isil_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_metric}.
	 * @param ctx the parse tree
	 */
	void exitIsil_metric(FlatJuniperParser.Isil_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_passive}.
	 * @param ctx the parse tree
	 */
	void enterIsil_passive(FlatJuniperParser.Isil_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_passive}.
	 * @param ctx the parse tree
	 */
	void exitIsil_passive(FlatJuniperParser.Isil_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_priority}.
	 * @param ctx the parse tree
	 */
	void enterIsil_priority(FlatJuniperParser.Isil_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_priority}.
	 * @param ctx the parse tree
	 */
	void exitIsil_priority(FlatJuniperParser.Isil_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isil_te_metric}.
	 * @param ctx the parse tree
	 */
	void enterIsil_te_metric(FlatJuniperParser.Isil_te_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isil_te_metric}.
	 * @param ctx the parse tree
	 */
	void exitIsil_te_metric(FlatJuniperParser.Isil_te_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isl_disable}.
	 * @param ctx the parse tree
	 */
	void enterIsl_disable(FlatJuniperParser.Isl_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isl_disable}.
	 * @param ctx the parse tree
	 */
	void exitIsl_disable(FlatJuniperParser.Isl_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isl_enable}.
	 * @param ctx the parse tree
	 */
	void enterIsl_enable(FlatJuniperParser.Isl_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isl_enable}.
	 * @param ctx the parse tree
	 */
	void exitIsl_enable(FlatJuniperParser.Isl_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isl_null}.
	 * @param ctx the parse tree
	 */
	void enterIsl_null(FlatJuniperParser.Isl_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isl_null}.
	 * @param ctx the parse tree
	 */
	void exitIsl_null(FlatJuniperParser.Isl_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#isl_wide_metrics_only}.
	 * @param ctx the parse tree
	 */
	void enterIsl_wide_metrics_only(FlatJuniperParser.Isl_wide_metrics_onlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#isl_wide_metrics_only}.
	 * @param ctx the parse tree
	 */
	void exitIsl_wide_metrics_only(FlatJuniperParser.Isl_wide_metrics_onlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ist_credibility_protocol_preference}.
	 * @param ctx the parse tree
	 */
	void enterIst_credibility_protocol_preference(FlatJuniperParser.Ist_credibility_protocol_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ist_credibility_protocol_preference}.
	 * @param ctx the parse tree
	 */
	void exitIst_credibility_protocol_preference(FlatJuniperParser.Ist_credibility_protocol_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#iso_timeout}.
	 * @param ctx the parse tree
	 */
	void enterIso_timeout(FlatJuniperParser.Iso_timeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#iso_timeout}.
	 * @param ctx the parse tree
	 */
	void exitIso_timeout(FlatJuniperParser.Iso_timeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ist_family_shortcuts}.
	 * @param ctx the parse tree
	 */
	void enterIst_family_shortcuts(FlatJuniperParser.Ist_family_shortcutsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ist_family_shortcuts}.
	 * @param ctx the parse tree
	 */
	void exitIst_family_shortcuts(FlatJuniperParser.Ist_family_shortcutsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ist_multipath}.
	 * @param ctx the parse tree
	 */
	void enterIst_multipath(FlatJuniperParser.Ist_multipathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ist_multipath}.
	 * @param ctx the parse tree
	 */
	void exitIst_multipath(FlatJuniperParser.Ist_multipathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_isis}.
	 * @param ctx the parse tree
	 */
	void enterP_isis(FlatJuniperParser.P_isisContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_isis}.
	 * @param ctx the parse tree
	 */
	void exitP_isis(FlatJuniperParser.P_isisContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#c_interface_switch}.
	 * @param ctx the parse tree
	 */
	void enterC_interface_switch(FlatJuniperParser.C_interface_switchContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#c_interface_switch}.
	 * @param ctx the parse tree
	 */
	void exitC_interface_switch(FlatJuniperParser.C_interface_switchContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ci_interface}.
	 * @param ctx the parse tree
	 */
	void enterCi_interface(FlatJuniperParser.Ci_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ci_interface}.
	 * @param ctx the parse tree
	 */
	void exitCi_interface(FlatJuniperParser.Ci_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_connections}.
	 * @param ctx the parse tree
	 */
	void enterP_connections(FlatJuniperParser.P_connectionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_connections}.
	 * @param ctx the parse tree
	 */
	void exitP_connections(FlatJuniperParser.P_connectionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_mpls}.
	 * @param ctx the parse tree
	 */
	void enterP_mpls(FlatJuniperParser.P_mplsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_mpls}.
	 * @param ctx the parse tree
	 */
	void exitP_mpls(FlatJuniperParser.P_mplsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_area}.
	 * @param ctx the parse tree
	 */
	void enterO_area(FlatJuniperParser.O_areaContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_area}.
	 * @param ctx the parse tree
	 */
	void exitO_area(FlatJuniperParser.O_areaContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_common}.
	 * @param ctx the parse tree
	 */
	void enterO_common(FlatJuniperParser.O_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_common}.
	 * @param ctx the parse tree
	 */
	void exitO_common(FlatJuniperParser.O_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_disable}.
	 * @param ctx the parse tree
	 */
	void enterO_disable(FlatJuniperParser.O_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_disable}.
	 * @param ctx the parse tree
	 */
	void exitO_disable(FlatJuniperParser.O_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_enable}.
	 * @param ctx the parse tree
	 */
	void enterO_enable(FlatJuniperParser.O_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_enable}.
	 * @param ctx the parse tree
	 */
	void exitO_enable(FlatJuniperParser.O_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_export}.
	 * @param ctx the parse tree
	 */
	void enterO_export(FlatJuniperParser.O_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_export}.
	 * @param ctx the parse tree
	 */
	void exitO_export(FlatJuniperParser.O_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_external_preference}.
	 * @param ctx the parse tree
	 */
	void enterO_external_preference(FlatJuniperParser.O_external_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_external_preference}.
	 * @param ctx the parse tree
	 */
	void exitO_external_preference(FlatJuniperParser.O_external_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_import}.
	 * @param ctx the parse tree
	 */
	void enterO_import(FlatJuniperParser.O_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_import}.
	 * @param ctx the parse tree
	 */
	void exitO_import(FlatJuniperParser.O_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_no_active_backbone}.
	 * @param ctx the parse tree
	 */
	void enterO_no_active_backbone(FlatJuniperParser.O_no_active_backboneContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_no_active_backbone}.
	 * @param ctx the parse tree
	 */
	void exitO_no_active_backbone(FlatJuniperParser.O_no_active_backboneContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_null}.
	 * @param ctx the parse tree
	 */
	void enterO_null(FlatJuniperParser.O_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_null}.
	 * @param ctx the parse tree
	 */
	void exitO_null(FlatJuniperParser.O_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_reference_bandwidth}.
	 * @param ctx the parse tree
	 */
	void enterO_reference_bandwidth(FlatJuniperParser.O_reference_bandwidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_reference_bandwidth}.
	 * @param ctx the parse tree
	 */
	void exitO_reference_bandwidth(FlatJuniperParser.O_reference_bandwidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_rib_group}.
	 * @param ctx the parse tree
	 */
	void enterO_rib_group(FlatJuniperParser.O_rib_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_rib_group}.
	 * @param ctx the parse tree
	 */
	void exitO_rib_group(FlatJuniperParser.O_rib_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#o_traffic_engineering}.
	 * @param ctx the parse tree
	 */
	void enterO_traffic_engineering(FlatJuniperParser.O_traffic_engineeringContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#o_traffic_engineering}.
	 * @param ctx the parse tree
	 */
	void exitO_traffic_engineering(FlatJuniperParser.O_traffic_engineeringContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_area_range}.
	 * @param ctx the parse tree
	 */
	void enterOa_area_range(FlatJuniperParser.Oa_area_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_area_range}.
	 * @param ctx the parse tree
	 */
	void exitOa_area_range(FlatJuniperParser.Oa_area_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_interface}.
	 * @param ctx the parse tree
	 */
	void enterOa_interface(FlatJuniperParser.Oa_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_interface}.
	 * @param ctx the parse tree
	 */
	void exitOa_interface(FlatJuniperParser.Oa_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_label_switched_path}.
	 * @param ctx the parse tree
	 */
	void enterOa_label_switched_path(FlatJuniperParser.Oa_label_switched_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_label_switched_path}.
	 * @param ctx the parse tree
	 */
	void exitOa_label_switched_path(FlatJuniperParser.Oa_label_switched_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_nssa}.
	 * @param ctx the parse tree
	 */
	void enterOa_nssa(FlatJuniperParser.Oa_nssaContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_nssa}.
	 * @param ctx the parse tree
	 */
	void exitOa_nssa(FlatJuniperParser.Oa_nssaContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_null}.
	 * @param ctx the parse tree
	 */
	void enterOa_null(FlatJuniperParser.Oa_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_null}.
	 * @param ctx the parse tree
	 */
	void exitOa_null(FlatJuniperParser.Oa_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oa_stub}.
	 * @param ctx the parse tree
	 */
	void enterOa_stub(FlatJuniperParser.Oa_stubContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oa_stub}.
	 * @param ctx the parse tree
	 */
	void exitOa_stub(FlatJuniperParser.Oa_stubContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oaa_override_metric}.
	 * @param ctx the parse tree
	 */
	void enterOaa_override_metric(FlatJuniperParser.Oaa_override_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oaa_override_metric}.
	 * @param ctx the parse tree
	 */
	void exitOaa_override_metric(FlatJuniperParser.Oaa_override_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oaa_restrict}.
	 * @param ctx the parse tree
	 */
	void enterOaa_restrict(FlatJuniperParser.Oaa_restrictContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oaa_restrict}.
	 * @param ctx the parse tree
	 */
	void exitOaa_restrict(FlatJuniperParser.Oaa_restrictContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_dead_interval}.
	 * @param ctx the parse tree
	 */
	void enterOai_dead_interval(FlatJuniperParser.Oai_dead_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_dead_interval}.
	 * @param ctx the parse tree
	 */
	void exitOai_dead_interval(FlatJuniperParser.Oai_dead_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_disable}.
	 * @param ctx the parse tree
	 */
	void enterOai_disable(FlatJuniperParser.Oai_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_disable}.
	 * @param ctx the parse tree
	 */
	void exitOai_disable(FlatJuniperParser.Oai_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_enable}.
	 * @param ctx the parse tree
	 */
	void enterOai_enable(FlatJuniperParser.Oai_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_enable}.
	 * @param ctx the parse tree
	 */
	void exitOai_enable(FlatJuniperParser.Oai_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_hello_interval}.
	 * @param ctx the parse tree
	 */
	void enterOai_hello_interval(FlatJuniperParser.Oai_hello_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_hello_interval}.
	 * @param ctx the parse tree
	 */
	void exitOai_hello_interval(FlatJuniperParser.Oai_hello_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_interface_type}.
	 * @param ctx the parse tree
	 */
	void enterOai_interface_type(FlatJuniperParser.Oai_interface_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_interface_type}.
	 * @param ctx the parse tree
	 */
	void exitOai_interface_type(FlatJuniperParser.Oai_interface_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_ldp_synchronization}.
	 * @param ctx the parse tree
	 */
	void enterOai_ldp_synchronization(FlatJuniperParser.Oai_ldp_synchronizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_ldp_synchronization}.
	 * @param ctx the parse tree
	 */
	void exitOai_ldp_synchronization(FlatJuniperParser.Oai_ldp_synchronizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_link_protection}.
	 * @param ctx the parse tree
	 */
	void enterOai_link_protection(FlatJuniperParser.Oai_link_protectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_link_protection}.
	 * @param ctx the parse tree
	 */
	void exitOai_link_protection(FlatJuniperParser.Oai_link_protectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_ls_disable}.
	 * @param ctx the parse tree
	 */
	void enterOai_ls_disable(FlatJuniperParser.Oai_ls_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_ls_disable}.
	 * @param ctx the parse tree
	 */
	void exitOai_ls_disable(FlatJuniperParser.Oai_ls_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_ls_hold_time}.
	 * @param ctx the parse tree
	 */
	void enterOai_ls_hold_time(FlatJuniperParser.Oai_ls_hold_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_ls_hold_time}.
	 * @param ctx the parse tree
	 */
	void exitOai_ls_hold_time(FlatJuniperParser.Oai_ls_hold_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_metric}.
	 * @param ctx the parse tree
	 */
	void enterOai_metric(FlatJuniperParser.Oai_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_metric}.
	 * @param ctx the parse tree
	 */
	void exitOai_metric(FlatJuniperParser.Oai_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_neighbor}.
	 * @param ctx the parse tree
	 */
	void enterOai_neighbor(FlatJuniperParser.Oai_neighborContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_neighbor}.
	 * @param ctx the parse tree
	 */
	void exitOai_neighbor(FlatJuniperParser.Oai_neighborContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_null}.
	 * @param ctx the parse tree
	 */
	void enterOai_null(FlatJuniperParser.Oai_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_null}.
	 * @param ctx the parse tree
	 */
	void exitOai_null(FlatJuniperParser.Oai_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_passive}.
	 * @param ctx the parse tree
	 */
	void enterOai_passive(FlatJuniperParser.Oai_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_passive}.
	 * @param ctx the parse tree
	 */
	void exitOai_passive(FlatJuniperParser.Oai_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_priority}.
	 * @param ctx the parse tree
	 */
	void enterOai_priority(FlatJuniperParser.Oai_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_priority}.
	 * @param ctx the parse tree
	 */
	void exitOai_priority(FlatJuniperParser.Oai_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oai_te_metric}.
	 * @param ctx the parse tree
	 */
	void enterOai_te_metric(FlatJuniperParser.Oai_te_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oai_te_metric}.
	 * @param ctx the parse tree
	 */
	void exitOai_te_metric(FlatJuniperParser.Oai_te_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oal_metric}.
	 * @param ctx the parse tree
	 */
	void enterOal_metric(FlatJuniperParser.Oal_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oal_metric}.
	 * @param ctx the parse tree
	 */
	void exitOal_metric(FlatJuniperParser.Oal_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oan_area_range}.
	 * @param ctx the parse tree
	 */
	void enterOan_area_range(FlatJuniperParser.Oan_area_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oan_area_range}.
	 * @param ctx the parse tree
	 */
	void exitOan_area_range(FlatJuniperParser.Oan_area_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oan_default_lsa}.
	 * @param ctx the parse tree
	 */
	void enterOan_default_lsa(FlatJuniperParser.Oan_default_lsaContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oan_default_lsa}.
	 * @param ctx the parse tree
	 */
	void exitOan_default_lsa(FlatJuniperParser.Oan_default_lsaContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oan_no_summaries}.
	 * @param ctx the parse tree
	 */
	void enterOan_no_summaries(FlatJuniperParser.Oan_no_summariesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oan_no_summaries}.
	 * @param ctx the parse tree
	 */
	void exitOan_no_summaries(FlatJuniperParser.Oan_no_summariesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oand_default_metric}.
	 * @param ctx the parse tree
	 */
	void enterOand_default_metric(FlatJuniperParser.Oand_default_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oand_default_metric}.
	 * @param ctx the parse tree
	 */
	void exitOand_default_metric(FlatJuniperParser.Oand_default_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oand_metric_type}.
	 * @param ctx the parse tree
	 */
	void enterOand_metric_type(FlatJuniperParser.Oand_metric_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oand_metric_type}.
	 * @param ctx the parse tree
	 */
	void exitOand_metric_type(FlatJuniperParser.Oand_metric_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oand_type_7}.
	 * @param ctx the parse tree
	 */
	void enterOand_type_7(FlatJuniperParser.Oand_type_7Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oand_type_7}.
	 * @param ctx the parse tree
	 */
	void exitOand_type_7(FlatJuniperParser.Oand_type_7Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oas_no_summaries}.
	 * @param ctx the parse tree
	 */
	void enterOas_no_summaries(FlatJuniperParser.Oas_no_summariesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oas_no_summaries}.
	 * @param ctx the parse tree
	 */
	void exitOas_no_summaries(FlatJuniperParser.Oas_no_summariesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#oas_default_metric}.
	 * @param ctx the parse tree
	 */
	void enterOas_default_metric(FlatJuniperParser.Oas_default_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#oas_default_metric}.
	 * @param ctx the parse tree
	 */
	void exitOas_default_metric(FlatJuniperParser.Oas_default_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ospf_interface_type}.
	 * @param ctx the parse tree
	 */
	void enterOspf_interface_type(FlatJuniperParser.Ospf_interface_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ospf_interface_type}.
	 * @param ctx the parse tree
	 */
	void exitOspf_interface_type(FlatJuniperParser.Ospf_interface_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ot_credibility_protocol_preference}.
	 * @param ctx the parse tree
	 */
	void enterOt_credibility_protocol_preference(FlatJuniperParser.Ot_credibility_protocol_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ot_credibility_protocol_preference}.
	 * @param ctx the parse tree
	 */
	void exitOt_credibility_protocol_preference(FlatJuniperParser.Ot_credibility_protocol_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ot_shortcuts}.
	 * @param ctx the parse tree
	 */
	void enterOt_shortcuts(FlatJuniperParser.Ot_shortcutsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ot_shortcuts}.
	 * @param ctx the parse tree
	 */
	void exitOt_shortcuts(FlatJuniperParser.Ot_shortcutsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_ospf}.
	 * @param ctx the parse tree
	 */
	void enterP_ospf(FlatJuniperParser.P_ospfContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_ospf}.
	 * @param ctx the parse tree
	 */
	void exitP_ospf(FlatJuniperParser.P_ospfContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#p_ospf3}.
	 * @param ctx the parse tree
	 */
	void enterP_ospf3(FlatJuniperParser.P_ospf3Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#p_ospf3}.
	 * @param ctx the parse tree
	 */
	void exitP_ospf3(FlatJuniperParser.P_ospf3Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fab_aliases}.
	 * @param ctx the parse tree
	 */
	void enterFab_aliases(FlatJuniperParser.Fab_aliasesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fab_aliases}.
	 * @param ctx the parse tree
	 */
	void exitFab_aliases(FlatJuniperParser.Fab_aliasesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fab_resources}.
	 * @param ctx the parse tree
	 */
	void enterFab_resources(FlatJuniperParser.Fab_resourcesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fab_resources}.
	 * @param ctx the parse tree
	 */
	void exitFab_resources(FlatJuniperParser.Fab_resourcesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#faba_interconnect_device}.
	 * @param ctx the parse tree
	 */
	void enterFaba_interconnect_device(FlatJuniperParser.Faba_interconnect_deviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#faba_interconnect_device}.
	 * @param ctx the parse tree
	 */
	void exitFaba_interconnect_device(FlatJuniperParser.Faba_interconnect_deviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#faba_node_device}.
	 * @param ctx the parse tree
	 */
	void enterFaba_node_device(FlatJuniperParser.Faba_node_deviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#faba_node_device}.
	 * @param ctx the parse tree
	 */
	void exitFaba_node_device(FlatJuniperParser.Faba_node_deviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fabr_node_group}.
	 * @param ctx the parse tree
	 */
	void enterFabr_node_group(FlatJuniperParser.Fabr_node_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fabr_node_group}.
	 * @param ctx the parse tree
	 */
	void exitFabr_node_group(FlatJuniperParser.Fabr_node_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fabrn_network_domain}.
	 * @param ctx the parse tree
	 */
	void enterFabrn_network_domain(FlatJuniperParser.Fabrn_network_domainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fabrn_network_domain}.
	 * @param ctx the parse tree
	 */
	void exitFabrn_network_domain(FlatJuniperParser.Fabrn_network_domainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fabrn_node_device}.
	 * @param ctx the parse tree
	 */
	void enterFabrn_node_device(FlatJuniperParser.Fabrn_node_deviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fabrn_node_device}.
	 * @param ctx the parse tree
	 */
	void exitFabrn_node_device(FlatJuniperParser.Fabrn_node_deviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_fabric}.
	 * @param ctx the parse tree
	 */
	void enterS_fabric(FlatJuniperParser.S_fabricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_fabric}.
	 * @param ctx the parse tree
	 */
	void exitS_fabric(FlatJuniperParser.S_fabricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#f_common}.
	 * @param ctx the parse tree
	 */
	void enterF_common(FlatJuniperParser.F_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#f_common}.
	 * @param ctx the parse tree
	 */
	void exitF_common(FlatJuniperParser.F_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#f_family}.
	 * @param ctx the parse tree
	 */
	void enterF_family(FlatJuniperParser.F_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#f_family}.
	 * @param ctx the parse tree
	 */
	void exitF_family(FlatJuniperParser.F_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#f_filter}.
	 * @param ctx the parse tree
	 */
	void enterF_filter(FlatJuniperParser.F_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#f_filter}.
	 * @param ctx the parse tree
	 */
	void exitF_filter(FlatJuniperParser.F_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#f_null}.
	 * @param ctx the parse tree
	 */
	void enterF_null(FlatJuniperParser.F_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#f_null}.
	 * @param ctx the parse tree
	 */
	void exitF_null(FlatJuniperParser.F_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ff_interface_specific}.
	 * @param ctx the parse tree
	 */
	void enterFf_interface_specific(FlatJuniperParser.Ff_interface_specificContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ff_interface_specific}.
	 * @param ctx the parse tree
	 */
	void exitFf_interface_specific(FlatJuniperParser.Ff_interface_specificContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ff_term}.
	 * @param ctx the parse tree
	 */
	void enterFf_term(FlatJuniperParser.Ff_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ff_term}.
	 * @param ctx the parse tree
	 */
	void exitFf_term(FlatJuniperParser.Ff_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fft_from}.
	 * @param ctx the parse tree
	 */
	void enterFft_from(FlatJuniperParser.Fft_fromContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fft_from}.
	 * @param ctx the parse tree
	 */
	void exitFft_from(FlatJuniperParser.Fft_fromContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fft_then}.
	 * @param ctx the parse tree
	 */
	void enterFft_then(FlatJuniperParser.Fft_thenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fft_then}.
	 * @param ctx the parse tree
	 */
	void exitFft_then(FlatJuniperParser.Fft_thenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftfa_address_mask_prefix}.
	 * @param ctx the parse tree
	 */
	void enterFftfa_address_mask_prefix(FlatJuniperParser.Fftfa_address_mask_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftfa_address_mask_prefix}.
	 * @param ctx the parse tree
	 */
	void exitFftfa_address_mask_prefix(FlatJuniperParser.Fftfa_address_mask_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_address}.
	 * @param ctx the parse tree
	 */
	void enterFftf_address(FlatJuniperParser.Fftf_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_address}.
	 * @param ctx the parse tree
	 */
	void exitFftf_address(FlatJuniperParser.Fftf_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_destination_address}.
	 * @param ctx the parse tree
	 */
	void enterFftf_destination_address(FlatJuniperParser.Fftf_destination_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_destination_address}.
	 * @param ctx the parse tree
	 */
	void exitFftf_destination_address(FlatJuniperParser.Fftf_destination_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_destination_port}.
	 * @param ctx the parse tree
	 */
	void enterFftf_destination_port(FlatJuniperParser.Fftf_destination_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_destination_port}.
	 * @param ctx the parse tree
	 */
	void exitFftf_destination_port(FlatJuniperParser.Fftf_destination_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_destination_port_except}.
	 * @param ctx the parse tree
	 */
	void enterFftf_destination_port_except(FlatJuniperParser.Fftf_destination_port_exceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_destination_port_except}.
	 * @param ctx the parse tree
	 */
	void exitFftf_destination_port_except(FlatJuniperParser.Fftf_destination_port_exceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_destination_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterFftf_destination_prefix_list(FlatJuniperParser.Fftf_destination_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_destination_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitFftf_destination_prefix_list(FlatJuniperParser.Fftf_destination_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_dscp}.
	 * @param ctx the parse tree
	 */
	void enterFftf_dscp(FlatJuniperParser.Fftf_dscpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_dscp}.
	 * @param ctx the parse tree
	 */
	void exitFftf_dscp(FlatJuniperParser.Fftf_dscpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_exp}.
	 * @param ctx the parse tree
	 */
	void enterFftf_exp(FlatJuniperParser.Fftf_expContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_exp}.
	 * @param ctx the parse tree
	 */
	void exitFftf_exp(FlatJuniperParser.Fftf_expContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_extension_header}.
	 * @param ctx the parse tree
	 */
	void enterFftf_extension_header(FlatJuniperParser.Fftf_extension_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_extension_header}.
	 * @param ctx the parse tree
	 */
	void exitFftf_extension_header(FlatJuniperParser.Fftf_extension_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_first_fragment}.
	 * @param ctx the parse tree
	 */
	void enterFftf_first_fragment(FlatJuniperParser.Fftf_first_fragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_first_fragment}.
	 * @param ctx the parse tree
	 */
	void exitFftf_first_fragment(FlatJuniperParser.Fftf_first_fragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_forwarding_class}.
	 * @param ctx the parse tree
	 */
	void enterFftf_forwarding_class(FlatJuniperParser.Fftf_forwarding_classContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_forwarding_class}.
	 * @param ctx the parse tree
	 */
	void exitFftf_forwarding_class(FlatJuniperParser.Fftf_forwarding_classContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_fragment_offset}.
	 * @param ctx the parse tree
	 */
	void enterFftf_fragment_offset(FlatJuniperParser.Fftf_fragment_offsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_fragment_offset}.
	 * @param ctx the parse tree
	 */
	void exitFftf_fragment_offset(FlatJuniperParser.Fftf_fragment_offsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_fragment_offset_except}.
	 * @param ctx the parse tree
	 */
	void enterFftf_fragment_offset_except(FlatJuniperParser.Fftf_fragment_offset_exceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_fragment_offset_except}.
	 * @param ctx the parse tree
	 */
	void exitFftf_fragment_offset_except(FlatJuniperParser.Fftf_fragment_offset_exceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_icmp_code}.
	 * @param ctx the parse tree
	 */
	void enterFftf_icmp_code(FlatJuniperParser.Fftf_icmp_codeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_icmp_code}.
	 * @param ctx the parse tree
	 */
	void exitFftf_icmp_code(FlatJuniperParser.Fftf_icmp_codeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_icmp_type}.
	 * @param ctx the parse tree
	 */
	void enterFftf_icmp_type(FlatJuniperParser.Fftf_icmp_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_icmp_type}.
	 * @param ctx the parse tree
	 */
	void exitFftf_icmp_type(FlatJuniperParser.Fftf_icmp_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_ip_options}.
	 * @param ctx the parse tree
	 */
	void enterFftf_ip_options(FlatJuniperParser.Fftf_ip_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_ip_options}.
	 * @param ctx the parse tree
	 */
	void exitFftf_ip_options(FlatJuniperParser.Fftf_ip_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_ip_protocol}.
	 * @param ctx the parse tree
	 */
	void enterFftf_ip_protocol(FlatJuniperParser.Fftf_ip_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_ip_protocol}.
	 * @param ctx the parse tree
	 */
	void exitFftf_ip_protocol(FlatJuniperParser.Fftf_ip_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_is_fragment}.
	 * @param ctx the parse tree
	 */
	void enterFftf_is_fragment(FlatJuniperParser.Fftf_is_fragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_is_fragment}.
	 * @param ctx the parse tree
	 */
	void exitFftf_is_fragment(FlatJuniperParser.Fftf_is_fragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_learn_vlan_1p_priority}.
	 * @param ctx the parse tree
	 */
	void enterFftf_learn_vlan_1p_priority(FlatJuniperParser.Fftf_learn_vlan_1p_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_learn_vlan_1p_priority}.
	 * @param ctx the parse tree
	 */
	void exitFftf_learn_vlan_1p_priority(FlatJuniperParser.Fftf_learn_vlan_1p_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_next_header}.
	 * @param ctx the parse tree
	 */
	void enterFftf_next_header(FlatJuniperParser.Fftf_next_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_next_header}.
	 * @param ctx the parse tree
	 */
	void exitFftf_next_header(FlatJuniperParser.Fftf_next_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_null}.
	 * @param ctx the parse tree
	 */
	void enterFftf_null(FlatJuniperParser.Fftf_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_null}.
	 * @param ctx the parse tree
	 */
	void exitFftf_null(FlatJuniperParser.Fftf_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_packet_length}.
	 * @param ctx the parse tree
	 */
	void enterFftf_packet_length(FlatJuniperParser.Fftf_packet_lengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_packet_length}.
	 * @param ctx the parse tree
	 */
	void exitFftf_packet_length(FlatJuniperParser.Fftf_packet_lengthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_packet_length_except}.
	 * @param ctx the parse tree
	 */
	void enterFftf_packet_length_except(FlatJuniperParser.Fftf_packet_length_exceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_packet_length_except}.
	 * @param ctx the parse tree
	 */
	void exitFftf_packet_length_except(FlatJuniperParser.Fftf_packet_length_exceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_port}.
	 * @param ctx the parse tree
	 */
	void enterFftf_port(FlatJuniperParser.Fftf_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_port}.
	 * @param ctx the parse tree
	 */
	void exitFftf_port(FlatJuniperParser.Fftf_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_precedence}.
	 * @param ctx the parse tree
	 */
	void enterFftf_precedence(FlatJuniperParser.Fftf_precedenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_precedence}.
	 * @param ctx the parse tree
	 */
	void exitFftf_precedence(FlatJuniperParser.Fftf_precedenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterFftf_prefix_list(FlatJuniperParser.Fftf_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitFftf_prefix_list(FlatJuniperParser.Fftf_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_protocol}.
	 * @param ctx the parse tree
	 */
	void enterFftf_protocol(FlatJuniperParser.Fftf_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_protocol}.
	 * @param ctx the parse tree
	 */
	void exitFftf_protocol(FlatJuniperParser.Fftf_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_source_address}.
	 * @param ctx the parse tree
	 */
	void enterFftf_source_address(FlatJuniperParser.Fftf_source_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_source_address}.
	 * @param ctx the parse tree
	 */
	void exitFftf_source_address(FlatJuniperParser.Fftf_source_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_source_mac_address}.
	 * @param ctx the parse tree
	 */
	void enterFftf_source_mac_address(FlatJuniperParser.Fftf_source_mac_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_source_mac_address}.
	 * @param ctx the parse tree
	 */
	void exitFftf_source_mac_address(FlatJuniperParser.Fftf_source_mac_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_source_port}.
	 * @param ctx the parse tree
	 */
	void enterFftf_source_port(FlatJuniperParser.Fftf_source_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_source_port}.
	 * @param ctx the parse tree
	 */
	void exitFftf_source_port(FlatJuniperParser.Fftf_source_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_source_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterFftf_source_prefix_list(FlatJuniperParser.Fftf_source_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_source_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitFftf_source_prefix_list(FlatJuniperParser.Fftf_source_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_tcp_established}.
	 * @param ctx the parse tree
	 */
	void enterFftf_tcp_established(FlatJuniperParser.Fftf_tcp_establishedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_tcp_established}.
	 * @param ctx the parse tree
	 */
	void exitFftf_tcp_established(FlatJuniperParser.Fftf_tcp_establishedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_tcp_flags}.
	 * @param ctx the parse tree
	 */
	void enterFftf_tcp_flags(FlatJuniperParser.Fftf_tcp_flagsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_tcp_flags}.
	 * @param ctx the parse tree
	 */
	void exitFftf_tcp_flags(FlatJuniperParser.Fftf_tcp_flagsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_tcp_initial}.
	 * @param ctx the parse tree
	 */
	void enterFftf_tcp_initial(FlatJuniperParser.Fftf_tcp_initialContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_tcp_initial}.
	 * @param ctx the parse tree
	 */
	void exitFftf_tcp_initial(FlatJuniperParser.Fftf_tcp_initialContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftf_vlan}.
	 * @param ctx the parse tree
	 */
	void enterFftf_vlan(FlatJuniperParser.Fftf_vlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftf_vlan}.
	 * @param ctx the parse tree
	 */
	void exitFftf_vlan(FlatJuniperParser.Fftf_vlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_accept}.
	 * @param ctx the parse tree
	 */
	void enterFftt_accept(FlatJuniperParser.Fftt_acceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_accept}.
	 * @param ctx the parse tree
	 */
	void exitFftt_accept(FlatJuniperParser.Fftt_acceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_discard}.
	 * @param ctx the parse tree
	 */
	void enterFftt_discard(FlatJuniperParser.Fftt_discardContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_discard}.
	 * @param ctx the parse tree
	 */
	void exitFftt_discard(FlatJuniperParser.Fftt_discardContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_loss_priority}.
	 * @param ctx the parse tree
	 */
	void enterFftt_loss_priority(FlatJuniperParser.Fftt_loss_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_loss_priority}.
	 * @param ctx the parse tree
	 */
	void exitFftt_loss_priority(FlatJuniperParser.Fftt_loss_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_next_ip}.
	 * @param ctx the parse tree
	 */
	void enterFftt_next_ip(FlatJuniperParser.Fftt_next_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_next_ip}.
	 * @param ctx the parse tree
	 */
	void exitFftt_next_ip(FlatJuniperParser.Fftt_next_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_next_term}.
	 * @param ctx the parse tree
	 */
	void enterFftt_next_term(FlatJuniperParser.Fftt_next_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_next_term}.
	 * @param ctx the parse tree
	 */
	void exitFftt_next_term(FlatJuniperParser.Fftt_next_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_nop}.
	 * @param ctx the parse tree
	 */
	void enterFftt_nop(FlatJuniperParser.Fftt_nopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_nop}.
	 * @param ctx the parse tree
	 */
	void exitFftt_nop(FlatJuniperParser.Fftt_nopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_port_mirror}.
	 * @param ctx the parse tree
	 */
	void enterFftt_port_mirror(FlatJuniperParser.Fftt_port_mirrorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_port_mirror}.
	 * @param ctx the parse tree
	 */
	void exitFftt_port_mirror(FlatJuniperParser.Fftt_port_mirrorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_reject}.
	 * @param ctx the parse tree
	 */
	void enterFftt_reject(FlatJuniperParser.Fftt_rejectContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_reject}.
	 * @param ctx the parse tree
	 */
	void exitFftt_reject(FlatJuniperParser.Fftt_rejectContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fftt_routing_instance}.
	 * @param ctx the parse tree
	 */
	void enterFftt_routing_instance(FlatJuniperParser.Fftt_routing_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fftt_routing_instance}.
	 * @param ctx the parse tree
	 */
	void exitFftt_routing_instance(FlatJuniperParser.Fftt_routing_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_firewall}.
	 * @param ctx the parse tree
	 */
	void enterS_firewall(FlatJuniperParser.S_firewallContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_firewall}.
	 * @param ctx the parse tree
	 */
	void exitS_firewall(FlatJuniperParser.S_firewallContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#tcp_flags}.
	 * @param ctx the parse tree
	 */
	void enterTcp_flags(FlatJuniperParser.Tcp_flagsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#tcp_flags}.
	 * @param ctx the parse tree
	 */
	void exitTcp_flags(FlatJuniperParser.Tcp_flagsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#tcp_flags_alternative}.
	 * @param ctx the parse tree
	 */
	void enterTcp_flags_alternative(FlatJuniperParser.Tcp_flags_alternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#tcp_flags_alternative}.
	 * @param ctx the parse tree
	 */
	void exitTcp_flags_alternative(FlatJuniperParser.Tcp_flags_alternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#tcp_flags_atom}.
	 * @param ctx the parse tree
	 */
	void enterTcp_flags_atom(FlatJuniperParser.Tcp_flags_atomContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#tcp_flags_atom}.
	 * @param ctx the parse tree
	 */
	void exitTcp_flags_atom(FlatJuniperParser.Tcp_flags_atomContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#tcp_flags_literal}.
	 * @param ctx the parse tree
	 */
	void enterTcp_flags_literal(FlatJuniperParser.Tcp_flags_literalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#tcp_flags_literal}.
	 * @param ctx the parse tree
	 */
	void exitTcp_flags_literal(FlatJuniperParser.Tcp_flags_literalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fo_dhcp_relay}.
	 * @param ctx the parse tree
	 */
	void enterFo_dhcp_relay(FlatJuniperParser.Fo_dhcp_relayContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fo_dhcp_relay}.
	 * @param ctx the parse tree
	 */
	void exitFo_dhcp_relay(FlatJuniperParser.Fo_dhcp_relayContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fo_helpers}.
	 * @param ctx the parse tree
	 */
	void enterFo_helpers(FlatJuniperParser.Fo_helpersContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fo_helpers}.
	 * @param ctx the parse tree
	 */
	void exitFo_helpers(FlatJuniperParser.Fo_helpersContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fo_null}.
	 * @param ctx the parse tree
	 */
	void enterFo_null(FlatJuniperParser.Fo_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fo_null}.
	 * @param ctx the parse tree
	 */
	void exitFo_null(FlatJuniperParser.Fo_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fod_active_server_group}.
	 * @param ctx the parse tree
	 */
	void enterFod_active_server_group(FlatJuniperParser.Fod_active_server_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fod_active_server_group}.
	 * @param ctx the parse tree
	 */
	void exitFod_active_server_group(FlatJuniperParser.Fod_active_server_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fod_common}.
	 * @param ctx the parse tree
	 */
	void enterFod_common(FlatJuniperParser.Fod_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fod_common}.
	 * @param ctx the parse tree
	 */
	void exitFod_common(FlatJuniperParser.Fod_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fod_group}.
	 * @param ctx the parse tree
	 */
	void enterFod_group(FlatJuniperParser.Fod_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fod_group}.
	 * @param ctx the parse tree
	 */
	void exitFod_group(FlatJuniperParser.Fod_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fod_null}.
	 * @param ctx the parse tree
	 */
	void enterFod_null(FlatJuniperParser.Fod_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fod_null}.
	 * @param ctx the parse tree
	 */
	void exitFod_null(FlatJuniperParser.Fod_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fod_server_group}.
	 * @param ctx the parse tree
	 */
	void enterFod_server_group(FlatJuniperParser.Fod_server_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fod_server_group}.
	 * @param ctx the parse tree
	 */
	void exitFod_server_group(FlatJuniperParser.Fod_server_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fodg_interface}.
	 * @param ctx the parse tree
	 */
	void enterFodg_interface(FlatJuniperParser.Fodg_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fodg_interface}.
	 * @param ctx the parse tree
	 */
	void exitFodg_interface(FlatJuniperParser.Fodg_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fodg_null}.
	 * @param ctx the parse tree
	 */
	void enterFodg_null(FlatJuniperParser.Fodg_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fodg_null}.
	 * @param ctx the parse tree
	 */
	void exitFodg_null(FlatJuniperParser.Fodg_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#foh_bootp}.
	 * @param ctx the parse tree
	 */
	void enterFoh_bootp(FlatJuniperParser.Foh_bootpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#foh_bootp}.
	 * @param ctx the parse tree
	 */
	void exitFoh_bootp(FlatJuniperParser.Foh_bootpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#foh_null}.
	 * @param ctx the parse tree
	 */
	void enterFoh_null(FlatJuniperParser.Foh_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#foh_null}.
	 * @param ctx the parse tree
	 */
	void exitFoh_null(FlatJuniperParser.Foh_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fohb_common}.
	 * @param ctx the parse tree
	 */
	void enterFohb_common(FlatJuniperParser.Fohb_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fohb_common}.
	 * @param ctx the parse tree
	 */
	void exitFohb_common(FlatJuniperParser.Fohb_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fohb_interface}.
	 * @param ctx the parse tree
	 */
	void enterFohb_interface(FlatJuniperParser.Fohb_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fohb_interface}.
	 * @param ctx the parse tree
	 */
	void exitFohb_interface(FlatJuniperParser.Fohb_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fohb_null}.
	 * @param ctx the parse tree
	 */
	void enterFohb_null(FlatJuniperParser.Fohb_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fohb_null}.
	 * @param ctx the parse tree
	 */
	void exitFohb_null(FlatJuniperParser.Fohb_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#fohb_server}.
	 * @param ctx the parse tree
	 */
	void enterFohb_server(FlatJuniperParser.Fohb_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#fohb_server}.
	 * @param ctx the parse tree
	 */
	void exitFohb_server(FlatJuniperParser.Fohb_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_forwarding_options}.
	 * @param ctx the parse tree
	 */
	void enterS_forwarding_options(FlatJuniperParser.S_forwarding_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_forwarding_options}.
	 * @param ctx the parse tree
	 */
	void exitS_forwarding_options(FlatJuniperParser.S_forwarding_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#direction}.
	 * @param ctx the parse tree
	 */
	void enterDirection(FlatJuniperParser.DirectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#direction}.
	 * @param ctx the parse tree
	 */
	void exitDirection(FlatJuniperParser.DirectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_802_3ad}.
	 * @param ctx the parse tree
	 */
	void enterEo_802_3ad(FlatJuniperParser.Eo_802_3adContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_802_3ad}.
	 * @param ctx the parse tree
	 */
	void exitEo_802_3ad(FlatJuniperParser.Eo_802_3adContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_auto_negotiation}.
	 * @param ctx the parse tree
	 */
	void enterEo_auto_negotiation(FlatJuniperParser.Eo_auto_negotiationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_auto_negotiation}.
	 * @param ctx the parse tree
	 */
	void exitEo_auto_negotiation(FlatJuniperParser.Eo_auto_negotiationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_no_auto_negotiation}.
	 * @param ctx the parse tree
	 */
	void enterEo_no_auto_negotiation(FlatJuniperParser.Eo_no_auto_negotiationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_no_auto_negotiation}.
	 * @param ctx the parse tree
	 */
	void exitEo_no_auto_negotiation(FlatJuniperParser.Eo_no_auto_negotiationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_null}.
	 * @param ctx the parse tree
	 */
	void enterEo_null(FlatJuniperParser.Eo_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_null}.
	 * @param ctx the parse tree
	 */
	void exitEo_null(FlatJuniperParser.Eo_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_redundant_parent}.
	 * @param ctx the parse tree
	 */
	void enterEo_redundant_parent(FlatJuniperParser.Eo_redundant_parentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_redundant_parent}.
	 * @param ctx the parse tree
	 */
	void exitEo_redundant_parent(FlatJuniperParser.Eo_redundant_parentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo_speed}.
	 * @param ctx the parse tree
	 */
	void enterEo_speed(FlatJuniperParser.Eo_speedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo_speed}.
	 * @param ctx the parse tree
	 */
	void exitEo_speed(FlatJuniperParser.Eo_speedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo8023ad_interface}.
	 * @param ctx the parse tree
	 */
	void enterEo8023ad_interface(FlatJuniperParser.Eo8023ad_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo8023ad_interface}.
	 * @param ctx the parse tree
	 */
	void exitEo8023ad_interface(FlatJuniperParser.Eo8023ad_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#eo8023ad_lacp}.
	 * @param ctx the parse tree
	 */
	void enterEo8023ad_lacp(FlatJuniperParser.Eo8023ad_lacpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#eo8023ad_lacp}.
	 * @param ctx the parse tree
	 */
	void exitEo8023ad_lacp(FlatJuniperParser.Eo8023ad_lacpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ether_options}.
	 * @param ctx the parse tree
	 */
	void enterEther_options(FlatJuniperParser.Ether_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ether_options}.
	 * @param ctx the parse tree
	 */
	void exitEther_options(FlatJuniperParser.Ether_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(FlatJuniperParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(FlatJuniperParser.FilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_apply_groups}.
	 * @param ctx the parse tree
	 */
	void enterI_apply_groups(FlatJuniperParser.I_apply_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_apply_groups}.
	 * @param ctx the parse tree
	 */
	void exitI_apply_groups(FlatJuniperParser.I_apply_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_apply_groups_except}.
	 * @param ctx the parse tree
	 */
	void enterI_apply_groups_except(FlatJuniperParser.I_apply_groups_exceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_apply_groups_except}.
	 * @param ctx the parse tree
	 */
	void exitI_apply_groups_except(FlatJuniperParser.I_apply_groups_exceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_arp_resp}.
	 * @param ctx the parse tree
	 */
	void enterI_arp_resp(FlatJuniperParser.I_arp_respContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_arp_resp}.
	 * @param ctx the parse tree
	 */
	void exitI_arp_resp(FlatJuniperParser.I_arp_respContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_bandwidth}.
	 * @param ctx the parse tree
	 */
	void enterI_bandwidth(FlatJuniperParser.I_bandwidthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_bandwidth}.
	 * @param ctx the parse tree
	 */
	void exitI_bandwidth(FlatJuniperParser.I_bandwidthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_common}.
	 * @param ctx the parse tree
	 */
	void enterI_common(FlatJuniperParser.I_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_common}.
	 * @param ctx the parse tree
	 */
	void exitI_common(FlatJuniperParser.I_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_common_physical}.
	 * @param ctx the parse tree
	 */
	void enterI_common_physical(FlatJuniperParser.I_common_physicalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_common_physical}.
	 * @param ctx the parse tree
	 */
	void exitI_common_physical(FlatJuniperParser.I_common_physicalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_description}.
	 * @param ctx the parse tree
	 */
	void enterI_description(FlatJuniperParser.I_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_description}.
	 * @param ctx the parse tree
	 */
	void exitI_description(FlatJuniperParser.I_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_disable}.
	 * @param ctx the parse tree
	 */
	void enterI_disable(FlatJuniperParser.I_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_disable}.
	 * @param ctx the parse tree
	 */
	void exitI_disable(FlatJuniperParser.I_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_enable}.
	 * @param ctx the parse tree
	 */
	void enterI_enable(FlatJuniperParser.I_enableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_enable}.
	 * @param ctx the parse tree
	 */
	void exitI_enable(FlatJuniperParser.I_enableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_ether_options}.
	 * @param ctx the parse tree
	 */
	void enterI_ether_options(FlatJuniperParser.I_ether_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_ether_options}.
	 * @param ctx the parse tree
	 */
	void exitI_ether_options(FlatJuniperParser.I_ether_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_fastether_options}.
	 * @param ctx the parse tree
	 */
	void enterI_fastether_options(FlatJuniperParser.I_fastether_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_fastether_options}.
	 * @param ctx the parse tree
	 */
	void exitI_fastether_options(FlatJuniperParser.I_fastether_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_family}.
	 * @param ctx the parse tree
	 */
	void enterI_family(FlatJuniperParser.I_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_family}.
	 * @param ctx the parse tree
	 */
	void exitI_family(FlatJuniperParser.I_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_flexible_vlan_tagging}.
	 * @param ctx the parse tree
	 */
	void enterI_flexible_vlan_tagging(FlatJuniperParser.I_flexible_vlan_taggingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_flexible_vlan_tagging}.
	 * @param ctx the parse tree
	 */
	void exitI_flexible_vlan_tagging(FlatJuniperParser.I_flexible_vlan_taggingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_gigether_options}.
	 * @param ctx the parse tree
	 */
	void enterI_gigether_options(FlatJuniperParser.I_gigether_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_gigether_options}.
	 * @param ctx the parse tree
	 */
	void exitI_gigether_options(FlatJuniperParser.I_gigether_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_link_mode}.
	 * @param ctx the parse tree
	 */
	void enterI_link_mode(FlatJuniperParser.I_link_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_link_mode}.
	 * @param ctx the parse tree
	 */
	void exitI_link_mode(FlatJuniperParser.I_link_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_mac}.
	 * @param ctx the parse tree
	 */
	void enterI_mac(FlatJuniperParser.I_macContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_mac}.
	 * @param ctx the parse tree
	 */
	void exitI_mac(FlatJuniperParser.I_macContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_mtu}.
	 * @param ctx the parse tree
	 */
	void enterI_mtu(FlatJuniperParser.I_mtuContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_mtu}.
	 * @param ctx the parse tree
	 */
	void exitI_mtu(FlatJuniperParser.I_mtuContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_native_vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterI_native_vlan_id(FlatJuniperParser.I_native_vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_native_vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitI_native_vlan_id(FlatJuniperParser.I_native_vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_null}.
	 * @param ctx the parse tree
	 */
	void enterI_null(FlatJuniperParser.I_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_null}.
	 * @param ctx the parse tree
	 */
	void exitI_null(FlatJuniperParser.I_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_peer_unit}.
	 * @param ctx the parse tree
	 */
	void enterI_peer_unit(FlatJuniperParser.I_peer_unitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_peer_unit}.
	 * @param ctx the parse tree
	 */
	void exitI_peer_unit(FlatJuniperParser.I_peer_unitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_per_unit_scheduler}.
	 * @param ctx the parse tree
	 */
	void enterI_per_unit_scheduler(FlatJuniperParser.I_per_unit_schedulerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_per_unit_scheduler}.
	 * @param ctx the parse tree
	 */
	void exitI_per_unit_scheduler(FlatJuniperParser.I_per_unit_schedulerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_redundant_ether_options}.
	 * @param ctx the parse tree
	 */
	void enterI_redundant_ether_options(FlatJuniperParser.I_redundant_ether_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_redundant_ether_options}.
	 * @param ctx the parse tree
	 */
	void exitI_redundant_ether_options(FlatJuniperParser.I_redundant_ether_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_speed}.
	 * @param ctx the parse tree
	 */
	void enterI_speed(FlatJuniperParser.I_speedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_speed}.
	 * @param ctx the parse tree
	 */
	void exitI_speed(FlatJuniperParser.I_speedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_unit}.
	 * @param ctx the parse tree
	 */
	void enterI_unit(FlatJuniperParser.I_unitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_unit}.
	 * @param ctx the parse tree
	 */
	void exitI_unit(FlatJuniperParser.I_unitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterI_vlan_id(FlatJuniperParser.I_vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitI_vlan_id(FlatJuniperParser.I_vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_vlan_id_list}.
	 * @param ctx the parse tree
	 */
	void enterI_vlan_id_list(FlatJuniperParser.I_vlan_id_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_vlan_id_list}.
	 * @param ctx the parse tree
	 */
	void exitI_vlan_id_list(FlatJuniperParser.I_vlan_id_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#i_vlan_tagging}.
	 * @param ctx the parse tree
	 */
	void enterI_vlan_tagging(FlatJuniperParser.I_vlan_taggingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#i_vlan_tagging}.
	 * @param ctx the parse tree
	 */
	void exitI_vlan_tagging(FlatJuniperParser.I_vlan_taggingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_bridge}.
	 * @param ctx the parse tree
	 */
	void enterIf_bridge(FlatJuniperParser.If_bridgeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_bridge}.
	 * @param ctx the parse tree
	 */
	void exitIf_bridge(FlatJuniperParser.If_bridgeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_ccc}.
	 * @param ctx the parse tree
	 */
	void enterIf_ccc(FlatJuniperParser.If_cccContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_ccc}.
	 * @param ctx the parse tree
	 */
	void exitIf_ccc(FlatJuniperParser.If_cccContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_ethernet_switching}.
	 * @param ctx the parse tree
	 */
	void enterIf_ethernet_switching(FlatJuniperParser.If_ethernet_switchingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_ethernet_switching}.
	 * @param ctx the parse tree
	 */
	void exitIf_ethernet_switching(FlatJuniperParser.If_ethernet_switchingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_inet}.
	 * @param ctx the parse tree
	 */
	void enterIf_inet(FlatJuniperParser.If_inetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_inet}.
	 * @param ctx the parse tree
	 */
	void exitIf_inet(FlatJuniperParser.If_inetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_inet6}.
	 * @param ctx the parse tree
	 */
	void enterIf_inet6(FlatJuniperParser.If_inet6Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_inet6}.
	 * @param ctx the parse tree
	 */
	void exitIf_inet6(FlatJuniperParser.If_inet6Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_iso}.
	 * @param ctx the parse tree
	 */
	void enterIf_iso(FlatJuniperParser.If_isoContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_iso}.
	 * @param ctx the parse tree
	 */
	void exitIf_iso(FlatJuniperParser.If_isoContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_mpls}.
	 * @param ctx the parse tree
	 */
	void enterIf_mpls(FlatJuniperParser.If_mplsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_mpls}.
	 * @param ctx the parse tree
	 */
	void exitIf_mpls(FlatJuniperParser.If_mplsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#if_storm_control}.
	 * @param ctx the parse tree
	 */
	void enterIf_storm_control(FlatJuniperParser.If_storm_controlContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#if_storm_control}.
	 * @param ctx the parse tree
	 */
	void exitIf_storm_control(FlatJuniperParser.If_storm_controlContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifbr_filter}.
	 * @param ctx the parse tree
	 */
	void enterIfbr_filter(FlatJuniperParser.Ifbr_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifbr_filter}.
	 * @param ctx the parse tree
	 */
	void exitIfbr_filter(FlatJuniperParser.Ifbr_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifbr_interface_mode}.
	 * @param ctx the parse tree
	 */
	void enterIfbr_interface_mode(FlatJuniperParser.Ifbr_interface_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifbr_interface_mode}.
	 * @param ctx the parse tree
	 */
	void exitIfbr_interface_mode(FlatJuniperParser.Ifbr_interface_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifbr_vlan_id_list}.
	 * @param ctx the parse tree
	 */
	void enterIfbr_vlan_id_list(FlatJuniperParser.Ifbr_vlan_id_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifbr_vlan_id_list}.
	 * @param ctx the parse tree
	 */
	void exitIfbr_vlan_id_list(FlatJuniperParser.Ifbr_vlan_id_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ife_filter}.
	 * @param ctx the parse tree
	 */
	void enterIfe_filter(FlatJuniperParser.Ife_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ife_filter}.
	 * @param ctx the parse tree
	 */
	void exitIfe_filter(FlatJuniperParser.Ife_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ife_interface_mode}.
	 * @param ctx the parse tree
	 */
	void enterIfe_interface_mode(FlatJuniperParser.Ife_interface_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ife_interface_mode}.
	 * @param ctx the parse tree
	 */
	void exitIfe_interface_mode(FlatJuniperParser.Ife_interface_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ife_native_vlan_id}.
	 * @param ctx the parse tree
	 */
	void enterIfe_native_vlan_id(FlatJuniperParser.Ife_native_vlan_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ife_native_vlan_id}.
	 * @param ctx the parse tree
	 */
	void exitIfe_native_vlan_id(FlatJuniperParser.Ife_native_vlan_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ife_port_mode}.
	 * @param ctx the parse tree
	 */
	void enterIfe_port_mode(FlatJuniperParser.Ife_port_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ife_port_mode}.
	 * @param ctx the parse tree
	 */
	void exitIfe_port_mode(FlatJuniperParser.Ife_port_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ife_vlan}.
	 * @param ctx the parse tree
	 */
	void enterIfe_vlan(FlatJuniperParser.Ife_vlanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ife_vlan}.
	 * @param ctx the parse tree
	 */
	void exitIfe_vlan(FlatJuniperParser.Ife_vlanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_address}.
	 * @param ctx the parse tree
	 */
	void enterIfi_address(FlatJuniperParser.Ifi_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_address}.
	 * @param ctx the parse tree
	 */
	void exitIfi_address(FlatJuniperParser.Ifi_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_filter}.
	 * @param ctx the parse tree
	 */
	void enterIfi_filter(FlatJuniperParser.Ifi_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_filter}.
	 * @param ctx the parse tree
	 */
	void exitIfi_filter(FlatJuniperParser.Ifi_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_mtu}.
	 * @param ctx the parse tree
	 */
	void enterIfi_mtu(FlatJuniperParser.Ifi_mtuContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_mtu}.
	 * @param ctx the parse tree
	 */
	void exitIfi_mtu(FlatJuniperParser.Ifi_mtuContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_no_redirects}.
	 * @param ctx the parse tree
	 */
	void enterIfi_no_redirects(FlatJuniperParser.Ifi_no_redirectsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_no_redirects}.
	 * @param ctx the parse tree
	 */
	void exitIfi_no_redirects(FlatJuniperParser.Ifi_no_redirectsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_null}.
	 * @param ctx the parse tree
	 */
	void enterIfi_null(FlatJuniperParser.Ifi_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_null}.
	 * @param ctx the parse tree
	 */
	void exitIfi_null(FlatJuniperParser.Ifi_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_rpf_check}.
	 * @param ctx the parse tree
	 */
	void enterIfi_rpf_check(FlatJuniperParser.Ifi_rpf_checkContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_rpf_check}.
	 * @param ctx the parse tree
	 */
	void exitIfi_rpf_check(FlatJuniperParser.Ifi_rpf_checkContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifi_tcp_mss}.
	 * @param ctx the parse tree
	 */
	void enterIfi_tcp_mss(FlatJuniperParser.Ifi_tcp_mssContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifi_tcp_mss}.
	 * @param ctx the parse tree
	 */
	void exitIfi_tcp_mss(FlatJuniperParser.Ifi_tcp_mssContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifia_arp}.
	 * @param ctx the parse tree
	 */
	void enterIfia_arp(FlatJuniperParser.Ifia_arpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifia_arp}.
	 * @param ctx the parse tree
	 */
	void exitIfia_arp(FlatJuniperParser.Ifia_arpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifia_master_only}.
	 * @param ctx the parse tree
	 */
	void enterIfia_master_only(FlatJuniperParser.Ifia_master_onlyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifia_master_only}.
	 * @param ctx the parse tree
	 */
	void exitIfia_master_only(FlatJuniperParser.Ifia_master_onlyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifia_preferred}.
	 * @param ctx the parse tree
	 */
	void enterIfia_preferred(FlatJuniperParser.Ifia_preferredContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifia_preferred}.
	 * @param ctx the parse tree
	 */
	void exitIfia_preferred(FlatJuniperParser.Ifia_preferredContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifia_primary}.
	 * @param ctx the parse tree
	 */
	void enterIfia_primary(FlatJuniperParser.Ifia_primaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifia_primary}.
	 * @param ctx the parse tree
	 */
	void exitIfia_primary(FlatJuniperParser.Ifia_primaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifia_vrrp_group}.
	 * @param ctx the parse tree
	 */
	void enterIfia_vrrp_group(FlatJuniperParser.Ifia_vrrp_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifia_vrrp_group}.
	 * @param ctx the parse tree
	 */
	void exitIfia_vrrp_group(FlatJuniperParser.Ifia_vrrp_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_accept_data}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_accept_data(FlatJuniperParser.Ifiav_accept_dataContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_accept_data}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_accept_data(FlatJuniperParser.Ifiav_accept_dataContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_advertise_interval}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_advertise_interval(FlatJuniperParser.Ifiav_advertise_intervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_advertise_interval}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_advertise_interval(FlatJuniperParser.Ifiav_advertise_intervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_authentication_key}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_authentication_key(FlatJuniperParser.Ifiav_authentication_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_authentication_key}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_authentication_key(FlatJuniperParser.Ifiav_authentication_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_authentication_type}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_authentication_type(FlatJuniperParser.Ifiav_authentication_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_authentication_type}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_authentication_type(FlatJuniperParser.Ifiav_authentication_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_preempt}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_preempt(FlatJuniperParser.Ifiav_preemptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_preempt}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_preempt(FlatJuniperParser.Ifiav_preemptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_priority}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_priority(FlatJuniperParser.Ifiav_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_priority}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_priority(FlatJuniperParser.Ifiav_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_track}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_track(FlatJuniperParser.Ifiav_trackContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_track}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_track(FlatJuniperParser.Ifiav_trackContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiav_virtual_address}.
	 * @param ctx the parse tree
	 */
	void enterIfiav_virtual_address(FlatJuniperParser.Ifiav_virtual_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiav_virtual_address}.
	 * @param ctx the parse tree
	 */
	void exitIfiav_virtual_address(FlatJuniperParser.Ifiav_virtual_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiavt_interface}.
	 * @param ctx the parse tree
	 */
	void enterIfiavt_interface(FlatJuniperParser.Ifiavt_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiavt_interface}.
	 * @param ctx the parse tree
	 */
	void exitIfiavt_interface(FlatJuniperParser.Ifiavt_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiavt_route}.
	 * @param ctx the parse tree
	 */
	void enterIfiavt_route(FlatJuniperParser.Ifiavt_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiavt_route}.
	 * @param ctx the parse tree
	 */
	void exitIfiavt_route(FlatJuniperParser.Ifiavt_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiavti_priority_cost}.
	 * @param ctx the parse tree
	 */
	void enterIfiavti_priority_cost(FlatJuniperParser.Ifiavti_priority_costContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiavti_priority_cost}.
	 * @param ctx the parse tree
	 */
	void exitIfiavti_priority_cost(FlatJuniperParser.Ifiavti_priority_costContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiso_address}.
	 * @param ctx the parse tree
	 */
	void enterIfiso_address(FlatJuniperParser.Ifiso_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiso_address}.
	 * @param ctx the parse tree
	 */
	void exitIfiso_address(FlatJuniperParser.Ifiso_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifiso_mtu}.
	 * @param ctx the parse tree
	 */
	void enterIfiso_mtu(FlatJuniperParser.Ifiso_mtuContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifiso_mtu}.
	 * @param ctx the parse tree
	 */
	void exitIfiso_mtu(FlatJuniperParser.Ifiso_mtuContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifm_filter}.
	 * @param ctx the parse tree
	 */
	void enterIfm_filter(FlatJuniperParser.Ifm_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifm_filter}.
	 * @param ctx the parse tree
	 */
	void exitIfm_filter(FlatJuniperParser.Ifm_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifm_maximum_labels}.
	 * @param ctx the parse tree
	 */
	void enterIfm_maximum_labels(FlatJuniperParser.Ifm_maximum_labelsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifm_maximum_labels}.
	 * @param ctx the parse tree
	 */
	void exitIfm_maximum_labels(FlatJuniperParser.Ifm_maximum_labelsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ifm_mtu}.
	 * @param ctx the parse tree
	 */
	void enterIfm_mtu(FlatJuniperParser.Ifm_mtuContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ifm_mtu}.
	 * @param ctx the parse tree
	 */
	void exitIfm_mtu(FlatJuniperParser.Ifm_mtuContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#int_interface_range}.
	 * @param ctx the parse tree
	 */
	void enterInt_interface_range(FlatJuniperParser.Int_interface_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#int_interface_range}.
	 * @param ctx the parse tree
	 */
	void exitInt_interface_range(FlatJuniperParser.Int_interface_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#int_named}.
	 * @param ctx the parse tree
	 */
	void enterInt_named(FlatJuniperParser.Int_namedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#int_named}.
	 * @param ctx the parse tree
	 */
	void exitInt_named(FlatJuniperParser.Int_namedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#int_null}.
	 * @param ctx the parse tree
	 */
	void enterInt_null(FlatJuniperParser.Int_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#int_null}.
	 * @param ctx the parse tree
	 */
	void exitInt_null(FlatJuniperParser.Int_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#interface_mode}.
	 * @param ctx the parse tree
	 */
	void enterInterface_mode(FlatJuniperParser.Interface_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#interface_mode}.
	 * @param ctx the parse tree
	 */
	void exitInterface_mode(FlatJuniperParser.Interface_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#intir_member}.
	 * @param ctx the parse tree
	 */
	void enterIntir_member(FlatJuniperParser.Intir_memberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#intir_member}.
	 * @param ctx the parse tree
	 */
	void exitIntir_member(FlatJuniperParser.Intir_memberContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#intir_member_range}.
	 * @param ctx the parse tree
	 */
	void enterIntir_member_range(FlatJuniperParser.Intir_member_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#intir_member_range}.
	 * @param ctx the parse tree
	 */
	void exitIntir_member_range(FlatJuniperParser.Intir_member_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_interfaces}.
	 * @param ctx the parse tree
	 */
	void enterS_interfaces(FlatJuniperParser.S_interfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_interfaces}.
	 * @param ctx the parse tree
	 */
	void exitS_interfaces(FlatJuniperParser.S_interfacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#speed_abbreviation}.
	 * @param ctx the parse tree
	 */
	void enterSpeed_abbreviation(FlatJuniperParser.Speed_abbreviationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#speed_abbreviation}.
	 * @param ctx the parse tree
	 */
	void exitSpeed_abbreviation(FlatJuniperParser.Speed_abbreviationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#base_community_regex}.
	 * @param ctx the parse tree
	 */
	void enterBase_community_regex(FlatJuniperParser.Base_community_regexContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#base_community_regex}.
	 * @param ctx the parse tree
	 */
	void exitBase_community_regex(FlatJuniperParser.Base_community_regexContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#base_extended_community_regex}.
	 * @param ctx the parse tree
	 */
	void enterBase_extended_community_regex(FlatJuniperParser.Base_extended_community_regexContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#base_extended_community_regex}.
	 * @param ctx the parse tree
	 */
	void exitBase_extended_community_regex(FlatJuniperParser.Base_extended_community_regexContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#community_regex}.
	 * @param ctx the parse tree
	 */
	void enterCommunity_regex(FlatJuniperParser.Community_regexContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#community_regex}.
	 * @param ctx the parse tree
	 */
	void exitCommunity_regex(FlatJuniperParser.Community_regexContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#extended_community_regex}.
	 * @param ctx the parse tree
	 */
	void enterExtended_community_regex(FlatJuniperParser.Extended_community_regexContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#extended_community_regex}.
	 * @param ctx the parse tree
	 */
	void exitExtended_community_regex(FlatJuniperParser.Extended_community_regexContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#invalid_community_regex}.
	 * @param ctx the parse tree
	 */
	void enterInvalid_community_regex(FlatJuniperParser.Invalid_community_regexContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#invalid_community_regex}.
	 * @param ctx the parse tree
	 */
	void exitInvalid_community_regex(FlatJuniperParser.Invalid_community_regexContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#metric_expression}.
	 * @param ctx the parse tree
	 */
	void enterMetric_expression(FlatJuniperParser.Metric_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#metric_expression}.
	 * @param ctx the parse tree
	 */
	void exitMetric_expression(FlatJuniperParser.Metric_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_as_path}.
	 * @param ctx the parse tree
	 */
	void enterPo_as_path(FlatJuniperParser.Po_as_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_as_path}.
	 * @param ctx the parse tree
	 */
	void exitPo_as_path(FlatJuniperParser.Po_as_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_as_path_group}.
	 * @param ctx the parse tree
	 */
	void enterPo_as_path_group(FlatJuniperParser.Po_as_path_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_as_path_group}.
	 * @param ctx the parse tree
	 */
	void exitPo_as_path_group(FlatJuniperParser.Po_as_path_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_community}.
	 * @param ctx the parse tree
	 */
	void enterPo_community(FlatJuniperParser.Po_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_community}.
	 * @param ctx the parse tree
	 */
	void exitPo_community(FlatJuniperParser.Po_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_condition}.
	 * @param ctx the parse tree
	 */
	void enterPo_condition(FlatJuniperParser.Po_conditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_condition}.
	 * @param ctx the parse tree
	 */
	void exitPo_condition(FlatJuniperParser.Po_conditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_policy_statement}.
	 * @param ctx the parse tree
	 */
	void enterPo_policy_statement(FlatJuniperParser.Po_policy_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_policy_statement}.
	 * @param ctx the parse tree
	 */
	void exitPo_policy_statement(FlatJuniperParser.Po_policy_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#po_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterPo_prefix_list(FlatJuniperParser.Po_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#po_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitPo_prefix_list(FlatJuniperParser.Po_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poapg_as_path}.
	 * @param ctx the parse tree
	 */
	void enterPoapg_as_path(FlatJuniperParser.Poapg_as_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poapg_as_path}.
	 * @param ctx the parse tree
	 */
	void exitPoapg_as_path(FlatJuniperParser.Poapg_as_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poc_invert_match}.
	 * @param ctx the parse tree
	 */
	void enterPoc_invert_match(FlatJuniperParser.Poc_invert_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poc_invert_match}.
	 * @param ctx the parse tree
	 */
	void exitPoc_invert_match(FlatJuniperParser.Poc_invert_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poc_members}.
	 * @param ctx the parse tree
	 */
	void enterPoc_members(FlatJuniperParser.Poc_membersContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poc_members}.
	 * @param ctx the parse tree
	 */
	void exitPoc_members(FlatJuniperParser.Poc_membersContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poplt_apply_path}.
	 * @param ctx the parse tree
	 */
	void enterPoplt_apply_path(FlatJuniperParser.Poplt_apply_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poplt_apply_path}.
	 * @param ctx the parse tree
	 */
	void exitPoplt_apply_path(FlatJuniperParser.Poplt_apply_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poplt_ip6}.
	 * @param ctx the parse tree
	 */
	void enterPoplt_ip6(FlatJuniperParser.Poplt_ip6Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poplt_ip6}.
	 * @param ctx the parse tree
	 */
	void exitPoplt_ip6(FlatJuniperParser.Poplt_ip6Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poplt_network}.
	 * @param ctx the parse tree
	 */
	void enterPoplt_network(FlatJuniperParser.Poplt_networkContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poplt_network}.
	 * @param ctx the parse tree
	 */
	void exitPoplt_network(FlatJuniperParser.Poplt_networkContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#poplt_network6}.
	 * @param ctx the parse tree
	 */
	void enterPoplt_network6(FlatJuniperParser.Poplt_network6Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#poplt_network6}.
	 * @param ctx the parse tree
	 */
	void exitPoplt_network6(FlatJuniperParser.Poplt_network6Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pops_common}.
	 * @param ctx the parse tree
	 */
	void enterPops_common(FlatJuniperParser.Pops_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pops_common}.
	 * @param ctx the parse tree
	 */
	void exitPops_common(FlatJuniperParser.Pops_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pops_from}.
	 * @param ctx the parse tree
	 */
	void enterPops_from(FlatJuniperParser.Pops_fromContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pops_from}.
	 * @param ctx the parse tree
	 */
	void exitPops_from(FlatJuniperParser.Pops_fromContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pops_term}.
	 * @param ctx the parse tree
	 */
	void enterPops_term(FlatJuniperParser.Pops_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pops_term}.
	 * @param ctx the parse tree
	 */
	void exitPops_term(FlatJuniperParser.Pops_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pops_then}.
	 * @param ctx the parse tree
	 */
	void enterPops_then(FlatJuniperParser.Pops_thenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pops_then}.
	 * @param ctx the parse tree
	 */
	void exitPops_then(FlatJuniperParser.Pops_thenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#pops_to}.
	 * @param ctx the parse tree
	 */
	void enterPops_to(FlatJuniperParser.Pops_toContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#pops_to}.
	 * @param ctx the parse tree
	 */
	void exitPops_to(FlatJuniperParser.Pops_toContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_area}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_area(FlatJuniperParser.Popsf_areaContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_area}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_area(FlatJuniperParser.Popsf_areaContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_as_path}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_as_path(FlatJuniperParser.Popsf_as_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_as_path}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_as_path(FlatJuniperParser.Popsf_as_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_as_path_group}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_as_path_group(FlatJuniperParser.Popsf_as_path_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_as_path_group}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_as_path_group(FlatJuniperParser.Popsf_as_path_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_color}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_color(FlatJuniperParser.Popsf_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_color}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_color(FlatJuniperParser.Popsf_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_community}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_community(FlatJuniperParser.Popsf_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_community}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_community(FlatJuniperParser.Popsf_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_family}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_family(FlatJuniperParser.Popsf_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_family}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_family(FlatJuniperParser.Popsf_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_instance}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_instance(FlatJuniperParser.Popsf_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_instance}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_instance(FlatJuniperParser.Popsf_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_interface}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_interface(FlatJuniperParser.Popsf_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_interface}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_interface(FlatJuniperParser.Popsf_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_level}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_level(FlatJuniperParser.Popsf_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_level}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_level(FlatJuniperParser.Popsf_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_local_preference}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_local_preference(FlatJuniperParser.Popsf_local_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_local_preference}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_local_preference(FlatJuniperParser.Popsf_local_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_metric}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_metric(FlatJuniperParser.Popsf_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_metric}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_metric(FlatJuniperParser.Popsf_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_neighbor}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_neighbor(FlatJuniperParser.Popsf_neighborContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_neighbor}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_neighbor(FlatJuniperParser.Popsf_neighborContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_origin}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_origin(FlatJuniperParser.Popsf_originContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_origin}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_origin(FlatJuniperParser.Popsf_originContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_policy}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_policy(FlatJuniperParser.Popsf_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_policy}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_policy(FlatJuniperParser.Popsf_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_prefix_list}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_prefix_list(FlatJuniperParser.Popsf_prefix_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_prefix_list}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_prefix_list(FlatJuniperParser.Popsf_prefix_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_prefix_list_filter}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_prefix_list_filter(FlatJuniperParser.Popsf_prefix_list_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_prefix_list_filter}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_prefix_list_filter(FlatJuniperParser.Popsf_prefix_list_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_protocol}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_protocol(FlatJuniperParser.Popsf_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_protocol}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_protocol(FlatJuniperParser.Popsf_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_rib}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_rib(FlatJuniperParser.Popsf_ribContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_rib}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_rib(FlatJuniperParser.Popsf_ribContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_route_filter}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_route_filter(FlatJuniperParser.Popsf_route_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_route_filter}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_route_filter(FlatJuniperParser.Popsf_route_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_route_type}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_route_type(FlatJuniperParser.Popsf_route_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_route_type}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_route_type(FlatJuniperParser.Popsf_route_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_source_address_filter}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_source_address_filter(FlatJuniperParser.Popsf_source_address_filterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_source_address_filter}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_source_address_filter(FlatJuniperParser.Popsf_source_address_filterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsf_tag}.
	 * @param ctx the parse tree
	 */
	void enterPopsf_tag(FlatJuniperParser.Popsf_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsf_tag}.
	 * @param ctx the parse tree
	 */
	void exitPopsf_tag(FlatJuniperParser.Popsf_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfpl_exact}.
	 * @param ctx the parse tree
	 */
	void enterPopsfpl_exact(FlatJuniperParser.Popsfpl_exactContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfpl_exact}.
	 * @param ctx the parse tree
	 */
	void exitPopsfpl_exact(FlatJuniperParser.Popsfpl_exactContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfpl_longer}.
	 * @param ctx the parse tree
	 */
	void enterPopsfpl_longer(FlatJuniperParser.Popsfpl_longerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfpl_longer}.
	 * @param ctx the parse tree
	 */
	void exitPopsfpl_longer(FlatJuniperParser.Popsfpl_longerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfpl_orlonger}.
	 * @param ctx the parse tree
	 */
	void enterPopsfpl_orlonger(FlatJuniperParser.Popsfpl_orlongerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfpl_orlonger}.
	 * @param ctx the parse tree
	 */
	void exitPopsfpl_orlonger(FlatJuniperParser.Popsfpl_orlongerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_common}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_common(FlatJuniperParser.Popsfrf_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_common}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_common(FlatJuniperParser.Popsfrf_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_address_mask}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_address_mask(FlatJuniperParser.Popsfrf_address_maskContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_address_mask}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_address_mask(FlatJuniperParser.Popsfrf_address_maskContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_exact}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_exact(FlatJuniperParser.Popsfrf_exactContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_exact}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_exact(FlatJuniperParser.Popsfrf_exactContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_longer}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_longer(FlatJuniperParser.Popsfrf_longerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_longer}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_longer(FlatJuniperParser.Popsfrf_longerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_orlonger}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_orlonger(FlatJuniperParser.Popsfrf_orlongerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_orlonger}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_orlonger(FlatJuniperParser.Popsfrf_orlongerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_prefix_length_range}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_prefix_length_range(FlatJuniperParser.Popsfrf_prefix_length_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_prefix_length_range}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_prefix_length_range(FlatJuniperParser.Popsfrf_prefix_length_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_then}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_then(FlatJuniperParser.Popsfrf_thenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_then}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_then(FlatJuniperParser.Popsfrf_thenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_through}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_through(FlatJuniperParser.Popsfrf_throughContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_through}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_through(FlatJuniperParser.Popsfrf_throughContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsfrf_upto}.
	 * @param ctx the parse tree
	 */
	void enterPopsfrf_upto(FlatJuniperParser.Popsfrf_uptoContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsfrf_upto}.
	 * @param ctx the parse tree
	 */
	void exitPopsfrf_upto(FlatJuniperParser.Popsfrf_uptoContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_accept}.
	 * @param ctx the parse tree
	 */
	void enterPopst_accept(FlatJuniperParser.Popst_acceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_accept}.
	 * @param ctx the parse tree
	 */
	void exitPopst_accept(FlatJuniperParser.Popst_acceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_as_path_expand}.
	 * @param ctx the parse tree
	 */
	void enterPopst_as_path_expand(FlatJuniperParser.Popst_as_path_expandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_as_path_expand}.
	 * @param ctx the parse tree
	 */
	void exitPopst_as_path_expand(FlatJuniperParser.Popst_as_path_expandContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_as_path_prepend}.
	 * @param ctx the parse tree
	 */
	void enterPopst_as_path_prepend(FlatJuniperParser.Popst_as_path_prependContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_as_path_prepend}.
	 * @param ctx the parse tree
	 */
	void exitPopst_as_path_prepend(FlatJuniperParser.Popst_as_path_prependContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_color}.
	 * @param ctx the parse tree
	 */
	void enterPopst_color(FlatJuniperParser.Popst_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_color}.
	 * @param ctx the parse tree
	 */
	void exitPopst_color(FlatJuniperParser.Popst_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_color2}.
	 * @param ctx the parse tree
	 */
	void enterPopst_color2(FlatJuniperParser.Popst_color2Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_color2}.
	 * @param ctx the parse tree
	 */
	void exitPopst_color2(FlatJuniperParser.Popst_color2Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_common}.
	 * @param ctx the parse tree
	 */
	void enterPopst_common(FlatJuniperParser.Popst_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_common}.
	 * @param ctx the parse tree
	 */
	void exitPopst_common(FlatJuniperParser.Popst_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_community_add}.
	 * @param ctx the parse tree
	 */
	void enterPopst_community_add(FlatJuniperParser.Popst_community_addContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_community_add}.
	 * @param ctx the parse tree
	 */
	void exitPopst_community_add(FlatJuniperParser.Popst_community_addContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_community_delete}.
	 * @param ctx the parse tree
	 */
	void enterPopst_community_delete(FlatJuniperParser.Popst_community_deleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_community_delete}.
	 * @param ctx the parse tree
	 */
	void exitPopst_community_delete(FlatJuniperParser.Popst_community_deleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_community_set}.
	 * @param ctx the parse tree
	 */
	void enterPopst_community_set(FlatJuniperParser.Popst_community_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_community_set}.
	 * @param ctx the parse tree
	 */
	void exitPopst_community_set(FlatJuniperParser.Popst_community_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_cos_next_hop_map}.
	 * @param ctx the parse tree
	 */
	void enterPopst_cos_next_hop_map(FlatJuniperParser.Popst_cos_next_hop_mapContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_cos_next_hop_map}.
	 * @param ctx the parse tree
	 */
	void exitPopst_cos_next_hop_map(FlatJuniperParser.Popst_cos_next_hop_mapContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_default_action_accept}.
	 * @param ctx the parse tree
	 */
	void enterPopst_default_action_accept(FlatJuniperParser.Popst_default_action_acceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_default_action_accept}.
	 * @param ctx the parse tree
	 */
	void exitPopst_default_action_accept(FlatJuniperParser.Popst_default_action_acceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_default_action_reject}.
	 * @param ctx the parse tree
	 */
	void enterPopst_default_action_reject(FlatJuniperParser.Popst_default_action_rejectContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_default_action_reject}.
	 * @param ctx the parse tree
	 */
	void exitPopst_default_action_reject(FlatJuniperParser.Popst_default_action_rejectContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_external}.
	 * @param ctx the parse tree
	 */
	void enterPopst_external(FlatJuniperParser.Popst_externalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_external}.
	 * @param ctx the parse tree
	 */
	void exitPopst_external(FlatJuniperParser.Popst_externalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_forwarding_class}.
	 * @param ctx the parse tree
	 */
	void enterPopst_forwarding_class(FlatJuniperParser.Popst_forwarding_classContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_forwarding_class}.
	 * @param ctx the parse tree
	 */
	void exitPopst_forwarding_class(FlatJuniperParser.Popst_forwarding_classContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_install_nexthop}.
	 * @param ctx the parse tree
	 */
	void enterPopst_install_nexthop(FlatJuniperParser.Popst_install_nexthopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_install_nexthop}.
	 * @param ctx the parse tree
	 */
	void exitPopst_install_nexthop(FlatJuniperParser.Popst_install_nexthopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_local_preference}.
	 * @param ctx the parse tree
	 */
	void enterPopst_local_preference(FlatJuniperParser.Popst_local_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_local_preference}.
	 * @param ctx the parse tree
	 */
	void exitPopst_local_preference(FlatJuniperParser.Popst_local_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric(FlatJuniperParser.Popst_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric(FlatJuniperParser.Popst_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric_add}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric_add(FlatJuniperParser.Popst_metric_addContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric_add}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric_add(FlatJuniperParser.Popst_metric_addContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric2}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric2(FlatJuniperParser.Popst_metric2Context ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric2}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric2(FlatJuniperParser.Popst_metric2Context ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric_expression}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric_expression(FlatJuniperParser.Popst_metric_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric_expression}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric_expression(FlatJuniperParser.Popst_metric_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric_igp}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric_igp(FlatJuniperParser.Popst_metric_igpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric_igp}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric_igp(FlatJuniperParser.Popst_metric_igpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_metric2_expression}.
	 * @param ctx the parse tree
	 */
	void enterPopst_metric2_expression(FlatJuniperParser.Popst_metric2_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_metric2_expression}.
	 * @param ctx the parse tree
	 */
	void exitPopst_metric2_expression(FlatJuniperParser.Popst_metric2_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_next_hop}.
	 * @param ctx the parse tree
	 */
	void enterPopst_next_hop(FlatJuniperParser.Popst_next_hopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_next_hop}.
	 * @param ctx the parse tree
	 */
	void exitPopst_next_hop(FlatJuniperParser.Popst_next_hopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_next_hop_self}.
	 * @param ctx the parse tree
	 */
	void enterPopst_next_hop_self(FlatJuniperParser.Popst_next_hop_selfContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_next_hop_self}.
	 * @param ctx the parse tree
	 */
	void exitPopst_next_hop_self(FlatJuniperParser.Popst_next_hop_selfContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_next_policy}.
	 * @param ctx the parse tree
	 */
	void enterPopst_next_policy(FlatJuniperParser.Popst_next_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_next_policy}.
	 * @param ctx the parse tree
	 */
	void exitPopst_next_policy(FlatJuniperParser.Popst_next_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_next_term}.
	 * @param ctx the parse tree
	 */
	void enterPopst_next_term(FlatJuniperParser.Popst_next_termContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_next_term}.
	 * @param ctx the parse tree
	 */
	void exitPopst_next_term(FlatJuniperParser.Popst_next_termContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_null}.
	 * @param ctx the parse tree
	 */
	void enterPopst_null(FlatJuniperParser.Popst_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_null}.
	 * @param ctx the parse tree
	 */
	void exitPopst_null(FlatJuniperParser.Popst_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_origin}.
	 * @param ctx the parse tree
	 */
	void enterPopst_origin(FlatJuniperParser.Popst_originContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_origin}.
	 * @param ctx the parse tree
	 */
	void exitPopst_origin(FlatJuniperParser.Popst_originContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_preference}.
	 * @param ctx the parse tree
	 */
	void enterPopst_preference(FlatJuniperParser.Popst_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_preference}.
	 * @param ctx the parse tree
	 */
	void exitPopst_preference(FlatJuniperParser.Popst_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_priority}.
	 * @param ctx the parse tree
	 */
	void enterPopst_priority(FlatJuniperParser.Popst_priorityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_priority}.
	 * @param ctx the parse tree
	 */
	void exitPopst_priority(FlatJuniperParser.Popst_priorityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_reject}.
	 * @param ctx the parse tree
	 */
	void enterPopst_reject(FlatJuniperParser.Popst_rejectContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_reject}.
	 * @param ctx the parse tree
	 */
	void exitPopst_reject(FlatJuniperParser.Popst_rejectContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popst_tag}.
	 * @param ctx the parse tree
	 */
	void enterPopst_tag(FlatJuniperParser.Popst_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popst_tag}.
	 * @param ctx the parse tree
	 */
	void exitPopst_tag(FlatJuniperParser.Popst_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popstc_add_color}.
	 * @param ctx the parse tree
	 */
	void enterPopstc_add_color(FlatJuniperParser.Popstc_add_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popstc_add_color}.
	 * @param ctx the parse tree
	 */
	void exitPopstc_add_color(FlatJuniperParser.Popstc_add_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popstc_color}.
	 * @param ctx the parse tree
	 */
	void enterPopstc_color(FlatJuniperParser.Popstc_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popstc_color}.
	 * @param ctx the parse tree
	 */
	void exitPopstc_color(FlatJuniperParser.Popstc_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popstc2_add_color}.
	 * @param ctx the parse tree
	 */
	void enterPopstc2_add_color(FlatJuniperParser.Popstc2_add_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popstc2_add_color}.
	 * @param ctx the parse tree
	 */
	void exitPopstc2_add_color(FlatJuniperParser.Popstc2_add_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popstc2_color}.
	 * @param ctx the parse tree
	 */
	void enterPopstc2_color(FlatJuniperParser.Popstc2_colorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popstc2_color}.
	 * @param ctx the parse tree
	 */
	void exitPopstc2_color(FlatJuniperParser.Popstc2_colorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsto_level}.
	 * @param ctx the parse tree
	 */
	void enterPopsto_level(FlatJuniperParser.Popsto_levelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsto_level}.
	 * @param ctx the parse tree
	 */
	void exitPopsto_level(FlatJuniperParser.Popsto_levelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#popsto_rib}.
	 * @param ctx the parse tree
	 */
	void enterPopsto_rib(FlatJuniperParser.Popsto_ribContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#popsto_rib}.
	 * @param ctx the parse tree
	 */
	void exitPopsto_rib(FlatJuniperParser.Popsto_ribContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_policy_options}.
	 * @param ctx the parse tree
	 */
	void enterS_policy_options(FlatJuniperParser.S_policy_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_policy_options}.
	 * @param ctx the parse tree
	 */
	void exitS_policy_options(FlatJuniperParser.S_policy_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_common}.
	 * @param ctx the parse tree
	 */
	void enterRi_common(FlatJuniperParser.Ri_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_common}.
	 * @param ctx the parse tree
	 */
	void exitRi_common(FlatJuniperParser.Ri_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_description}.
	 * @param ctx the parse tree
	 */
	void enterRi_description(FlatJuniperParser.Ri_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_description}.
	 * @param ctx the parse tree
	 */
	void exitRi_description(FlatJuniperParser.Ri_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_instance_type}.
	 * @param ctx the parse tree
	 */
	void enterRi_instance_type(FlatJuniperParser.Ri_instance_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_instance_type}.
	 * @param ctx the parse tree
	 */
	void exitRi_instance_type(FlatJuniperParser.Ri_instance_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_interface}.
	 * @param ctx the parse tree
	 */
	void enterRi_interface(FlatJuniperParser.Ri_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_interface}.
	 * @param ctx the parse tree
	 */
	void exitRi_interface(FlatJuniperParser.Ri_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_named_routing_instance}.
	 * @param ctx the parse tree
	 */
	void enterRi_named_routing_instance(FlatJuniperParser.Ri_named_routing_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_named_routing_instance}.
	 * @param ctx the parse tree
	 */
	void exitRi_named_routing_instance(FlatJuniperParser.Ri_named_routing_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_null}.
	 * @param ctx the parse tree
	 */
	void enterRi_null(FlatJuniperParser.Ri_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_null}.
	 * @param ctx the parse tree
	 */
	void exitRi_null(FlatJuniperParser.Ri_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_protocols}.
	 * @param ctx the parse tree
	 */
	void enterRi_protocols(FlatJuniperParser.Ri_protocolsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_protocols}.
	 * @param ctx the parse tree
	 */
	void exitRi_protocols(FlatJuniperParser.Ri_protocolsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_route_distinguisher}.
	 * @param ctx the parse tree
	 */
	void enterRi_route_distinguisher(FlatJuniperParser.Ri_route_distinguisherContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_route_distinguisher}.
	 * @param ctx the parse tree
	 */
	void exitRi_route_distinguisher(FlatJuniperParser.Ri_route_distinguisherContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_snmp}.
	 * @param ctx the parse tree
	 */
	void enterRi_snmp(FlatJuniperParser.Ri_snmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_snmp}.
	 * @param ctx the parse tree
	 */
	void exitRi_snmp(FlatJuniperParser.Ri_snmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_vrf_export}.
	 * @param ctx the parse tree
	 */
	void enterRi_vrf_export(FlatJuniperParser.Ri_vrf_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_vrf_export}.
	 * @param ctx the parse tree
	 */
	void exitRi_vrf_export(FlatJuniperParser.Ri_vrf_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_vrf_import}.
	 * @param ctx the parse tree
	 */
	void enterRi_vrf_import(FlatJuniperParser.Ri_vrf_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_vrf_import}.
	 * @param ctx the parse tree
	 */
	void exitRi_vrf_import(FlatJuniperParser.Ri_vrf_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_vrf_table_label}.
	 * @param ctx the parse tree
	 */
	void enterRi_vrf_table_label(FlatJuniperParser.Ri_vrf_table_labelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_vrf_table_label}.
	 * @param ctx the parse tree
	 */
	void exitRi_vrf_table_label(FlatJuniperParser.Ri_vrf_table_labelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_vrf_target}.
	 * @param ctx the parse tree
	 */
	void enterRi_vrf_target(FlatJuniperParser.Ri_vrf_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_vrf_target}.
	 * @param ctx the parse tree
	 */
	void exitRi_vrf_target(FlatJuniperParser.Ri_vrf_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ri_vtep_source_interface}.
	 * @param ctx the parse tree
	 */
	void enterRi_vtep_source_interface(FlatJuniperParser.Ri_vtep_source_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ri_vtep_source_interface}.
	 * @param ctx the parse tree
	 */
	void exitRi_vtep_source_interface(FlatJuniperParser.Ri_vtep_source_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#riv_community}.
	 * @param ctx the parse tree
	 */
	void enterRiv_community(FlatJuniperParser.Riv_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#riv_community}.
	 * @param ctx the parse tree
	 */
	void exitRiv_community(FlatJuniperParser.Riv_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#riv_export}.
	 * @param ctx the parse tree
	 */
	void enterRiv_export(FlatJuniperParser.Riv_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#riv_export}.
	 * @param ctx the parse tree
	 */
	void exitRiv_export(FlatJuniperParser.Riv_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#riv_import}.
	 * @param ctx the parse tree
	 */
	void enterRiv_import(FlatJuniperParser.Riv_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#riv_import}.
	 * @param ctx the parse tree
	 */
	void exitRiv_import(FlatJuniperParser.Riv_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_aggregate}.
	 * @param ctx the parse tree
	 */
	void enterRo_aggregate(FlatJuniperParser.Ro_aggregateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_aggregate}.
	 * @param ctx the parse tree
	 */
	void exitRo_aggregate(FlatJuniperParser.Ro_aggregateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_auto_export}.
	 * @param ctx the parse tree
	 */
	void enterRo_auto_export(FlatJuniperParser.Ro_auto_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_auto_export}.
	 * @param ctx the parse tree
	 */
	void exitRo_auto_export(FlatJuniperParser.Ro_auto_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_autonomous_system}.
	 * @param ctx the parse tree
	 */
	void enterRo_autonomous_system(FlatJuniperParser.Ro_autonomous_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_autonomous_system}.
	 * @param ctx the parse tree
	 */
	void exitRo_autonomous_system(FlatJuniperParser.Ro_autonomous_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_bmp}.
	 * @param ctx the parse tree
	 */
	void enterRo_bmp(FlatJuniperParser.Ro_bmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_bmp}.
	 * @param ctx the parse tree
	 */
	void exitRo_bmp(FlatJuniperParser.Ro_bmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_confederation}.
	 * @param ctx the parse tree
	 */
	void enterRo_confederation(FlatJuniperParser.Ro_confederationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_confederation}.
	 * @param ctx the parse tree
	 */
	void exitRo_confederation(FlatJuniperParser.Ro_confederationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_forwarding_table}.
	 * @param ctx the parse tree
	 */
	void enterRo_forwarding_table(FlatJuniperParser.Ro_forwarding_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_forwarding_table}.
	 * @param ctx the parse tree
	 */
	void exitRo_forwarding_table(FlatJuniperParser.Ro_forwarding_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_generate}.
	 * @param ctx the parse tree
	 */
	void enterRo_generate(FlatJuniperParser.Ro_generateContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_generate}.
	 * @param ctx the parse tree
	 */
	void exitRo_generate(FlatJuniperParser.Ro_generateContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_instance_import}.
	 * @param ctx the parse tree
	 */
	void enterRo_instance_import(FlatJuniperParser.Ro_instance_importContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_instance_import}.
	 * @param ctx the parse tree
	 */
	void exitRo_instance_import(FlatJuniperParser.Ro_instance_importContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_interface_routes}.
	 * @param ctx the parse tree
	 */
	void enterRo_interface_routes(FlatJuniperParser.Ro_interface_routesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_interface_routes}.
	 * @param ctx the parse tree
	 */
	void exitRo_interface_routes(FlatJuniperParser.Ro_interface_routesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_martians}.
	 * @param ctx the parse tree
	 */
	void enterRo_martians(FlatJuniperParser.Ro_martiansContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_martians}.
	 * @param ctx the parse tree
	 */
	void exitRo_martians(FlatJuniperParser.Ro_martiansContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_null}.
	 * @param ctx the parse tree
	 */
	void enterRo_null(FlatJuniperParser.Ro_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_null}.
	 * @param ctx the parse tree
	 */
	void exitRo_null(FlatJuniperParser.Ro_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_rib}.
	 * @param ctx the parse tree
	 */
	void enterRo_rib(FlatJuniperParser.Ro_ribContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_rib}.
	 * @param ctx the parse tree
	 */
	void exitRo_rib(FlatJuniperParser.Ro_ribContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_rib_groups}.
	 * @param ctx the parse tree
	 */
	void enterRo_rib_groups(FlatJuniperParser.Ro_rib_groupsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_rib_groups}.
	 * @param ctx the parse tree
	 */
	void exitRo_rib_groups(FlatJuniperParser.Ro_rib_groupsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_route_distinguisher_id}.
	 * @param ctx the parse tree
	 */
	void enterRo_route_distinguisher_id(FlatJuniperParser.Ro_route_distinguisher_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_route_distinguisher_id}.
	 * @param ctx the parse tree
	 */
	void exitRo_route_distinguisher_id(FlatJuniperParser.Ro_route_distinguisher_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_router_id}.
	 * @param ctx the parse tree
	 */
	void enterRo_router_id(FlatJuniperParser.Ro_router_idContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_router_id}.
	 * @param ctx the parse tree
	 */
	void exitRo_router_id(FlatJuniperParser.Ro_router_idContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_srlg}.
	 * @param ctx the parse tree
	 */
	void enterRo_srlg(FlatJuniperParser.Ro_srlgContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_srlg}.
	 * @param ctx the parse tree
	 */
	void exitRo_srlg(FlatJuniperParser.Ro_srlgContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ro_static}.
	 * @param ctx the parse tree
	 */
	void enterRo_static(FlatJuniperParser.Ro_staticContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ro_static}.
	 * @param ctx the parse tree
	 */
	void exitRo_static(FlatJuniperParser.Ro_staticContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_active}.
	 * @param ctx the parse tree
	 */
	void enterRoa_active(FlatJuniperParser.Roa_activeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_active}.
	 * @param ctx the parse tree
	 */
	void exitRoa_active(FlatJuniperParser.Roa_activeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_as_path}.
	 * @param ctx the parse tree
	 */
	void enterRoa_as_path(FlatJuniperParser.Roa_as_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_as_path}.
	 * @param ctx the parse tree
	 */
	void exitRoa_as_path(FlatJuniperParser.Roa_as_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_common}.
	 * @param ctx the parse tree
	 */
	void enterRoa_common(FlatJuniperParser.Roa_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_common}.
	 * @param ctx the parse tree
	 */
	void exitRoa_common(FlatJuniperParser.Roa_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_community}.
	 * @param ctx the parse tree
	 */
	void enterRoa_community(FlatJuniperParser.Roa_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_community}.
	 * @param ctx the parse tree
	 */
	void exitRoa_community(FlatJuniperParser.Roa_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_defaults}.
	 * @param ctx the parse tree
	 */
	void enterRoa_defaults(FlatJuniperParser.Roa_defaultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_defaults}.
	 * @param ctx the parse tree
	 */
	void exitRoa_defaults(FlatJuniperParser.Roa_defaultsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_discard}.
	 * @param ctx the parse tree
	 */
	void enterRoa_discard(FlatJuniperParser.Roa_discardContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_discard}.
	 * @param ctx the parse tree
	 */
	void exitRoa_discard(FlatJuniperParser.Roa_discardContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_passive}.
	 * @param ctx the parse tree
	 */
	void enterRoa_passive(FlatJuniperParser.Roa_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_passive}.
	 * @param ctx the parse tree
	 */
	void exitRoa_passive(FlatJuniperParser.Roa_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_policy}.
	 * @param ctx the parse tree
	 */
	void enterRoa_policy(FlatJuniperParser.Roa_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_policy}.
	 * @param ctx the parse tree
	 */
	void exitRoa_policy(FlatJuniperParser.Roa_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_preference}.
	 * @param ctx the parse tree
	 */
	void enterRoa_preference(FlatJuniperParser.Roa_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_preference}.
	 * @param ctx the parse tree
	 */
	void exitRoa_preference(FlatJuniperParser.Roa_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_route}.
	 * @param ctx the parse tree
	 */
	void enterRoa_route(FlatJuniperParser.Roa_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_route}.
	 * @param ctx the parse tree
	 */
	void exitRoa_route(FlatJuniperParser.Roa_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roa_tag}.
	 * @param ctx the parse tree
	 */
	void enterRoa_tag(FlatJuniperParser.Roa_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roa_tag}.
	 * @param ctx the parse tree
	 */
	void exitRoa_tag(FlatJuniperParser.Roa_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roaa_aggregator}.
	 * @param ctx the parse tree
	 */
	void enterRoaa_aggregator(FlatJuniperParser.Roaa_aggregatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roaa_aggregator}.
	 * @param ctx the parse tree
	 */
	void exitRoaa_aggregator(FlatJuniperParser.Roaa_aggregatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roaa_origin}.
	 * @param ctx the parse tree
	 */
	void enterRoaa_origin(FlatJuniperParser.Roaa_originContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roaa_origin}.
	 * @param ctx the parse tree
	 */
	void exitRoaa_origin(FlatJuniperParser.Roaa_originContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roaa_path}.
	 * @param ctx the parse tree
	 */
	void enterRoaa_path(FlatJuniperParser.Roaa_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roaa_path}.
	 * @param ctx the parse tree
	 */
	void exitRoaa_path(FlatJuniperParser.Roaa_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roas_asdot_notation}.
	 * @param ctx the parse tree
	 */
	void enterRoas_asdot_notation(FlatJuniperParser.Roas_asdot_notationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roas_asdot_notation}.
	 * @param ctx the parse tree
	 */
	void exitRoas_asdot_notation(FlatJuniperParser.Roas_asdot_notationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roas_loops}.
	 * @param ctx the parse tree
	 */
	void enterRoas_loops(FlatJuniperParser.Roas_loopsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roas_loops}.
	 * @param ctx the parse tree
	 */
	void exitRoas_loops(FlatJuniperParser.Roas_loopsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rob_station_address}.
	 * @param ctx the parse tree
	 */
	void enterRob_station_address(FlatJuniperParser.Rob_station_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rob_station_address}.
	 * @param ctx the parse tree
	 */
	void exitRob_station_address(FlatJuniperParser.Rob_station_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rob_station_port}.
	 * @param ctx the parse tree
	 */
	void enterRob_station_port(FlatJuniperParser.Rob_station_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rob_station_port}.
	 * @param ctx the parse tree
	 */
	void exitRob_station_port(FlatJuniperParser.Rob_station_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rof_export}.
	 * @param ctx the parse tree
	 */
	void enterRof_export(FlatJuniperParser.Rof_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rof_export}.
	 * @param ctx the parse tree
	 */
	void exitRof_export(FlatJuniperParser.Rof_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rof_no_ecmp_fast_reroute}.
	 * @param ctx the parse tree
	 */
	void enterRof_no_ecmp_fast_reroute(FlatJuniperParser.Rof_no_ecmp_fast_rerouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rof_no_ecmp_fast_reroute}.
	 * @param ctx the parse tree
	 */
	void exitRof_no_ecmp_fast_reroute(FlatJuniperParser.Rof_no_ecmp_fast_rerouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rof_null}.
	 * @param ctx the parse tree
	 */
	void enterRof_null(FlatJuniperParser.Rof_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rof_null}.
	 * @param ctx the parse tree
	 */
	void exitRof_null(FlatJuniperParser.Rof_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_active}.
	 * @param ctx the parse tree
	 */
	void enterRog_active(FlatJuniperParser.Rog_activeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_active}.
	 * @param ctx the parse tree
	 */
	void exitRog_active(FlatJuniperParser.Rog_activeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_common}.
	 * @param ctx the parse tree
	 */
	void enterRog_common(FlatJuniperParser.Rog_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_common}.
	 * @param ctx the parse tree
	 */
	void exitRog_common(FlatJuniperParser.Rog_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_community}.
	 * @param ctx the parse tree
	 */
	void enterRog_community(FlatJuniperParser.Rog_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_community}.
	 * @param ctx the parse tree
	 */
	void exitRog_community(FlatJuniperParser.Rog_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_defaults}.
	 * @param ctx the parse tree
	 */
	void enterRog_defaults(FlatJuniperParser.Rog_defaultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_defaults}.
	 * @param ctx the parse tree
	 */
	void exitRog_defaults(FlatJuniperParser.Rog_defaultsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_discard}.
	 * @param ctx the parse tree
	 */
	void enterRog_discard(FlatJuniperParser.Rog_discardContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_discard}.
	 * @param ctx the parse tree
	 */
	void exitRog_discard(FlatJuniperParser.Rog_discardContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_metric}.
	 * @param ctx the parse tree
	 */
	void enterRog_metric(FlatJuniperParser.Rog_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_metric}.
	 * @param ctx the parse tree
	 */
	void exitRog_metric(FlatJuniperParser.Rog_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_passive}.
	 * @param ctx the parse tree
	 */
	void enterRog_passive(FlatJuniperParser.Rog_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_passive}.
	 * @param ctx the parse tree
	 */
	void exitRog_passive(FlatJuniperParser.Rog_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_policy}.
	 * @param ctx the parse tree
	 */
	void enterRog_policy(FlatJuniperParser.Rog_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_policy}.
	 * @param ctx the parse tree
	 */
	void exitRog_policy(FlatJuniperParser.Rog_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rog_route}.
	 * @param ctx the parse tree
	 */
	void enterRog_route(FlatJuniperParser.Rog_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rog_route}.
	 * @param ctx the parse tree
	 */
	void exitRog_route(FlatJuniperParser.Rog_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roi_family}.
	 * @param ctx the parse tree
	 */
	void enterRoi_family(FlatJuniperParser.Roi_familyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roi_family}.
	 * @param ctx the parse tree
	 */
	void exitRoi_family(FlatJuniperParser.Roi_familyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roi_rib_group}.
	 * @param ctx the parse tree
	 */
	void enterRoi_rib_group(FlatJuniperParser.Roi_rib_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roi_rib_group}.
	 * @param ctx the parse tree
	 */
	void exitRoi_rib_group(FlatJuniperParser.Roi_rib_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roif_inet}.
	 * @param ctx the parse tree
	 */
	void enterRoif_inet(FlatJuniperParser.Roif_inetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roif_inet}.
	 * @param ctx the parse tree
	 */
	void exitRoif_inet(FlatJuniperParser.Roif_inetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roif_null}.
	 * @param ctx the parse tree
	 */
	void enterRoif_null(FlatJuniperParser.Roif_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roif_null}.
	 * @param ctx the parse tree
	 */
	void exitRoif_null(FlatJuniperParser.Roif_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roifi_export}.
	 * @param ctx the parse tree
	 */
	void enterRoifi_export(FlatJuniperParser.Roifi_exportContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roifi_export}.
	 * @param ctx the parse tree
	 */
	void exitRoifi_export(FlatJuniperParser.Roifi_exportContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roifie_lan}.
	 * @param ctx the parse tree
	 */
	void enterRoifie_lan(FlatJuniperParser.Roifie_lanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roifie_lan}.
	 * @param ctx the parse tree
	 */
	void exitRoifie_lan(FlatJuniperParser.Roifie_lanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roifie_point_to_point}.
	 * @param ctx the parse tree
	 */
	void enterRoifie_point_to_point(FlatJuniperParser.Roifie_point_to_pointContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roifie_point_to_point}.
	 * @param ctx the parse tree
	 */
	void exitRoifie_point_to_point(FlatJuniperParser.Roifie_point_to_pointContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ror_export_rib}.
	 * @param ctx the parse tree
	 */
	void enterRor_export_rib(FlatJuniperParser.Ror_export_ribContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ror_export_rib}.
	 * @param ctx the parse tree
	 */
	void exitRor_export_rib(FlatJuniperParser.Ror_export_ribContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ror_import_policy}.
	 * @param ctx the parse tree
	 */
	void enterRor_import_policy(FlatJuniperParser.Ror_import_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ror_import_policy}.
	 * @param ctx the parse tree
	 */
	void exitRor_import_policy(FlatJuniperParser.Ror_import_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ror_import_rib}.
	 * @param ctx the parse tree
	 */
	void enterRor_import_rib(FlatJuniperParser.Ror_import_ribContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ror_import_rib}.
	 * @param ctx the parse tree
	 */
	void exitRor_import_rib(FlatJuniperParser.Ror_import_ribContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ros_rib_group}.
	 * @param ctx the parse tree
	 */
	void enterRos_rib_group(FlatJuniperParser.Ros_rib_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ros_rib_group}.
	 * @param ctx the parse tree
	 */
	void exitRos_rib_group(FlatJuniperParser.Ros_rib_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ros_route}.
	 * @param ctx the parse tree
	 */
	void enterRos_route(FlatJuniperParser.Ros_routeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ros_route}.
	 * @param ctx the parse tree
	 */
	void exitRos_route(FlatJuniperParser.Ros_routeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roslrg_srlg_cost}.
	 * @param ctx the parse tree
	 */
	void enterRoslrg_srlg_cost(FlatJuniperParser.Roslrg_srlg_costContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roslrg_srlg_cost}.
	 * @param ctx the parse tree
	 */
	void exitRoslrg_srlg_cost(FlatJuniperParser.Roslrg_srlg_costContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#roslrg_srlg_value}.
	 * @param ctx the parse tree
	 */
	void enterRoslrg_srlg_value(FlatJuniperParser.Roslrg_srlg_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#roslrg_srlg_value}.
	 * @param ctx the parse tree
	 */
	void exitRoslrg_srlg_value(FlatJuniperParser.Roslrg_srlg_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_active}.
	 * @param ctx the parse tree
	 */
	void enterRosr_active(FlatJuniperParser.Rosr_activeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_active}.
	 * @param ctx the parse tree
	 */
	void exitRosr_active(FlatJuniperParser.Rosr_activeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_as_path}.
	 * @param ctx the parse tree
	 */
	void enterRosr_as_path(FlatJuniperParser.Rosr_as_pathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_as_path}.
	 * @param ctx the parse tree
	 */
	void exitRosr_as_path(FlatJuniperParser.Rosr_as_pathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_common}.
	 * @param ctx the parse tree
	 */
	void enterRosr_common(FlatJuniperParser.Rosr_commonContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_common}.
	 * @param ctx the parse tree
	 */
	void exitRosr_common(FlatJuniperParser.Rosr_commonContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_community}.
	 * @param ctx the parse tree
	 */
	void enterRosr_community(FlatJuniperParser.Rosr_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_community}.
	 * @param ctx the parse tree
	 */
	void exitRosr_community(FlatJuniperParser.Rosr_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_discard}.
	 * @param ctx the parse tree
	 */
	void enterRosr_discard(FlatJuniperParser.Rosr_discardContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_discard}.
	 * @param ctx the parse tree
	 */
	void exitRosr_discard(FlatJuniperParser.Rosr_discardContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_install}.
	 * @param ctx the parse tree
	 */
	void enterRosr_install(FlatJuniperParser.Rosr_installContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_install}.
	 * @param ctx the parse tree
	 */
	void exitRosr_install(FlatJuniperParser.Rosr_installContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_metric}.
	 * @param ctx the parse tree
	 */
	void enterRosr_metric(FlatJuniperParser.Rosr_metricContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_metric}.
	 * @param ctx the parse tree
	 */
	void exitRosr_metric(FlatJuniperParser.Rosr_metricContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_next_hop}.
	 * @param ctx the parse tree
	 */
	void enterRosr_next_hop(FlatJuniperParser.Rosr_next_hopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_next_hop}.
	 * @param ctx the parse tree
	 */
	void exitRosr_next_hop(FlatJuniperParser.Rosr_next_hopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_next_table}.
	 * @param ctx the parse tree
	 */
	void enterRosr_next_table(FlatJuniperParser.Rosr_next_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_next_table}.
	 * @param ctx the parse tree
	 */
	void exitRosr_next_table(FlatJuniperParser.Rosr_next_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_no_install}.
	 * @param ctx the parse tree
	 */
	void enterRosr_no_install(FlatJuniperParser.Rosr_no_installContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_no_install}.
	 * @param ctx the parse tree
	 */
	void exitRosr_no_install(FlatJuniperParser.Rosr_no_installContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_no_readvertise}.
	 * @param ctx the parse tree
	 */
	void enterRosr_no_readvertise(FlatJuniperParser.Rosr_no_readvertiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_no_readvertise}.
	 * @param ctx the parse tree
	 */
	void exitRosr_no_readvertise(FlatJuniperParser.Rosr_no_readvertiseContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_no_retain}.
	 * @param ctx the parse tree
	 */
	void enterRosr_no_retain(FlatJuniperParser.Rosr_no_retainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_no_retain}.
	 * @param ctx the parse tree
	 */
	void exitRosr_no_retain(FlatJuniperParser.Rosr_no_retainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_passive}.
	 * @param ctx the parse tree
	 */
	void enterRosr_passive(FlatJuniperParser.Rosr_passiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_passive}.
	 * @param ctx the parse tree
	 */
	void exitRosr_passive(FlatJuniperParser.Rosr_passiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_preference}.
	 * @param ctx the parse tree
	 */
	void enterRosr_preference(FlatJuniperParser.Rosr_preferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_preference}.
	 * @param ctx the parse tree
	 */
	void exitRosr_preference(FlatJuniperParser.Rosr_preferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_qualified_next_hop}.
	 * @param ctx the parse tree
	 */
	void enterRosr_qualified_next_hop(FlatJuniperParser.Rosr_qualified_next_hopContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_qualified_next_hop}.
	 * @param ctx the parse tree
	 */
	void exitRosr_qualified_next_hop(FlatJuniperParser.Rosr_qualified_next_hopContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_readvertise}.
	 * @param ctx the parse tree
	 */
	void enterRosr_readvertise(FlatJuniperParser.Rosr_readvertiseContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_readvertise}.
	 * @param ctx the parse tree
	 */
	void exitRosr_readvertise(FlatJuniperParser.Rosr_readvertiseContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_reject}.
	 * @param ctx the parse tree
	 */
	void enterRosr_reject(FlatJuniperParser.Rosr_rejectContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_reject}.
	 * @param ctx the parse tree
	 */
	void exitRosr_reject(FlatJuniperParser.Rosr_rejectContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_resolve}.
	 * @param ctx the parse tree
	 */
	void enterRosr_resolve(FlatJuniperParser.Rosr_resolveContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_resolve}.
	 * @param ctx the parse tree
	 */
	void exitRosr_resolve(FlatJuniperParser.Rosr_resolveContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_retain}.
	 * @param ctx the parse tree
	 */
	void enterRosr_retain(FlatJuniperParser.Rosr_retainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_retain}.
	 * @param ctx the parse tree
	 */
	void exitRosr_retain(FlatJuniperParser.Rosr_retainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rosr_tag}.
	 * @param ctx the parse tree
	 */
	void enterRosr_tag(FlatJuniperParser.Rosr_tagContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rosr_tag}.
	 * @param ctx the parse tree
	 */
	void exitRosr_tag(FlatJuniperParser.Rosr_tagContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_routing_instances}.
	 * @param ctx the parse tree
	 */
	void enterS_routing_instances(FlatJuniperParser.S_routing_instancesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_routing_instances}.
	 * @param ctx the parse tree
	 */
	void exitS_routing_instances(FlatJuniperParser.S_routing_instancesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_routing_options}.
	 * @param ctx the parse tree
	 */
	void enterS_routing_options(FlatJuniperParser.S_routing_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_routing_options}.
	 * @param ctx the parse tree
	 */
	void exitS_routing_options(FlatJuniperParser.S_routing_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_snmp}.
	 * @param ctx the parse tree
	 */
	void enterS_snmp(FlatJuniperParser.S_snmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_snmp}.
	 * @param ctx the parse tree
	 */
	void exitS_snmp(FlatJuniperParser.S_snmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmp_community}.
	 * @param ctx the parse tree
	 */
	void enterSnmp_community(FlatJuniperParser.Snmp_communityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmp_community}.
	 * @param ctx the parse tree
	 */
	void exitSnmp_community(FlatJuniperParser.Snmp_communityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmp_filter_interfaces}.
	 * @param ctx the parse tree
	 */
	void enterSnmp_filter_interfaces(FlatJuniperParser.Snmp_filter_interfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmp_filter_interfaces}.
	 * @param ctx the parse tree
	 */
	void exitSnmp_filter_interfaces(FlatJuniperParser.Snmp_filter_interfacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmp_name}.
	 * @param ctx the parse tree
	 */
	void enterSnmp_name(FlatJuniperParser.Snmp_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmp_name}.
	 * @param ctx the parse tree
	 */
	void exitSnmp_name(FlatJuniperParser.Snmp_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmp_null}.
	 * @param ctx the parse tree
	 */
	void enterSnmp_null(FlatJuniperParser.Snmp_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmp_null}.
	 * @param ctx the parse tree
	 */
	void exitSnmp_null(FlatJuniperParser.Snmp_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmp_trap_group}.
	 * @param ctx the parse tree
	 */
	void enterSnmp_trap_group(FlatJuniperParser.Snmp_trap_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmp_trap_group}.
	 * @param ctx the parse tree
	 */
	void exitSnmp_trap_group(FlatJuniperParser.Snmp_trap_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmpc_authorization}.
	 * @param ctx the parse tree
	 */
	void enterSnmpc_authorization(FlatJuniperParser.Snmpc_authorizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmpc_authorization}.
	 * @param ctx the parse tree
	 */
	void exitSnmpc_authorization(FlatJuniperParser.Snmpc_authorizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmpc_client_list_name}.
	 * @param ctx the parse tree
	 */
	void enterSnmpc_client_list_name(FlatJuniperParser.Snmpc_client_list_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmpc_client_list_name}.
	 * @param ctx the parse tree
	 */
	void exitSnmpc_client_list_name(FlatJuniperParser.Snmpc_client_list_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmpc_null}.
	 * @param ctx the parse tree
	 */
	void enterSnmpc_null(FlatJuniperParser.Snmpc_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmpc_null}.
	 * @param ctx the parse tree
	 */
	void exitSnmpc_null(FlatJuniperParser.Snmpc_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmptg_null}.
	 * @param ctx the parse tree
	 */
	void enterSnmptg_null(FlatJuniperParser.Snmptg_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmptg_null}.
	 * @param ctx the parse tree
	 */
	void exitSnmptg_null(FlatJuniperParser.Snmptg_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#snmptg_targets}.
	 * @param ctx the parse tree
	 */
	void enterSnmptg_targets(FlatJuniperParser.Snmptg_targetsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#snmptg_targets}.
	 * @param ctx the parse tree
	 */
	void exitSnmptg_targets(FlatJuniperParser.Snmptg_targetsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#address_specifier}.
	 * @param ctx the parse tree
	 */
	void enterAddress_specifier(FlatJuniperParser.Address_specifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#address_specifier}.
	 * @param ctx the parse tree
	 */
	void exitAddress_specifier(FlatJuniperParser.Address_specifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#dh_group}.
	 * @param ctx the parse tree
	 */
	void enterDh_group(FlatJuniperParser.Dh_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#dh_group}.
	 * @param ctx the parse tree
	 */
	void exitDh_group(FlatJuniperParser.Dh_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterEncryption_algorithm(FlatJuniperParser.Encryption_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitEncryption_algorithm(FlatJuniperParser.Encryption_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#hib_protocol}.
	 * @param ctx the parse tree
	 */
	void enterHib_protocol(FlatJuniperParser.Hib_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#hib_protocol}.
	 * @param ctx the parse tree
	 */
	void exitHib_protocol(FlatJuniperParser.Hib_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#hib_system_service}.
	 * @param ctx the parse tree
	 */
	void enterHib_system_service(FlatJuniperParser.Hib_system_serviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#hib_system_service}.
	 * @param ctx the parse tree
	 */
	void exitHib_system_service(FlatJuniperParser.Hib_system_serviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ike_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterIke_authentication_algorithm(FlatJuniperParser.Ike_authentication_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ike_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitIke_authentication_algorithm(FlatJuniperParser.Ike_authentication_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ike_authentication_method}.
	 * @param ctx the parse tree
	 */
	void enterIke_authentication_method(FlatJuniperParser.Ike_authentication_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ike_authentication_method}.
	 * @param ctx the parse tree
	 */
	void exitIke_authentication_method(FlatJuniperParser.Ike_authentication_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ipsec_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterIpsec_authentication_algorithm(FlatJuniperParser.Ipsec_authentication_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ipsec_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitIpsec_authentication_algorithm(FlatJuniperParser.Ipsec_authentication_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ipsec_protocol}.
	 * @param ctx the parse tree
	 */
	void enterIpsec_protocol(FlatJuniperParser.Ipsec_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ipsec_protocol}.
	 * @param ctx the parse tree
	 */
	void exitIpsec_protocol(FlatJuniperParser.Ipsec_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_interface}.
	 * @param ctx the parse tree
	 */
	void enterNat_interface(FlatJuniperParser.Nat_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_interface}.
	 * @param ctx the parse tree
	 */
	void exitNat_interface(FlatJuniperParser.Nat_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_pool}.
	 * @param ctx the parse tree
	 */
	void enterNat_pool(FlatJuniperParser.Nat_poolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_pool}.
	 * @param ctx the parse tree
	 */
	void exitNat_pool(FlatJuniperParser.Nat_poolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_pool_utilization_alarm}.
	 * @param ctx the parse tree
	 */
	void enterNat_pool_utilization_alarm(FlatJuniperParser.Nat_pool_utilization_alarmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_pool_utilization_alarm}.
	 * @param ctx the parse tree
	 */
	void exitNat_pool_utilization_alarm(FlatJuniperParser.Nat_pool_utilization_alarmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_pool_default_port_range}.
	 * @param ctx the parse tree
	 */
	void enterNat_pool_default_port_range(FlatJuniperParser.Nat_pool_default_port_rangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_pool_default_port_range}.
	 * @param ctx the parse tree
	 */
	void exitNat_pool_default_port_range(FlatJuniperParser.Nat_pool_default_port_rangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_port_randomization}.
	 * @param ctx the parse tree
	 */
	void enterNat_port_randomization(FlatJuniperParser.Nat_port_randomizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_port_randomization}.
	 * @param ctx the parse tree
	 */
	void exitNat_port_randomization(FlatJuniperParser.Nat_port_randomizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nat_rule_set}.
	 * @param ctx the parse tree
	 */
	void enterNat_rule_set(FlatJuniperParser.Nat_rule_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nat_rule_set}.
	 * @param ctx the parse tree
	 */
	void exitNat_rule_set(FlatJuniperParser.Nat_rule_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nati_port_overloading}.
	 * @param ctx the parse tree
	 */
	void enterNati_port_overloading(FlatJuniperParser.Nati_port_overloadingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nati_port_overloading}.
	 * @param ctx the parse tree
	 */
	void exitNati_port_overloading(FlatJuniperParser.Nati_port_overloadingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#nati_port_overloading_factor}.
	 * @param ctx the parse tree
	 */
	void enterNati_port_overloading_factor(FlatJuniperParser.Nati_port_overloading_factorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#nati_port_overloading_factor}.
	 * @param ctx the parse tree
	 */
	void exitNati_port_overloading_factor(FlatJuniperParser.Nati_port_overloading_factorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#natp_address}.
	 * @param ctx the parse tree
	 */
	void enterNatp_address(FlatJuniperParser.Natp_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#natp_address}.
	 * @param ctx the parse tree
	 */
	void exitNatp_address(FlatJuniperParser.Natp_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#natp_port}.
	 * @param ctx the parse tree
	 */
	void enterNatp_port(FlatJuniperParser.Natp_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#natp_port}.
	 * @param ctx the parse tree
	 */
	void exitNatp_port(FlatJuniperParser.Natp_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#natp_description}.
	 * @param ctx the parse tree
	 */
	void enterNatp_description(FlatJuniperParser.Natp_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#natp_description}.
	 * @param ctx the parse tree
	 */
	void exitNatp_description(FlatJuniperParser.Natp_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#natp_routing_instance}.
	 * @param ctx the parse tree
	 */
	void enterNatp_routing_instance(FlatJuniperParser.Natp_routing_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#natp_routing_instance}.
	 * @param ctx the parse tree
	 */
	void exitNatp_routing_instance(FlatJuniperParser.Natp_routing_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#proposal_set_type}.
	 * @param ctx the parse tree
	 */
	void enterProposal_set_type(FlatJuniperParser.Proposal_set_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#proposal_set_type}.
	 * @param ctx the parse tree
	 */
	void exitProposal_set_type(FlatJuniperParser.Proposal_set_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rs_interface}.
	 * @param ctx the parse tree
	 */
	void enterRs_interface(FlatJuniperParser.Rs_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rs_interface}.
	 * @param ctx the parse tree
	 */
	void exitRs_interface(FlatJuniperParser.Rs_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rs_packet_location}.
	 * @param ctx the parse tree
	 */
	void enterRs_packet_location(FlatJuniperParser.Rs_packet_locationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rs_packet_location}.
	 * @param ctx the parse tree
	 */
	void exitRs_packet_location(FlatJuniperParser.Rs_packet_locationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rs_routing_instance}.
	 * @param ctx the parse tree
	 */
	void enterRs_routing_instance(FlatJuniperParser.Rs_routing_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rs_routing_instance}.
	 * @param ctx the parse tree
	 */
	void exitRs_routing_instance(FlatJuniperParser.Rs_routing_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rs_rule}.
	 * @param ctx the parse tree
	 */
	void enterRs_rule(FlatJuniperParser.Rs_ruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rs_rule}.
	 * @param ctx the parse tree
	 */
	void exitRs_rule(FlatJuniperParser.Rs_ruleContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rs_zone}.
	 * @param ctx the parse tree
	 */
	void enterRs_zone(FlatJuniperParser.Rs_zoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rs_zone}.
	 * @param ctx the parse tree
	 */
	void exitRs_zone(FlatJuniperParser.Rs_zoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsr_description}.
	 * @param ctx the parse tree
	 */
	void enterRsr_description(FlatJuniperParser.Rsr_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsr_description}.
	 * @param ctx the parse tree
	 */
	void exitRsr_description(FlatJuniperParser.Rsr_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsr_match}.
	 * @param ctx the parse tree
	 */
	void enterRsr_match(FlatJuniperParser.Rsr_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsr_match}.
	 * @param ctx the parse tree
	 */
	void exitRsr_match(FlatJuniperParser.Rsr_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsr_then}.
	 * @param ctx the parse tree
	 */
	void enterRsr_then(FlatJuniperParser.Rsr_thenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsr_then}.
	 * @param ctx the parse tree
	 */
	void exitRsr_then(FlatJuniperParser.Rsr_thenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_destination_address}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_destination_address(FlatJuniperParser.Rsrm_destination_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_destination_address}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_destination_address(FlatJuniperParser.Rsrm_destination_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_destination_address_name}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_destination_address_name(FlatJuniperParser.Rsrm_destination_address_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_destination_address_name}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_destination_address_name(FlatJuniperParser.Rsrm_destination_address_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_destination_port}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_destination_port(FlatJuniperParser.Rsrm_destination_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_destination_port}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_destination_port(FlatJuniperParser.Rsrm_destination_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_source_address}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_source_address(FlatJuniperParser.Rsrm_source_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_source_address}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_source_address(FlatJuniperParser.Rsrm_source_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_source_address_name}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_source_address_name(FlatJuniperParser.Rsrm_source_address_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_source_address_name}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_source_address_name(FlatJuniperParser.Rsrm_source_address_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrm_source_port}.
	 * @param ctx the parse tree
	 */
	void enterRsrm_source_port(FlatJuniperParser.Rsrm_source_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrm_source_port}.
	 * @param ctx the parse tree
	 */
	void exitRsrm_source_port(FlatJuniperParser.Rsrm_source_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_destination_nat}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_destination_nat(FlatJuniperParser.Rsrt_destination_natContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_destination_nat}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_destination_nat(FlatJuniperParser.Rsrt_destination_natContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_nat_interface}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_nat_interface(FlatJuniperParser.Rsrt_nat_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_nat_interface}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_nat_interface(FlatJuniperParser.Rsrt_nat_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_nat_off}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_nat_off(FlatJuniperParser.Rsrt_nat_offContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_nat_off}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_nat_off(FlatJuniperParser.Rsrt_nat_offContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_nat_pool}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_nat_pool(FlatJuniperParser.Rsrt_nat_poolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_nat_pool}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_nat_pool(FlatJuniperParser.Rsrt_nat_poolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_source_nat}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_source_nat(FlatJuniperParser.Rsrt_source_natContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_source_nat}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_source_nat(FlatJuniperParser.Rsrt_source_natContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrt_static_nat}.
	 * @param ctx the parse tree
	 */
	void enterRsrt_static_nat(FlatJuniperParser.Rsrt_static_natContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrt_static_nat}.
	 * @param ctx the parse tree
	 */
	void exitRsrt_static_nat(FlatJuniperParser.Rsrt_static_natContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtnp_persistent_nat}.
	 * @param ctx the parse tree
	 */
	void enterRsrtnp_persistent_nat(FlatJuniperParser.Rsrtnp_persistent_natContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtnp_persistent_nat}.
	 * @param ctx the parse tree
	 */
	void exitRsrtnp_persistent_nat(FlatJuniperParser.Rsrtnp_persistent_natContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtnpp_inactivity_timeout}.
	 * @param ctx the parse tree
	 */
	void enterRsrtnpp_inactivity_timeout(FlatJuniperParser.Rsrtnpp_inactivity_timeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtnpp_inactivity_timeout}.
	 * @param ctx the parse tree
	 */
	void exitRsrtnpp_inactivity_timeout(FlatJuniperParser.Rsrtnpp_inactivity_timeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtnpp_max_session_number}.
	 * @param ctx the parse tree
	 */
	void enterRsrtnpp_max_session_number(FlatJuniperParser.Rsrtnpp_max_session_numberContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtnpp_max_session_number}.
	 * @param ctx the parse tree
	 */
	void exitRsrtnpp_max_session_number(FlatJuniperParser.Rsrtnpp_max_session_numberContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtnpp_permit}.
	 * @param ctx the parse tree
	 */
	void enterRsrtnpp_permit(FlatJuniperParser.Rsrtnpp_permitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtnpp_permit}.
	 * @param ctx the parse tree
	 */
	void exitRsrtnpp_permit(FlatJuniperParser.Rsrtnpp_permitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtst_prefix}.
	 * @param ctx the parse tree
	 */
	void enterRsrtst_prefix(FlatJuniperParser.Rsrtst_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtst_prefix}.
	 * @param ctx the parse tree
	 */
	void exitRsrtst_prefix(FlatJuniperParser.Rsrtst_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtst_prefix_name}.
	 * @param ctx the parse tree
	 */
	void enterRsrtst_prefix_name(FlatJuniperParser.Rsrtst_prefix_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtst_prefix_name}.
	 * @param ctx the parse tree
	 */
	void exitRsrtst_prefix_name(FlatJuniperParser.Rsrtst_prefix_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtstp_mapped_port}.
	 * @param ctx the parse tree
	 */
	void enterRsrtstp_mapped_port(FlatJuniperParser.Rsrtstp_mapped_portContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtstp_mapped_port}.
	 * @param ctx the parse tree
	 */
	void exitRsrtstp_mapped_port(FlatJuniperParser.Rsrtstp_mapped_portContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtstp_prefix}.
	 * @param ctx the parse tree
	 */
	void enterRsrtstp_prefix(FlatJuniperParser.Rsrtstp_prefixContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtstp_prefix}.
	 * @param ctx the parse tree
	 */
	void exitRsrtstp_prefix(FlatJuniperParser.Rsrtstp_prefixContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtstp_prefix_name}.
	 * @param ctx the parse tree
	 */
	void enterRsrtstp_prefix_name(FlatJuniperParser.Rsrtstp_prefix_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtstp_prefix_name}.
	 * @param ctx the parse tree
	 */
	void exitRsrtstp_prefix_name(FlatJuniperParser.Rsrtstp_prefix_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#rsrtstp_routing_instance}.
	 * @param ctx the parse tree
	 */
	void enterRsrtstp_routing_instance(FlatJuniperParser.Rsrtstp_routing_instanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#rsrtstp_routing_instance}.
	 * @param ctx the parse tree
	 */
	void exitRsrtstp_routing_instance(FlatJuniperParser.Rsrtstp_routing_instanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_security}.
	 * @param ctx the parse tree
	 */
	void enterS_security(FlatJuniperParser.S_securityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_security}.
	 * @param ctx the parse tree
	 */
	void exitS_security(FlatJuniperParser.S_securityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_address_book}.
	 * @param ctx the parse tree
	 */
	void enterSe_address_book(FlatJuniperParser.Se_address_bookContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_address_book}.
	 * @param ctx the parse tree
	 */
	void exitSe_address_book(FlatJuniperParser.Se_address_bookContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_authentication_key_chain}.
	 * @param ctx the parse tree
	 */
	void enterSe_authentication_key_chain(FlatJuniperParser.Se_authentication_key_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_authentication_key_chain}.
	 * @param ctx the parse tree
	 */
	void exitSe_authentication_key_chain(FlatJuniperParser.Se_authentication_key_chainContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_certificates}.
	 * @param ctx the parse tree
	 */
	void enterSe_certificates(FlatJuniperParser.Se_certificatesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_certificates}.
	 * @param ctx the parse tree
	 */
	void exitSe_certificates(FlatJuniperParser.Se_certificatesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_ike}.
	 * @param ctx the parse tree
	 */
	void enterSe_ike(FlatJuniperParser.Se_ikeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_ike}.
	 * @param ctx the parse tree
	 */
	void exitSe_ike(FlatJuniperParser.Se_ikeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_ipsec}.
	 * @param ctx the parse tree
	 */
	void enterSe_ipsec(FlatJuniperParser.Se_ipsecContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_ipsec}.
	 * @param ctx the parse tree
	 */
	void exitSe_ipsec(FlatJuniperParser.Se_ipsecContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_nat}.
	 * @param ctx the parse tree
	 */
	void enterSe_nat(FlatJuniperParser.Se_natContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_nat}.
	 * @param ctx the parse tree
	 */
	void exitSe_nat(FlatJuniperParser.Se_natContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_null}.
	 * @param ctx the parse tree
	 */
	void enterSe_null(FlatJuniperParser.Se_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_null}.
	 * @param ctx the parse tree
	 */
	void exitSe_null(FlatJuniperParser.Se_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_policies}.
	 * @param ctx the parse tree
	 */
	void enterSe_policies(FlatJuniperParser.Se_policiesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_policies}.
	 * @param ctx the parse tree
	 */
	void exitSe_policies(FlatJuniperParser.Se_policiesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_screen}.
	 * @param ctx the parse tree
	 */
	void enterSe_screen(FlatJuniperParser.Se_screenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_screen}.
	 * @param ctx the parse tree
	 */
	void exitSe_screen(FlatJuniperParser.Se_screenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#se_zones}.
	 * @param ctx the parse tree
	 */
	void enterSe_zones(FlatJuniperParser.Se_zonesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#se_zones}.
	 * @param ctx the parse tree
	 */
	void exitSe_zones(FlatJuniperParser.Se_zonesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sea_description}.
	 * @param ctx the parse tree
	 */
	void enterSea_description(FlatJuniperParser.Sea_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sea_description}.
	 * @param ctx the parse tree
	 */
	void exitSea_description(FlatJuniperParser.Sea_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sea_key}.
	 * @param ctx the parse tree
	 */
	void enterSea_key(FlatJuniperParser.Sea_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sea_key}.
	 * @param ctx the parse tree
	 */
	void exitSea_key(FlatJuniperParser.Sea_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sea_tolerance}.
	 * @param ctx the parse tree
	 */
	void enterSea_tolerance(FlatJuniperParser.Sea_toleranceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sea_tolerance}.
	 * @param ctx the parse tree
	 */
	void exitSea_tolerance(FlatJuniperParser.Sea_toleranceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sead_address}.
	 * @param ctx the parse tree
	 */
	void enterSead_address(FlatJuniperParser.Sead_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sead_address}.
	 * @param ctx the parse tree
	 */
	void exitSead_address(FlatJuniperParser.Sead_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sead_address_set}.
	 * @param ctx the parse tree
	 */
	void enterSead_address_set(FlatJuniperParser.Sead_address_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sead_address_set}.
	 * @param ctx the parse tree
	 */
	void exitSead_address_set(FlatJuniperParser.Sead_address_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sead_attach}.
	 * @param ctx the parse tree
	 */
	void enterSead_attach(FlatJuniperParser.Sead_attachContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sead_attach}.
	 * @param ctx the parse tree
	 */
	void exitSead_attach(FlatJuniperParser.Sead_attachContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seada_address}.
	 * @param ctx the parse tree
	 */
	void enterSeada_address(FlatJuniperParser.Seada_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seada_address}.
	 * @param ctx the parse tree
	 */
	void exitSeada_address(FlatJuniperParser.Seada_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seada_address_set}.
	 * @param ctx the parse tree
	 */
	void enterSeada_address_set(FlatJuniperParser.Seada_address_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seada_address_set}.
	 * @param ctx the parse tree
	 */
	void exitSeada_address_set(FlatJuniperParser.Seada_address_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seada_description}.
	 * @param ctx the parse tree
	 */
	void enterSeada_description(FlatJuniperParser.Seada_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seada_description}.
	 * @param ctx the parse tree
	 */
	void exitSeada_description(FlatJuniperParser.Seada_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sec_local}.
	 * @param ctx the parse tree
	 */
	void enterSec_local(FlatJuniperParser.Sec_localContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sec_local}.
	 * @param ctx the parse tree
	 */
	void exitSec_local(FlatJuniperParser.Sec_localContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seak_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterSeak_algorithm(FlatJuniperParser.Seak_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seak_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitSeak_algorithm(FlatJuniperParser.Seak_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seak_options}.
	 * @param ctx the parse tree
	 */
	void enterSeak_options(FlatJuniperParser.Seak_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seak_options}.
	 * @param ctx the parse tree
	 */
	void exitSeak_options(FlatJuniperParser.Seak_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seak_secret}.
	 * @param ctx the parse tree
	 */
	void enterSeak_secret(FlatJuniperParser.Seak_secretContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seak_secret}.
	 * @param ctx the parse tree
	 */
	void exitSeak_secret(FlatJuniperParser.Seak_secretContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seak_start_time}.
	 * @param ctx the parse tree
	 */
	void enterSeak_start_time(FlatJuniperParser.Seak_start_timeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seak_start_time}.
	 * @param ctx the parse tree
	 */
	void exitSeak_start_time(FlatJuniperParser.Seak_start_timeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seik_gateway}.
	 * @param ctx the parse tree
	 */
	void enterSeik_gateway(FlatJuniperParser.Seik_gatewayContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seik_gateway}.
	 * @param ctx the parse tree
	 */
	void exitSeik_gateway(FlatJuniperParser.Seik_gatewayContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seik_policy}.
	 * @param ctx the parse tree
	 */
	void enterSeik_policy(FlatJuniperParser.Seik_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seik_policy}.
	 * @param ctx the parse tree
	 */
	void exitSeik_policy(FlatJuniperParser.Seik_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seik_proposal}.
	 * @param ctx the parse tree
	 */
	void enterSeik_proposal(FlatJuniperParser.Seik_proposalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seik_proposal}.
	 * @param ctx the parse tree
	 */
	void exitSeik_proposal(FlatJuniperParser.Seik_proposalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_address}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_address(FlatJuniperParser.Seikg_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_address}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_address(FlatJuniperParser.Seikg_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_dead_peer_detection}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_dead_peer_detection(FlatJuniperParser.Seikg_dead_peer_detectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_dead_peer_detection}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_dead_peer_detection(FlatJuniperParser.Seikg_dead_peer_detectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_dynamic}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_dynamic(FlatJuniperParser.Seikg_dynamicContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_dynamic}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_dynamic(FlatJuniperParser.Seikg_dynamicContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_external_interface}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_external_interface(FlatJuniperParser.Seikg_external_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_external_interface}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_external_interface(FlatJuniperParser.Seikg_external_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_ike_policy}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_ike_policy(FlatJuniperParser.Seikg_ike_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_ike_policy}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_ike_policy(FlatJuniperParser.Seikg_ike_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_local_address}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_local_address(FlatJuniperParser.Seikg_local_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_local_address}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_local_address(FlatJuniperParser.Seikg_local_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_local_identity}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_local_identity(FlatJuniperParser.Seikg_local_identityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_local_identity}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_local_identity(FlatJuniperParser.Seikg_local_identityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_no_nat_traversal}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_no_nat_traversal(FlatJuniperParser.Seikg_no_nat_traversalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_no_nat_traversal}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_no_nat_traversal(FlatJuniperParser.Seikg_no_nat_traversalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_version}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_version(FlatJuniperParser.Seikg_versionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_version}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_version(FlatJuniperParser.Seikg_versionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikg_xauth}.
	 * @param ctx the parse tree
	 */
	void enterSeikg_xauth(FlatJuniperParser.Seikg_xauthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikg_xauth}.
	 * @param ctx the parse tree
	 */
	void exitSeikg_xauth(FlatJuniperParser.Seikg_xauthContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikgd_connections_limit}.
	 * @param ctx the parse tree
	 */
	void enterSeikgd_connections_limit(FlatJuniperParser.Seikgd_connections_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikgd_connections_limit}.
	 * @param ctx the parse tree
	 */
	void exitSeikgd_connections_limit(FlatJuniperParser.Seikgd_connections_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikgd_hostname}.
	 * @param ctx the parse tree
	 */
	void enterSeikgd_hostname(FlatJuniperParser.Seikgd_hostnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikgd_hostname}.
	 * @param ctx the parse tree
	 */
	void exitSeikgd_hostname(FlatJuniperParser.Seikgd_hostnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikgd_ike_user_type}.
	 * @param ctx the parse tree
	 */
	void enterSeikgd_ike_user_type(FlatJuniperParser.Seikgd_ike_user_typeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikgd_ike_user_type}.
	 * @param ctx the parse tree
	 */
	void exitSeikgd_ike_user_type(FlatJuniperParser.Seikgd_ike_user_typeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikgl_inet}.
	 * @param ctx the parse tree
	 */
	void enterSeikgl_inet(FlatJuniperParser.Seikgl_inetContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikgl_inet}.
	 * @param ctx the parse tree
	 */
	void exitSeikgl_inet(FlatJuniperParser.Seikgl_inetContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikp_description}.
	 * @param ctx the parse tree
	 */
	void enterSeikp_description(FlatJuniperParser.Seikp_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikp_description}.
	 * @param ctx the parse tree
	 */
	void exitSeikp_description(FlatJuniperParser.Seikp_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikp_mode}.
	 * @param ctx the parse tree
	 */
	void enterSeikp_mode(FlatJuniperParser.Seikp_modeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikp_mode}.
	 * @param ctx the parse tree
	 */
	void exitSeikp_mode(FlatJuniperParser.Seikp_modeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikp_pre_shared_key}.
	 * @param ctx the parse tree
	 */
	void enterSeikp_pre_shared_key(FlatJuniperParser.Seikp_pre_shared_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikp_pre_shared_key}.
	 * @param ctx the parse tree
	 */
	void exitSeikp_pre_shared_key(FlatJuniperParser.Seikp_pre_shared_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikp_proposal_set}.
	 * @param ctx the parse tree
	 */
	void enterSeikp_proposal_set(FlatJuniperParser.Seikp_proposal_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikp_proposal_set}.
	 * @param ctx the parse tree
	 */
	void exitSeikp_proposal_set(FlatJuniperParser.Seikp_proposal_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikp_proposals}.
	 * @param ctx the parse tree
	 */
	void enterSeikp_proposals(FlatJuniperParser.Seikp_proposalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikp_proposals}.
	 * @param ctx the parse tree
	 */
	void exitSeikp_proposals(FlatJuniperParser.Seikp_proposalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_authentication_algorithm(FlatJuniperParser.Seikpr_authentication_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_authentication_algorithm(FlatJuniperParser.Seikpr_authentication_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_authentication_method}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_authentication_method(FlatJuniperParser.Seikpr_authentication_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_authentication_method}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_authentication_method(FlatJuniperParser.Seikpr_authentication_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_description}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_description(FlatJuniperParser.Seikpr_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_description}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_description(FlatJuniperParser.Seikpr_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_dh_group}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_dh_group(FlatJuniperParser.Seikpr_dh_groupContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_dh_group}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_dh_group(FlatJuniperParser.Seikpr_dh_groupContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_encryption_algorithm(FlatJuniperParser.Seikpr_encryption_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_encryption_algorithm(FlatJuniperParser.Seikpr_encryption_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seikpr_lifetime_seconds}.
	 * @param ctx the parse tree
	 */
	void enterSeikpr_lifetime_seconds(FlatJuniperParser.Seikpr_lifetime_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seikpr_lifetime_seconds}.
	 * @param ctx the parse tree
	 */
	void exitSeikpr_lifetime_seconds(FlatJuniperParser.Seikpr_lifetime_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seip_policy}.
	 * @param ctx the parse tree
	 */
	void enterSeip_policy(FlatJuniperParser.Seip_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seip_policy}.
	 * @param ctx the parse tree
	 */
	void exitSeip_policy(FlatJuniperParser.Seip_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seip_proposal}.
	 * @param ctx the parse tree
	 */
	void enterSeip_proposal(FlatJuniperParser.Seip_proposalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seip_proposal}.
	 * @param ctx the parse tree
	 */
	void exitSeip_proposal(FlatJuniperParser.Seip_proposalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seip_vpn}.
	 * @param ctx the parse tree
	 */
	void enterSeip_vpn(FlatJuniperParser.Seip_vpnContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seip_vpn}.
	 * @param ctx the parse tree
	 */
	void exitSeip_vpn(FlatJuniperParser.Seip_vpnContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipp_perfect_forward_secrecy}.
	 * @param ctx the parse tree
	 */
	void enterSeipp_perfect_forward_secrecy(FlatJuniperParser.Seipp_perfect_forward_secrecyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipp_perfect_forward_secrecy}.
	 * @param ctx the parse tree
	 */
	void exitSeipp_perfect_forward_secrecy(FlatJuniperParser.Seipp_perfect_forward_secrecyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipp_proposal_set}.
	 * @param ctx the parse tree
	 */
	void enterSeipp_proposal_set(FlatJuniperParser.Seipp_proposal_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipp_proposal_set}.
	 * @param ctx the parse tree
	 */
	void exitSeipp_proposal_set(FlatJuniperParser.Seipp_proposal_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipp_proposals}.
	 * @param ctx the parse tree
	 */
	void enterSeipp_proposals(FlatJuniperParser.Seipp_proposalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipp_proposals}.
	 * @param ctx the parse tree
	 */
	void exitSeipp_proposals(FlatJuniperParser.Seipp_proposalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_authentication_algorithm(FlatJuniperParser.Seippr_authentication_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_authentication_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_authentication_algorithm(FlatJuniperParser.Seippr_authentication_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_description}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_description(FlatJuniperParser.Seippr_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_description}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_description(FlatJuniperParser.Seippr_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_encryption_algorithm(FlatJuniperParser.Seippr_encryption_algorithmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_encryption_algorithm}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_encryption_algorithm(FlatJuniperParser.Seippr_encryption_algorithmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_lifetime_kilobytes}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_lifetime_kilobytes(FlatJuniperParser.Seippr_lifetime_kilobytesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_lifetime_kilobytes}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_lifetime_kilobytes(FlatJuniperParser.Seippr_lifetime_kilobytesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_lifetime_seconds}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_lifetime_seconds(FlatJuniperParser.Seippr_lifetime_secondsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_lifetime_seconds}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_lifetime_seconds(FlatJuniperParser.Seippr_lifetime_secondsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seippr_protocol}.
	 * @param ctx the parse tree
	 */
	void enterSeippr_protocol(FlatJuniperParser.Seippr_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seippr_protocol}.
	 * @param ctx the parse tree
	 */
	void exitSeippr_protocol(FlatJuniperParser.Seippr_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipv_bind_interface}.
	 * @param ctx the parse tree
	 */
	void enterSeipv_bind_interface(FlatJuniperParser.Seipv_bind_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipv_bind_interface}.
	 * @param ctx the parse tree
	 */
	void exitSeipv_bind_interface(FlatJuniperParser.Seipv_bind_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipv_df_bit}.
	 * @param ctx the parse tree
	 */
	void enterSeipv_df_bit(FlatJuniperParser.Seipv_df_bitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipv_df_bit}.
	 * @param ctx the parse tree
	 */
	void exitSeipv_df_bit(FlatJuniperParser.Seipv_df_bitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipv_establish_tunnels}.
	 * @param ctx the parse tree
	 */
	void enterSeipv_establish_tunnels(FlatJuniperParser.Seipv_establish_tunnelsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipv_establish_tunnels}.
	 * @param ctx the parse tree
	 */
	void exitSeipv_establish_tunnels(FlatJuniperParser.Seipv_establish_tunnelsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipv_ike}.
	 * @param ctx the parse tree
	 */
	void enterSeipv_ike(FlatJuniperParser.Seipv_ikeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipv_ike}.
	 * @param ctx the parse tree
	 */
	void exitSeipv_ike(FlatJuniperParser.Seipv_ikeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipv_vpn_monitor}.
	 * @param ctx the parse tree
	 */
	void enterSeipv_vpn_monitor(FlatJuniperParser.Seipv_vpn_monitorContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipv_vpn_monitor}.
	 * @param ctx the parse tree
	 */
	void exitSeipv_vpn_monitor(FlatJuniperParser.Seipv_vpn_monitorContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvi_gateway}.
	 * @param ctx the parse tree
	 */
	void enterSeipvi_gateway(FlatJuniperParser.Seipvi_gatewayContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvi_gateway}.
	 * @param ctx the parse tree
	 */
	void exitSeipvi_gateway(FlatJuniperParser.Seipvi_gatewayContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvi_ipsec_policy}.
	 * @param ctx the parse tree
	 */
	void enterSeipvi_ipsec_policy(FlatJuniperParser.Seipvi_ipsec_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvi_ipsec_policy}.
	 * @param ctx the parse tree
	 */
	void exitSeipvi_ipsec_policy(FlatJuniperParser.Seipvi_ipsec_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvi_null}.
	 * @param ctx the parse tree
	 */
	void enterSeipvi_null(FlatJuniperParser.Seipvi_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvi_null}.
	 * @param ctx the parse tree
	 */
	void exitSeipvi_null(FlatJuniperParser.Seipvi_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvi_proxy_identity}.
	 * @param ctx the parse tree
	 */
	void enterSeipvi_proxy_identity(FlatJuniperParser.Seipvi_proxy_identityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvi_proxy_identity}.
	 * @param ctx the parse tree
	 */
	void exitSeipvi_proxy_identity(FlatJuniperParser.Seipvi_proxy_identityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvip_local}.
	 * @param ctx the parse tree
	 */
	void enterSeipvip_local(FlatJuniperParser.Seipvip_localContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvip_local}.
	 * @param ctx the parse tree
	 */
	void exitSeipvip_local(FlatJuniperParser.Seipvip_localContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvip_remote}.
	 * @param ctx the parse tree
	 */
	void enterSeipvip_remote(FlatJuniperParser.Seipvip_remoteContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvip_remote}.
	 * @param ctx the parse tree
	 */
	void exitSeipvip_remote(FlatJuniperParser.Seipvip_remoteContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvip_service}.
	 * @param ctx the parse tree
	 */
	void enterSeipvip_service(FlatJuniperParser.Seipvip_serviceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvip_service}.
	 * @param ctx the parse tree
	 */
	void exitSeipvip_service(FlatJuniperParser.Seipvip_serviceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvv_destination_ip}.
	 * @param ctx the parse tree
	 */
	void enterSeipvv_destination_ip(FlatJuniperParser.Seipvv_destination_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvv_destination_ip}.
	 * @param ctx the parse tree
	 */
	void exitSeipvv_destination_ip(FlatJuniperParser.Seipvv_destination_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seipvv_source_interface}.
	 * @param ctx the parse tree
	 */
	void enterSeipvv_source_interface(FlatJuniperParser.Seipvv_source_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seipvv_source_interface}.
	 * @param ctx the parse tree
	 */
	void exitSeipvv_source_interface(FlatJuniperParser.Seipvv_source_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sen_destination}.
	 * @param ctx the parse tree
	 */
	void enterSen_destination(FlatJuniperParser.Sen_destinationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sen_destination}.
	 * @param ctx the parse tree
	 */
	void exitSen_destination(FlatJuniperParser.Sen_destinationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sen_proxy_arp}.
	 * @param ctx the parse tree
	 */
	void enterSen_proxy_arp(FlatJuniperParser.Sen_proxy_arpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sen_proxy_arp}.
	 * @param ctx the parse tree
	 */
	void exitSen_proxy_arp(FlatJuniperParser.Sen_proxy_arpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sen_source}.
	 * @param ctx the parse tree
	 */
	void enterSen_source(FlatJuniperParser.Sen_sourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sen_source}.
	 * @param ctx the parse tree
	 */
	void exitSen_source(FlatJuniperParser.Sen_sourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sen_static}.
	 * @param ctx the parse tree
	 */
	void enterSen_static(FlatJuniperParser.Sen_staticContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sen_static}.
	 * @param ctx the parse tree
	 */
	void exitSen_static(FlatJuniperParser.Sen_staticContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#senp_interface}.
	 * @param ctx the parse tree
	 */
	void enterSenp_interface(FlatJuniperParser.Senp_interfaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#senp_interface}.
	 * @param ctx the parse tree
	 */
	void exitSenp_interface(FlatJuniperParser.Senp_interfaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#senpi_address}.
	 * @param ctx the parse tree
	 */
	void enterSenpi_address(FlatJuniperParser.Senpi_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#senpi_address}.
	 * @param ctx the parse tree
	 */
	void exitSenpi_address(FlatJuniperParser.Senpi_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sep_default_policy}.
	 * @param ctx the parse tree
	 */
	void enterSep_default_policy(FlatJuniperParser.Sep_default_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sep_default_policy}.
	 * @param ctx the parse tree
	 */
	void exitSep_default_policy(FlatJuniperParser.Sep_default_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sep_from_zone}.
	 * @param ctx the parse tree
	 */
	void enterSep_from_zone(FlatJuniperParser.Sep_from_zoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sep_from_zone}.
	 * @param ctx the parse tree
	 */
	void exitSep_from_zone(FlatJuniperParser.Sep_from_zoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sep_global}.
	 * @param ctx the parse tree
	 */
	void enterSep_global(FlatJuniperParser.Sep_globalContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sep_global}.
	 * @param ctx the parse tree
	 */
	void exitSep_global(FlatJuniperParser.Sep_globalContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctx_policy}.
	 * @param ctx the parse tree
	 */
	void enterSepctx_policy(FlatJuniperParser.Sepctx_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctx_policy}.
	 * @param ctx the parse tree
	 */
	void exitSepctx_policy(FlatJuniperParser.Sepctx_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxp_description}.
	 * @param ctx the parse tree
	 */
	void enterSepctxp_description(FlatJuniperParser.Sepctxp_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxp_description}.
	 * @param ctx the parse tree
	 */
	void exitSepctxp_description(FlatJuniperParser.Sepctxp_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxp_match}.
	 * @param ctx the parse tree
	 */
	void enterSepctxp_match(FlatJuniperParser.Sepctxp_matchContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxp_match}.
	 * @param ctx the parse tree
	 */
	void exitSepctxp_match(FlatJuniperParser.Sepctxp_matchContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxp_then}.
	 * @param ctx the parse tree
	 */
	void enterSepctxp_then(FlatJuniperParser.Sepctxp_thenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxp_then}.
	 * @param ctx the parse tree
	 */
	void exitSepctxp_then(FlatJuniperParser.Sepctxp_thenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_application}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_application(FlatJuniperParser.Sepctxpm_applicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_application}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_application(FlatJuniperParser.Sepctxpm_applicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_destination_address}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_destination_address(FlatJuniperParser.Sepctxpm_destination_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_destination_address}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_destination_address(FlatJuniperParser.Sepctxpm_destination_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_destination_address_excluded}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_destination_address_excluded(FlatJuniperParser.Sepctxpm_destination_address_excludedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_destination_address_excluded}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_destination_address_excluded(FlatJuniperParser.Sepctxpm_destination_address_excludedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_address}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_source_address(FlatJuniperParser.Sepctxpm_source_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_address}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_source_address(FlatJuniperParser.Sepctxpm_source_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_address_excluded}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_source_address_excluded(FlatJuniperParser.Sepctxpm_source_address_excludedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_address_excluded}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_source_address_excluded(FlatJuniperParser.Sepctxpm_source_address_excludedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_identity}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpm_source_identity(FlatJuniperParser.Sepctxpm_source_identityContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpm_source_identity}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpm_source_identity(FlatJuniperParser.Sepctxpm_source_identityContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpt_count}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpt_count(FlatJuniperParser.Sepctxpt_countContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpt_count}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpt_count(FlatJuniperParser.Sepctxpt_countContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpt_deny}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpt_deny(FlatJuniperParser.Sepctxpt_denyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpt_deny}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpt_deny(FlatJuniperParser.Sepctxpt_denyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpt_log}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpt_log(FlatJuniperParser.Sepctxpt_logContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpt_log}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpt_log(FlatJuniperParser.Sepctxpt_logContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpt_permit}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpt_permit(FlatJuniperParser.Sepctxpt_permitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpt_permit}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpt_permit(FlatJuniperParser.Sepctxpt_permitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxpt_trace}.
	 * @param ctx the parse tree
	 */
	void enterSepctxpt_trace(FlatJuniperParser.Sepctxpt_traceContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxpt_trace}.
	 * @param ctx the parse tree
	 */
	void exitSepctxpt_trace(FlatJuniperParser.Sepctxpt_traceContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxptp_tunnel}.
	 * @param ctx the parse tree
	 */
	void enterSepctxptp_tunnel(FlatJuniperParser.Sepctxptp_tunnelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxptp_tunnel}.
	 * @param ctx the parse tree
	 */
	void exitSepctxptp_tunnel(FlatJuniperParser.Sepctxptp_tunnelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sepctxptpt_ipsec_vpn}.
	 * @param ctx the parse tree
	 */
	void enterSepctxptpt_ipsec_vpn(FlatJuniperParser.Sepctxptpt_ipsec_vpnContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sepctxptpt_ipsec_vpn}.
	 * @param ctx the parse tree
	 */
	void exitSepctxptpt_ipsec_vpn(FlatJuniperParser.Sepctxptpt_ipsec_vpnContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ses_ids_option}.
	 * @param ctx the parse tree
	 */
	void enterSes_ids_option(FlatJuniperParser.Ses_ids_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ses_ids_option}.
	 * @param ctx the parse tree
	 */
	void exitSes_ids_option(FlatJuniperParser.Ses_ids_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#ses_null}.
	 * @param ctx the parse tree
	 */
	void enterSes_null(FlatJuniperParser.Ses_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#ses_null}.
	 * @param ctx the parse tree
	 */
	void exitSes_null(FlatJuniperParser.Ses_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_alarm}.
	 * @param ctx the parse tree
	 */
	void enterSeso_alarm(FlatJuniperParser.Seso_alarmContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_alarm}.
	 * @param ctx the parse tree
	 */
	void exitSeso_alarm(FlatJuniperParser.Seso_alarmContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_description}.
	 * @param ctx the parse tree
	 */
	void enterSeso_description(FlatJuniperParser.Seso_descriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_description}.
	 * @param ctx the parse tree
	 */
	void exitSeso_description(FlatJuniperParser.Seso_descriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_icmp}.
	 * @param ctx the parse tree
	 */
	void enterSeso_icmp(FlatJuniperParser.Seso_icmpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_icmp}.
	 * @param ctx the parse tree
	 */
	void exitSeso_icmp(FlatJuniperParser.Seso_icmpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_ip}.
	 * @param ctx the parse tree
	 */
	void enterSeso_ip(FlatJuniperParser.Seso_ipContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_ip}.
	 * @param ctx the parse tree
	 */
	void exitSeso_ip(FlatJuniperParser.Seso_ipContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_limit_session}.
	 * @param ctx the parse tree
	 */
	void enterSeso_limit_session(FlatJuniperParser.Seso_limit_sessionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_limit_session}.
	 * @param ctx the parse tree
	 */
	void exitSeso_limit_session(FlatJuniperParser.Seso_limit_sessionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_tcp}.
	 * @param ctx the parse tree
	 */
	void enterSeso_tcp(FlatJuniperParser.Seso_tcpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_tcp}.
	 * @param ctx the parse tree
	 */
	void exitSeso_tcp(FlatJuniperParser.Seso_tcpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#seso_udp}.
	 * @param ctx the parse tree
	 */
	void enterSeso_udp(FlatJuniperParser.Seso_udpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#seso_udp}.
	 * @param ctx the parse tree
	 */
	void exitSeso_udp(FlatJuniperParser.Seso_udpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_flood}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_flood(FlatJuniperParser.Sesoi_floodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_flood}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_flood(FlatJuniperParser.Sesoi_floodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_fragment}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_fragment(FlatJuniperParser.Sesoi_fragmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_fragment}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_fragment(FlatJuniperParser.Sesoi_fragmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_icmpv6_malformed}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_icmpv6_malformed(FlatJuniperParser.Sesoi_icmpv6_malformedContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_icmpv6_malformed}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_icmpv6_malformed(FlatJuniperParser.Sesoi_icmpv6_malformedContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_ip_sweep}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_ip_sweep(FlatJuniperParser.Sesoi_ip_sweepContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_ip_sweep}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_ip_sweep(FlatJuniperParser.Sesoi_ip_sweepContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_large}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_large(FlatJuniperParser.Sesoi_largeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_large}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_large(FlatJuniperParser.Sesoi_largeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesoi_ping_death}.
	 * @param ctx the parse tree
	 */
	void enterSesoi_ping_death(FlatJuniperParser.Sesoi_ping_deathContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesoi_ping_death}.
	 * @param ctx the parse tree
	 */
	void exitSesoi_ping_death(FlatJuniperParser.Sesoi_ping_deathContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_bad_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_bad_option(FlatJuniperParser.Sesop_bad_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_bad_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_bad_option(FlatJuniperParser.Sesop_bad_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_block_frag}.
	 * @param ctx the parse tree
	 */
	void enterSesop_block_frag(FlatJuniperParser.Sesop_block_fragContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_block_frag}.
	 * @param ctx the parse tree
	 */
	void exitSesop_block_frag(FlatJuniperParser.Sesop_block_fragContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_extension_header}.
	 * @param ctx the parse tree
	 */
	void enterSesop_ipv6_extension_header(FlatJuniperParser.Sesop_ipv6_extension_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_extension_header}.
	 * @param ctx the parse tree
	 */
	void exitSesop_ipv6_extension_header(FlatJuniperParser.Sesop_ipv6_extension_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_extension_header_limit}.
	 * @param ctx the parse tree
	 */
	void enterSesop_ipv6_extension_header_limit(FlatJuniperParser.Sesop_ipv6_extension_header_limitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_extension_header_limit}.
	 * @param ctx the parse tree
	 */
	void exitSesop_ipv6_extension_header_limit(FlatJuniperParser.Sesop_ipv6_extension_header_limitContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_malformed_header}.
	 * @param ctx the parse tree
	 */
	void enterSesop_ipv6_malformed_header(FlatJuniperParser.Sesop_ipv6_malformed_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_ipv6_malformed_header}.
	 * @param ctx the parse tree
	 */
	void exitSesop_ipv6_malformed_header(FlatJuniperParser.Sesop_ipv6_malformed_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_loose_source_route_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_loose_source_route_option(FlatJuniperParser.Sesop_loose_source_route_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_loose_source_route_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_loose_source_route_option(FlatJuniperParser.Sesop_loose_source_route_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_record_route_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_record_route_option(FlatJuniperParser.Sesop_record_route_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_record_route_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_record_route_option(FlatJuniperParser.Sesop_record_route_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_security_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_security_option(FlatJuniperParser.Sesop_security_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_security_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_security_option(FlatJuniperParser.Sesop_security_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_source_route_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_source_route_option(FlatJuniperParser.Sesop_source_route_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_source_route_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_source_route_option(FlatJuniperParser.Sesop_source_route_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_spoofing}.
	 * @param ctx the parse tree
	 */
	void enterSesop_spoofing(FlatJuniperParser.Sesop_spoofingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_spoofing}.
	 * @param ctx the parse tree
	 */
	void exitSesop_spoofing(FlatJuniperParser.Sesop_spoofingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_stream_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_stream_option(FlatJuniperParser.Sesop_stream_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_stream_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_stream_option(FlatJuniperParser.Sesop_stream_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_strict_source_route_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_strict_source_route_option(FlatJuniperParser.Sesop_strict_source_route_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_strict_source_route_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_strict_source_route_option(FlatJuniperParser.Sesop_strict_source_route_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_tear_drop}.
	 * @param ctx the parse tree
	 */
	void enterSesop_tear_drop(FlatJuniperParser.Sesop_tear_dropContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_tear_drop}.
	 * @param ctx the parse tree
	 */
	void exitSesop_tear_drop(FlatJuniperParser.Sesop_tear_dropContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_timestamp_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop_timestamp_option(FlatJuniperParser.Sesop_timestamp_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_timestamp_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop_timestamp_option(FlatJuniperParser.Sesop_timestamp_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_tunnel}.
	 * @param ctx the parse tree
	 */
	void enterSesop_tunnel(FlatJuniperParser.Sesop_tunnelContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_tunnel}.
	 * @param ctx the parse tree
	 */
	void exitSesop_tunnel(FlatJuniperParser.Sesop_tunnelContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop_unknown_protocol}.
	 * @param ctx the parse tree
	 */
	void enterSesop_unknown_protocol(FlatJuniperParser.Sesop_unknown_protocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop_unknown_protocol}.
	 * @param ctx the parse tree
	 */
	void exitSesop_unknown_protocol(FlatJuniperParser.Sesop_unknown_protocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop6_dst_header}.
	 * @param ctx the parse tree
	 */
	void enterSesop6_dst_header(FlatJuniperParser.Sesop6_dst_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop6_dst_header}.
	 * @param ctx the parse tree
	 */
	void exitSesop6_dst_header(FlatJuniperParser.Sesop6_dst_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop6_hop_header}.
	 * @param ctx the parse tree
	 */
	void enterSesop6_hop_header(FlatJuniperParser.Sesop6_hop_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop6_hop_header}.
	 * @param ctx the parse tree
	 */
	void exitSesop6_hop_header(FlatJuniperParser.Sesop6_hop_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesop6_user_option}.
	 * @param ctx the parse tree
	 */
	void enterSesop6_user_option(FlatJuniperParser.Sesop6_user_optionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesop6_user_option}.
	 * @param ctx the parse tree
	 */
	void exitSesop6_user_option(FlatJuniperParser.Sesop6_user_optionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_fin_no_ack}.
	 * @param ctx the parse tree
	 */
	void enterSesot_fin_no_ack(FlatJuniperParser.Sesot_fin_no_ackContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_fin_no_ack}.
	 * @param ctx the parse tree
	 */
	void exitSesot_fin_no_ack(FlatJuniperParser.Sesot_fin_no_ackContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_land}.
	 * @param ctx the parse tree
	 */
	void enterSesot_land(FlatJuniperParser.Sesot_landContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_land}.
	 * @param ctx the parse tree
	 */
	void exitSesot_land(FlatJuniperParser.Sesot_landContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_port_scan}.
	 * @param ctx the parse tree
	 */
	void enterSesot_port_scan(FlatJuniperParser.Sesot_port_scanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_port_scan}.
	 * @param ctx the parse tree
	 */
	void exitSesot_port_scan(FlatJuniperParser.Sesot_port_scanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_syn_ack_ack_proxy}.
	 * @param ctx the parse tree
	 */
	void enterSesot_syn_ack_ack_proxy(FlatJuniperParser.Sesot_syn_ack_ack_proxyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_syn_ack_ack_proxy}.
	 * @param ctx the parse tree
	 */
	void exitSesot_syn_ack_ack_proxy(FlatJuniperParser.Sesot_syn_ack_ack_proxyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_syn_fin}.
	 * @param ctx the parse tree
	 */
	void enterSesot_syn_fin(FlatJuniperParser.Sesot_syn_finContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_syn_fin}.
	 * @param ctx the parse tree
	 */
	void exitSesot_syn_fin(FlatJuniperParser.Sesot_syn_finContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_syn_flood}.
	 * @param ctx the parse tree
	 */
	void enterSesot_syn_flood(FlatJuniperParser.Sesot_syn_floodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_syn_flood}.
	 * @param ctx the parse tree
	 */
	void exitSesot_syn_flood(FlatJuniperParser.Sesot_syn_floodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_syn_frag}.
	 * @param ctx the parse tree
	 */
	void enterSesot_syn_frag(FlatJuniperParser.Sesot_syn_fragContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_syn_frag}.
	 * @param ctx the parse tree
	 */
	void exitSesot_syn_frag(FlatJuniperParser.Sesot_syn_fragContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_tcp_no_flag}.
	 * @param ctx the parse tree
	 */
	void enterSesot_tcp_no_flag(FlatJuniperParser.Sesot_tcp_no_flagContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_tcp_no_flag}.
	 * @param ctx the parse tree
	 */
	void exitSesot_tcp_no_flag(FlatJuniperParser.Sesot_tcp_no_flagContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_tcp_sweep}.
	 * @param ctx the parse tree
	 */
	void enterSesot_tcp_sweep(FlatJuniperParser.Sesot_tcp_sweepContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_tcp_sweep}.
	 * @param ctx the parse tree
	 */
	void exitSesot_tcp_sweep(FlatJuniperParser.Sesot_tcp_sweepContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesot_winnuke}.
	 * @param ctx the parse tree
	 */
	void enterSesot_winnuke(FlatJuniperParser.Sesot_winnukeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesot_winnuke}.
	 * @param ctx the parse tree
	 */
	void exitSesot_winnuke(FlatJuniperParser.Sesot_winnukeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_alarm_thred}.
	 * @param ctx the parse tree
	 */
	void enterSesots_alarm_thred(FlatJuniperParser.Sesots_alarm_thredContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_alarm_thred}.
	 * @param ctx the parse tree
	 */
	void exitSesots_alarm_thred(FlatJuniperParser.Sesots_alarm_thredContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_attack_thred}.
	 * @param ctx the parse tree
	 */
	void enterSesots_attack_thred(FlatJuniperParser.Sesots_attack_thredContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_attack_thred}.
	 * @param ctx the parse tree
	 */
	void exitSesots_attack_thred(FlatJuniperParser.Sesots_attack_thredContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_dst_thred}.
	 * @param ctx the parse tree
	 */
	void enterSesots_dst_thred(FlatJuniperParser.Sesots_dst_thredContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_dst_thred}.
	 * @param ctx the parse tree
	 */
	void exitSesots_dst_thred(FlatJuniperParser.Sesots_dst_thredContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_src_thred}.
	 * @param ctx the parse tree
	 */
	void enterSesots_src_thred(FlatJuniperParser.Sesots_src_thredContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_src_thred}.
	 * @param ctx the parse tree
	 */
	void exitSesots_src_thred(FlatJuniperParser.Sesots_src_thredContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_timeout}.
	 * @param ctx the parse tree
	 */
	void enterSesots_timeout(FlatJuniperParser.Sesots_timeoutContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_timeout}.
	 * @param ctx the parse tree
	 */
	void exitSesots_timeout(FlatJuniperParser.Sesots_timeoutContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesots_whitelist}.
	 * @param ctx the parse tree
	 */
	void enterSesots_whitelist(FlatJuniperParser.Sesots_whitelistContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesots_whitelist}.
	 * @param ctx the parse tree
	 */
	void exitSesots_whitelist(FlatJuniperParser.Sesots_whitelistContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesotsw_dst}.
	 * @param ctx the parse tree
	 */
	void enterSesotsw_dst(FlatJuniperParser.Sesotsw_dstContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesotsw_dst}.
	 * @param ctx the parse tree
	 */
	void exitSesotsw_dst(FlatJuniperParser.Sesotsw_dstContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesotsw_src}.
	 * @param ctx the parse tree
	 */
	void enterSesotsw_src(FlatJuniperParser.Sesotsw_srcContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesotsw_src}.
	 * @param ctx the parse tree
	 */
	void exitSesotsw_src(FlatJuniperParser.Sesotsw_srcContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesou_flood}.
	 * @param ctx the parse tree
	 */
	void enterSesou_flood(FlatJuniperParser.Sesou_floodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesou_flood}.
	 * @param ctx the parse tree
	 */
	void exitSesou_flood(FlatJuniperParser.Sesou_floodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesou_port_scan}.
	 * @param ctx the parse tree
	 */
	void enterSesou_port_scan(FlatJuniperParser.Sesou_port_scanContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesou_port_scan}.
	 * @param ctx the parse tree
	 */
	void exitSesou_port_scan(FlatJuniperParser.Sesou_port_scanContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesou_udp_sweep}.
	 * @param ctx the parse tree
	 */
	void enterSesou_udp_sweep(FlatJuniperParser.Sesou_udp_sweepContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesou_udp_sweep}.
	 * @param ctx the parse tree
	 */
	void exitSesou_udp_sweep(FlatJuniperParser.Sesou_udp_sweepContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesopt_gre}.
	 * @param ctx the parse tree
	 */
	void enterSesopt_gre(FlatJuniperParser.Sesopt_greContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesopt_gre}.
	 * @param ctx the parse tree
	 */
	void exitSesopt_gre(FlatJuniperParser.Sesopt_greContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesopt_ip_in_udp}.
	 * @param ctx the parse tree
	 */
	void enterSesopt_ip_in_udp(FlatJuniperParser.Sesopt_ip_in_udpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesopt_ip_in_udp}.
	 * @param ctx the parse tree
	 */
	void exitSesopt_ip_in_udp(FlatJuniperParser.Sesopt_ip_in_udpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sesopt_ipip}.
	 * @param ctx the parse tree
	 */
	void enterSesopt_ipip(FlatJuniperParser.Sesopt_ipipContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sesopt_ipip}.
	 * @param ctx the parse tree
	 */
	void exitSesopt_ipip(FlatJuniperParser.Sesopt_ipipContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sez_security_zone}.
	 * @param ctx the parse tree
	 */
	void enterSez_security_zone(FlatJuniperParser.Sez_security_zoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sez_security_zone}.
	 * @param ctx the parse tree
	 */
	void exitSez_security_zone(FlatJuniperParser.Sez_security_zoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_address_book}.
	 * @param ctx the parse tree
	 */
	void enterSezs_address_book(FlatJuniperParser.Sezs_address_bookContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_address_book}.
	 * @param ctx the parse tree
	 */
	void exitSezs_address_book(FlatJuniperParser.Sezs_address_bookContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_application_tracking}.
	 * @param ctx the parse tree
	 */
	void enterSezs_application_tracking(FlatJuniperParser.Sezs_application_trackingContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_application_tracking}.
	 * @param ctx the parse tree
	 */
	void exitSezs_application_tracking(FlatJuniperParser.Sezs_application_trackingContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_host_inbound_traffic}.
	 * @param ctx the parse tree
	 */
	void enterSezs_host_inbound_traffic(FlatJuniperParser.Sezs_host_inbound_trafficContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_host_inbound_traffic}.
	 * @param ctx the parse tree
	 */
	void exitSezs_host_inbound_traffic(FlatJuniperParser.Sezs_host_inbound_trafficContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_interfaces}.
	 * @param ctx the parse tree
	 */
	void enterSezs_interfaces(FlatJuniperParser.Sezs_interfacesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_interfaces}.
	 * @param ctx the parse tree
	 */
	void exitSezs_interfaces(FlatJuniperParser.Sezs_interfacesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_screen}.
	 * @param ctx the parse tree
	 */
	void enterSezs_screen(FlatJuniperParser.Sezs_screenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_screen}.
	 * @param ctx the parse tree
	 */
	void exitSezs_screen(FlatJuniperParser.Sezs_screenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezs_tcp_rst}.
	 * @param ctx the parse tree
	 */
	void enterSezs_tcp_rst(FlatJuniperParser.Sezs_tcp_rstContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezs_tcp_rst}.
	 * @param ctx the parse tree
	 */
	void exitSezs_tcp_rst(FlatJuniperParser.Sezs_tcp_rstContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsa_address}.
	 * @param ctx the parse tree
	 */
	void enterSezsa_address(FlatJuniperParser.Sezsa_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsa_address}.
	 * @param ctx the parse tree
	 */
	void exitSezsa_address(FlatJuniperParser.Sezsa_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsa_address_set}.
	 * @param ctx the parse tree
	 */
	void enterSezsa_address_set(FlatJuniperParser.Sezsa_address_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsa_address_set}.
	 * @param ctx the parse tree
	 */
	void exitSezsa_address_set(FlatJuniperParser.Sezsa_address_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsaad_address}.
	 * @param ctx the parse tree
	 */
	void enterSezsaad_address(FlatJuniperParser.Sezsaad_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsaad_address}.
	 * @param ctx the parse tree
	 */
	void exitSezsaad_address(FlatJuniperParser.Sezsaad_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsaad_address_set}.
	 * @param ctx the parse tree
	 */
	void enterSezsaad_address_set(FlatJuniperParser.Sezsaad_address_setContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsaad_address_set}.
	 * @param ctx the parse tree
	 */
	void exitSezsaad_address_set(FlatJuniperParser.Sezsaad_address_setContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsh_protocols}.
	 * @param ctx the parse tree
	 */
	void enterSezsh_protocols(FlatJuniperParser.Sezsh_protocolsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsh_protocols}.
	 * @param ctx the parse tree
	 */
	void exitSezsh_protocols(FlatJuniperParser.Sezsh_protocolsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sezsh_system_services}.
	 * @param ctx the parse tree
	 */
	void enterSezsh_system_services(FlatJuniperParser.Sezsh_system_servicesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sezsh_system_services}.
	 * @param ctx the parse tree
	 */
	void exitSezsh_system_services(FlatJuniperParser.Sezsh_system_servicesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#zone}.
	 * @param ctx the parse tree
	 */
	void enterZone(FlatJuniperParser.ZoneContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#zone}.
	 * @param ctx the parse tree
	 */
	void exitZone(FlatJuniperParser.ZoneContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#s_system}.
	 * @param ctx the parse tree
	 */
	void enterS_system(FlatJuniperParser.S_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#s_system}.
	 * @param ctx the parse tree
	 */
	void exitS_system(FlatJuniperParser.S_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_authentication_method}.
	 * @param ctx the parse tree
	 */
	void enterSy_authentication_method(FlatJuniperParser.Sy_authentication_methodContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_authentication_method}.
	 * @param ctx the parse tree
	 */
	void exitSy_authentication_method(FlatJuniperParser.Sy_authentication_methodContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_authentication_order}.
	 * @param ctx the parse tree
	 */
	void enterSy_authentication_order(FlatJuniperParser.Sy_authentication_orderContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_authentication_order}.
	 * @param ctx the parse tree
	 */
	void exitSy_authentication_order(FlatJuniperParser.Sy_authentication_orderContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_default_address_selection}.
	 * @param ctx the parse tree
	 */
	void enterSy_default_address_selection(FlatJuniperParser.Sy_default_address_selectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_default_address_selection}.
	 * @param ctx the parse tree
	 */
	void exitSy_default_address_selection(FlatJuniperParser.Sy_default_address_selectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_domain_name}.
	 * @param ctx the parse tree
	 */
	void enterSy_domain_name(FlatJuniperParser.Sy_domain_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_domain_name}.
	 * @param ctx the parse tree
	 */
	void exitSy_domain_name(FlatJuniperParser.Sy_domain_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_host_name}.
	 * @param ctx the parse tree
	 */
	void enterSy_host_name(FlatJuniperParser.Sy_host_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_host_name}.
	 * @param ctx the parse tree
	 */
	void exitSy_host_name(FlatJuniperParser.Sy_host_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_name_server}.
	 * @param ctx the parse tree
	 */
	void enterSy_name_server(FlatJuniperParser.Sy_name_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_name_server}.
	 * @param ctx the parse tree
	 */
	void exitSy_name_server(FlatJuniperParser.Sy_name_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_ntp}.
	 * @param ctx the parse tree
	 */
	void enterSy_ntp(FlatJuniperParser.Sy_ntpContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_ntp}.
	 * @param ctx the parse tree
	 */
	void exitSy_ntp(FlatJuniperParser.Sy_ntpContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_null}.
	 * @param ctx the parse tree
	 */
	void enterSy_null(FlatJuniperParser.Sy_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_null}.
	 * @param ctx the parse tree
	 */
	void exitSy_null(FlatJuniperParser.Sy_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_porttype}.
	 * @param ctx the parse tree
	 */
	void enterSy_porttype(FlatJuniperParser.Sy_porttypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_porttype}.
	 * @param ctx the parse tree
	 */
	void exitSy_porttype(FlatJuniperParser.Sy_porttypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_ports}.
	 * @param ctx the parse tree
	 */
	void enterSy_ports(FlatJuniperParser.Sy_portsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_ports}.
	 * @param ctx the parse tree
	 */
	void exitSy_ports(FlatJuniperParser.Sy_portsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_root_authentication}.
	 * @param ctx the parse tree
	 */
	void enterSy_root_authentication(FlatJuniperParser.Sy_root_authenticationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_root_authentication}.
	 * @param ctx the parse tree
	 */
	void exitSy_root_authentication(FlatJuniperParser.Sy_root_authenticationContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_syslog}.
	 * @param ctx the parse tree
	 */
	void enterSy_syslog(FlatJuniperParser.Sy_syslogContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_syslog}.
	 * @param ctx the parse tree
	 */
	void exitSy_syslog(FlatJuniperParser.Sy_syslogContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_security_profile}.
	 * @param ctx the parse tree
	 */
	void enterSy_security_profile(FlatJuniperParser.Sy_security_profileContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_security_profile}.
	 * @param ctx the parse tree
	 */
	void exitSy_security_profile(FlatJuniperParser.Sy_security_profileContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_services}.
	 * @param ctx the parse tree
	 */
	void enterSy_services(FlatJuniperParser.Sy_servicesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_services}.
	 * @param ctx the parse tree
	 */
	void exitSy_services(FlatJuniperParser.Sy_servicesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_services_linetype}.
	 * @param ctx the parse tree
	 */
	void enterSy_services_linetype(FlatJuniperParser.Sy_services_linetypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_services_linetype}.
	 * @param ctx the parse tree
	 */
	void exitSy_services_linetype(FlatJuniperParser.Sy_services_linetypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_services_null}.
	 * @param ctx the parse tree
	 */
	void enterSy_services_null(FlatJuniperParser.Sy_services_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_services_null}.
	 * @param ctx the parse tree
	 */
	void exitSy_services_null(FlatJuniperParser.Sy_services_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sy_tacplus_server}.
	 * @param ctx the parse tree
	 */
	void enterSy_tacplus_server(FlatJuniperParser.Sy_tacplus_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sy_tacplus_server}.
	 * @param ctx the parse tree
	 */
	void exitSy_tacplus_server(FlatJuniperParser.Sy_tacplus_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syn_null}.
	 * @param ctx the parse tree
	 */
	void enterSyn_null(FlatJuniperParser.Syn_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syn_null}.
	 * @param ctx the parse tree
	 */
	void exitSyn_null(FlatJuniperParser.Syn_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syn_server}.
	 * @param ctx the parse tree
	 */
	void enterSyn_server(FlatJuniperParser.Syn_serverContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syn_server}.
	 * @param ctx the parse tree
	 */
	void exitSyn_server(FlatJuniperParser.Syn_serverContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syn_server_key}.
	 * @param ctx the parse tree
	 */
	void enterSyn_server_key(FlatJuniperParser.Syn_server_keyContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syn_server_key}.
	 * @param ctx the parse tree
	 */
	void exitSyn_server_key(FlatJuniperParser.Syn_server_keyContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syn_server_prefer}.
	 * @param ctx the parse tree
	 */
	void enterSyn_server_prefer(FlatJuniperParser.Syn_server_preferContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syn_server_prefer}.
	 * @param ctx the parse tree
	 */
	void exitSyn_server_prefer(FlatJuniperParser.Syn_server_preferContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syn_server_version}.
	 * @param ctx the parse tree
	 */
	void enterSyn_server_version(FlatJuniperParser.Syn_server_versionContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syn_server_version}.
	 * @param ctx the parse tree
	 */
	void exitSyn_server_version(FlatJuniperParser.Syn_server_versionContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syp_disable}.
	 * @param ctx the parse tree
	 */
	void enterSyp_disable(FlatJuniperParser.Syp_disableContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syp_disable}.
	 * @param ctx the parse tree
	 */
	void exitSyp_disable(FlatJuniperParser.Syp_disableContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syp_null}.
	 * @param ctx the parse tree
	 */
	void enterSyp_null(FlatJuniperParser.Syp_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syp_null}.
	 * @param ctx the parse tree
	 */
	void exitSyp_null(FlatJuniperParser.Syp_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syr_encrypted_password}.
	 * @param ctx the parse tree
	 */
	void enterSyr_encrypted_password(FlatJuniperParser.Syr_encrypted_passwordContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syr_encrypted_password}.
	 * @param ctx the parse tree
	 */
	void exitSyr_encrypted_password(FlatJuniperParser.Syr_encrypted_passwordContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sys_host}.
	 * @param ctx the parse tree
	 */
	void enterSys_host(FlatJuniperParser.Sys_hostContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sys_host}.
	 * @param ctx the parse tree
	 */
	void exitSys_host(FlatJuniperParser.Sys_hostContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sys_null}.
	 * @param ctx the parse tree
	 */
	void enterSys_null(FlatJuniperParser.Sys_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sys_null}.
	 * @param ctx the parse tree
	 */
	void exitSys_null(FlatJuniperParser.Sys_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sysh_null}.
	 * @param ctx the parse tree
	 */
	void enterSysh_null(FlatJuniperParser.Sysh_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sysh_null}.
	 * @param ctx the parse tree
	 */
	void exitSysh_null(FlatJuniperParser.Sysh_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sysl_null}.
	 * @param ctx the parse tree
	 */
	void enterSysl_null(FlatJuniperParser.Sysl_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sysl_null}.
	 * @param ctx the parse tree
	 */
	void exitSysl_null(FlatJuniperParser.Sysl_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sysp_logical_system}.
	 * @param ctx the parse tree
	 */
	void enterSysp_logical_system(FlatJuniperParser.Sysp_logical_systemContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sysp_logical_system}.
	 * @param ctx the parse tree
	 */
	void exitSysp_logical_system(FlatJuniperParser.Sysp_logical_systemContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#sysp_null}.
	 * @param ctx the parse tree
	 */
	void enterSysp_null(FlatJuniperParser.Sysp_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#sysp_null}.
	 * @param ctx the parse tree
	 */
	void exitSysp_null(FlatJuniperParser.Sysp_nullContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syt_secret}.
	 * @param ctx the parse tree
	 */
	void enterSyt_secret(FlatJuniperParser.Syt_secretContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syt_secret}.
	 * @param ctx the parse tree
	 */
	void exitSyt_secret(FlatJuniperParser.Syt_secretContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syt_source_address}.
	 * @param ctx the parse tree
	 */
	void enterSyt_source_address(FlatJuniperParser.Syt_source_addressContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syt_source_address}.
	 * @param ctx the parse tree
	 */
	void exitSyt_source_address(FlatJuniperParser.Syt_source_addressContext ctx);
	/**
	 * Enter a parse tree produced by {@link FlatJuniperParser#syt_null}.
	 * @param ctx the parse tree
	 */
	void enterSyt_null(FlatJuniperParser.Syt_nullContext ctx);
	/**
	 * Exit a parse tree produced by {@link FlatJuniperParser#syt_null}.
	 * @param ctx the parse tree
	 */
	void exitSyt_null(FlatJuniperParser.Syt_nullContext ctx);
}