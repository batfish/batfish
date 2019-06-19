// Generated from org/batfish/grammar/juniper/JuniperParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.juniper;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link JuniperParser}.
 */
public interface JuniperParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link JuniperParser#braced_clause}.
	 * @param ctx the parse tree
	 */
	void enterBraced_clause(JuniperParser.Braced_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#braced_clause}.
	 * @param ctx the parse tree
	 */
	void exitBraced_clause(JuniperParser.Braced_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JuniperParser#bracketed_clause}.
	 * @param ctx the parse tree
	 */
	void enterBracketed_clause(JuniperParser.Bracketed_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#bracketed_clause}.
	 * @param ctx the parse tree
	 */
	void exitBracketed_clause(JuniperParser.Bracketed_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link JuniperParser#juniper_configuration}.
	 * @param ctx the parse tree
	 */
	void enterJuniper_configuration(JuniperParser.Juniper_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#juniper_configuration}.
	 * @param ctx the parse tree
	 */
	void exitJuniper_configuration(JuniperParser.Juniper_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link JuniperParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(JuniperParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(JuniperParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link JuniperParser#terminator}.
	 * @param ctx the parse tree
	 */
	void enterTerminator(JuniperParser.TerminatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#terminator}.
	 * @param ctx the parse tree
	 */
	void exitTerminator(JuniperParser.TerminatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link JuniperParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(JuniperParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link JuniperParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(JuniperParser.WordContext ctx);
}