// Generated from org/batfish/grammar/vyos/VyosParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.vyos;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link VyosParser}.
 */
public interface VyosParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link VyosParser#braced_clause}.
	 * @param ctx the parse tree
	 */
	void enterBraced_clause(VyosParser.Braced_clauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link VyosParser#braced_clause}.
	 * @param ctx the parse tree
	 */
	void exitBraced_clause(VyosParser.Braced_clauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link VyosParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(VyosParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link VyosParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(VyosParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link VyosParser#terminator}.
	 * @param ctx the parse tree
	 */
	void enterTerminator(VyosParser.TerminatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link VyosParser#terminator}.
	 * @param ctx the parse tree
	 */
	void exitTerminator(VyosParser.TerminatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link VyosParser#vyos_configuration}.
	 * @param ctx the parse tree
	 */
	void enterVyos_configuration(VyosParser.Vyos_configurationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VyosParser#vyos_configuration}.
	 * @param ctx the parse tree
	 */
	void exitVyos_configuration(VyosParser.Vyos_configurationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VyosParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(VyosParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link VyosParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(VyosParser.WordContext ctx);
}