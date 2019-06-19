// Generated from org/batfish/grammar/iptables/IptablesParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.iptables;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IptablesParser}.
 */
public interface IptablesParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IptablesParser#iptables_configuration}.
	 * @param ctx the parse tree
	 */
	void enterIptables_configuration(IptablesParser.Iptables_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#iptables_configuration}.
	 * @param ctx the parse tree
	 */
	void exitIptables_configuration(IptablesParser.Iptables_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(IptablesParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(IptablesParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#chain}.
	 * @param ctx the parse tree
	 */
	void enterChain(IptablesParser.ChainContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#chain}.
	 * @param ctx the parse tree
	 */
	void exitChain(IptablesParser.ChainContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#declaration_chain_policy}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration_chain_policy(IptablesParser.Declaration_chain_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#declaration_chain_policy}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration_chain_policy(IptablesParser.Declaration_chain_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#declaration_table}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration_table(IptablesParser.Declaration_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#declaration_table}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration_table(IptablesParser.Declaration_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#table}.
	 * @param ctx the parse tree
	 */
	void enterTable(IptablesParser.TableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#table}.
	 * @param ctx the parse tree
	 */
	void exitTable(IptablesParser.TableContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command}.
	 * @param ctx the parse tree
	 */
	void enterCommand(IptablesParser.CommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command}.
	 * @param ctx the parse tree
	 */
	void exitCommand(IptablesParser.CommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_append}.
	 * @param ctx the parse tree
	 */
	void enterCommand_append(IptablesParser.Command_appendContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_append}.
	 * @param ctx the parse tree
	 */
	void exitCommand_append(IptablesParser.Command_appendContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_check}.
	 * @param ctx the parse tree
	 */
	void enterCommand_check(IptablesParser.Command_checkContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_check}.
	 * @param ctx the parse tree
	 */
	void exitCommand_check(IptablesParser.Command_checkContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_delete}.
	 * @param ctx the parse tree
	 */
	void enterCommand_delete(IptablesParser.Command_deleteContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_delete}.
	 * @param ctx the parse tree
	 */
	void exitCommand_delete(IptablesParser.Command_deleteContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_delete_chain}.
	 * @param ctx the parse tree
	 */
	void enterCommand_delete_chain(IptablesParser.Command_delete_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_delete_chain}.
	 * @param ctx the parse tree
	 */
	void exitCommand_delete_chain(IptablesParser.Command_delete_chainContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_flush}.
	 * @param ctx the parse tree
	 */
	void enterCommand_flush(IptablesParser.Command_flushContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_flush}.
	 * @param ctx the parse tree
	 */
	void exitCommand_flush(IptablesParser.Command_flushContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_help}.
	 * @param ctx the parse tree
	 */
	void enterCommand_help(IptablesParser.Command_helpContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_help}.
	 * @param ctx the parse tree
	 */
	void exitCommand_help(IptablesParser.Command_helpContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_insert}.
	 * @param ctx the parse tree
	 */
	void enterCommand_insert(IptablesParser.Command_insertContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_insert}.
	 * @param ctx the parse tree
	 */
	void exitCommand_insert(IptablesParser.Command_insertContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_list}.
	 * @param ctx the parse tree
	 */
	void enterCommand_list(IptablesParser.Command_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_list}.
	 * @param ctx the parse tree
	 */
	void exitCommand_list(IptablesParser.Command_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_list_rules}.
	 * @param ctx the parse tree
	 */
	void enterCommand_list_rules(IptablesParser.Command_list_rulesContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_list_rules}.
	 * @param ctx the parse tree
	 */
	void exitCommand_list_rules(IptablesParser.Command_list_rulesContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_new_chain}.
	 * @param ctx the parse tree
	 */
	void enterCommand_new_chain(IptablesParser.Command_new_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_new_chain}.
	 * @param ctx the parse tree
	 */
	void exitCommand_new_chain(IptablesParser.Command_new_chainContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_policy}.
	 * @param ctx the parse tree
	 */
	void enterCommand_policy(IptablesParser.Command_policyContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_policy}.
	 * @param ctx the parse tree
	 */
	void exitCommand_policy(IptablesParser.Command_policyContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_rename_chain}.
	 * @param ctx the parse tree
	 */
	void enterCommand_rename_chain(IptablesParser.Command_rename_chainContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_rename_chain}.
	 * @param ctx the parse tree
	 */
	void exitCommand_rename_chain(IptablesParser.Command_rename_chainContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_replace}.
	 * @param ctx the parse tree
	 */
	void enterCommand_replace(IptablesParser.Command_replaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_replace}.
	 * @param ctx the parse tree
	 */
	void exitCommand_replace(IptablesParser.Command_replaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_zero}.
	 * @param ctx the parse tree
	 */
	void enterCommand_zero(IptablesParser.Command_zeroContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_zero}.
	 * @param ctx the parse tree
	 */
	void exitCommand_zero(IptablesParser.Command_zeroContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#command_tail}.
	 * @param ctx the parse tree
	 */
	void enterCommand_tail(IptablesParser.Command_tailContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#command_tail}.
	 * @param ctx the parse tree
	 */
	void exitCommand_tail(IptablesParser.Command_tailContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#endpoint}.
	 * @param ctx the parse tree
	 */
	void enterEndpoint(IptablesParser.EndpointContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#endpoint}.
	 * @param ctx the parse tree
	 */
	void exitEndpoint(IptablesParser.EndpointContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#match}.
	 * @param ctx the parse tree
	 */
	void enterMatch(IptablesParser.MatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#match}.
	 * @param ctx the parse tree
	 */
	void exitMatch(IptablesParser.MatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#match_module}.
	 * @param ctx the parse tree
	 */
	void enterMatch_module(IptablesParser.Match_moduleContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#match_module}.
	 * @param ctx the parse tree
	 */
	void exitMatch_module(IptablesParser.Match_moduleContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#match_module_tcp}.
	 * @param ctx the parse tree
	 */
	void enterMatch_module_tcp(IptablesParser.Match_module_tcpContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#match_module_tcp}.
	 * @param ctx the parse tree
	 */
	void exitMatch_module_tcp(IptablesParser.Match_module_tcpContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#other_options}.
	 * @param ctx the parse tree
	 */
	void enterOther_options(IptablesParser.Other_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#other_options}.
	 * @param ctx the parse tree
	 */
	void exitOther_options(IptablesParser.Other_optionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#protocol}.
	 * @param ctx the parse tree
	 */
	void enterProtocol(IptablesParser.ProtocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#protocol}.
	 * @param ctx the parse tree
	 */
	void exitProtocol(IptablesParser.ProtocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#rule_spec}.
	 * @param ctx the parse tree
	 */
	void enterRule_spec(IptablesParser.Rule_specContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#rule_spec}.
	 * @param ctx the parse tree
	 */
	void exitRule_spec(IptablesParser.Rule_specContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#built_in_target}.
	 * @param ctx the parse tree
	 */
	void enterBuilt_in_target(IptablesParser.Built_in_targetContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#built_in_target}.
	 * @param ctx the parse tree
	 */
	void exitBuilt_in_target(IptablesParser.Built_in_targetContext ctx);
	/**
	 * Enter a parse tree produced by {@link IptablesParser#target_options}.
	 * @param ctx the parse tree
	 */
	void enterTarget_options(IptablesParser.Target_optionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link IptablesParser#target_options}.
	 * @param ctx the parse tree
	 */
	void exitTarget_options(IptablesParser.Target_optionsContext ctx);
}