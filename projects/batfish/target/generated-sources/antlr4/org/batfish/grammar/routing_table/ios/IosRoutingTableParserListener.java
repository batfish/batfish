// Generated from org/batfish/grammar/routing_table/ios/IosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.ios;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link IosRoutingTableParser}.
 */
public interface IosRoutingTableParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#code}.
	 * @param ctx the parse tree
	 */
	void enterCode(IosRoutingTableParser.CodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#code}.
	 * @param ctx the parse tree
	 */
	void exitCode(IosRoutingTableParser.CodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#codes_declaration}.
	 * @param ctx the parse tree
	 */
	void enterCodes_declaration(IosRoutingTableParser.Codes_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#codes_declaration}.
	 * @param ctx the parse tree
	 */
	void exitCodes_declaration(IosRoutingTableParser.Codes_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#code_line}.
	 * @param ctx the parse tree
	 */
	void enterCode_line(IosRoutingTableParser.Code_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#code_line}.
	 * @param ctx the parse tree
	 */
	void exitCode_line(IosRoutingTableParser.Code_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#ios_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterIos_routing_table(IosRoutingTableParser.Ios_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#ios_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitIos_routing_table(IosRoutingTableParser.Ios_routing_tableContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#gateway_header}.
	 * @param ctx the parse tree
	 */
	void enterGateway_header(IosRoutingTableParser.Gateway_headerContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#gateway_header}.
	 * @param ctx the parse tree
	 */
	void exitGateway_header(IosRoutingTableParser.Gateway_headerContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(IosRoutingTableParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(IosRoutingTableParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#info}.
	 * @param ctx the parse tree
	 */
	void enterInfo(IosRoutingTableParser.InfoContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#info}.
	 * @param ctx the parse tree
	 */
	void exitInfo(IosRoutingTableParser.InfoContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void enterProtocol(IosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#protocol}.
	 * @param ctx the parse tree
	 */
	void exitProtocol(IosRoutingTableParser.ProtocolContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void enterRoute(IosRoutingTableParser.RouteContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#route}.
	 * @param ctx the parse tree
	 */
	void exitRoute(IosRoutingTableParser.RouteContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#time}.
	 * @param ctx the parse tree
	 */
	void enterTime(IosRoutingTableParser.TimeContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#time}.
	 * @param ctx the parse tree
	 */
	void exitTime(IosRoutingTableParser.TimeContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void enterVrf_declaration(IosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#vrf_declaration}.
	 * @param ctx the parse tree
	 */
	void exitVrf_declaration(IosRoutingTableParser.Vrf_declarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link IosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void enterVrf_routing_table(IosRoutingTableParser.Vrf_routing_tableContext ctx);
	/**
	 * Exit a parse tree produced by {@link IosRoutingTableParser#vrf_routing_table}.
	 * @param ctx the parse tree
	 */
	void exitVrf_routing_table(IosRoutingTableParser.Vrf_routing_tableContext ctx);
}