// Generated from org/batfish/grammar/routing_table/eos/EosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.eos;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link EosRoutingTableParser}.
 */
public interface EosRoutingTableParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#code}.
	 * @param ctx the parse tree
	 */
	void enterCode(EosRoutingTableParser.CodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#code}.
	 * @param ctx the parse tree
	 */
	void exitCode(EosRoutingTableParser.CodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#codes_declaration}.
	 * @param ctx the parse tree
	 */
	void enterCodes_declaration(EosRoutingTableParser.Codes_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#codes_declaration}.
	 * @param ctx the parse tree
	 */
	void exitCodes_declaration(EosRoutingTableParser.Codes_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#eos_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterEos_routing_table(EosRoutingTableParser.Eos_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#eos_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitEos_routing_table(EosRoutingTableParser.Eos_routing_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#gateway_header}.
	 * @param ctx the parse tree
	 */
	void enterGateway_header(EosRoutingTableParser.Gateway_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#gateway_header}.
	 * @param ctx the parse tree
	 */
	void exitGateway_header(EosRoutingTableParser.Gateway_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(EosRoutingTableParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(EosRoutingTableParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void enterProtocol(EosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void exitProtocol(EosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(EosRoutingTableParser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(EosRoutingTableParser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void enterVrf_declaration(EosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void exitVrf_declaration(EosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link EosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterVrf_routing_table(EosRoutingTableParser.Vrf_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link EosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitVrf_routing_table(EosRoutingTableParser.Vrf_routing_tableContext ctx);
}