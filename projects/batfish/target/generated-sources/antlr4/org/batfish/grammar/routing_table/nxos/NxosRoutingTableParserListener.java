// Generated from org/batfish/grammar/routing_table/nxos/NxosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.nxos;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NxosRoutingTableParser}.
 */
public interface NxosRoutingTableParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#double_quoted_string}.
	 * @param ctx the parse tree
	 */
	void enterDouble_quoted_string(NxosRoutingTableParser.Double_quoted_stringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#double_quoted_string}.
	 * @param ctx the parse tree
	 */
	void exitDouble_quoted_string(NxosRoutingTableParser.Double_quoted_stringContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#interface_name}.
	 * @param ctx the parse tree
	 */
	void enterInterface_name(NxosRoutingTableParser.Interface_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#interface_name}.
	 * @param ctx the parse tree
	 */
	void exitInterface_name(NxosRoutingTableParser.Interface_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#network}.
	 * @param ctx the parse tree
	 */
	void enterNetwork(NxosRoutingTableParser.NetworkContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#network}.
	 * @param ctx the parse tree
	 */
	void exitNetwork(NxosRoutingTableParser.NetworkContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#nxos_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterNxos_routing_table(NxosRoutingTableParser.Nxos_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#nxos_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitNxos_routing_table(NxosRoutingTableParser.Nxos_routing_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void enterProtocol(NxosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void exitProtocol(NxosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(NxosRoutingTableParser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(NxosRoutingTableParser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void enterVrf_declaration(NxosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void exitVrf_declaration(NxosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link NxosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterVrf_routing_table(NxosRoutingTableParser.Vrf_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link NxosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitVrf_routing_table(NxosRoutingTableParser.Vrf_routing_tableContext ctx);
}