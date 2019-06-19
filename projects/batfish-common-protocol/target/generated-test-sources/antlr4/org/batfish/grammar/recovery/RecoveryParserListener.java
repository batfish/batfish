// Generated from org/batfish/grammar/recovery/RecoveryParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.recovery;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RecoveryParser}.
 */
public interface RecoveryParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#block_statement}.
	 * @param ctx the parse tree
	 */
	void enterBlock_statement(RecoveryParser.Block_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#block_statement}.
	 * @param ctx the parse tree
	 */
	void exitBlock_statement(RecoveryParser.Block_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#inner_statement}.
	 * @param ctx the parse tree
	 */
	void enterInner_statement(RecoveryParser.Inner_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#inner_statement}.
	 * @param ctx the parse tree
	 */
	void exitInner_statement(RecoveryParser.Inner_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(RecoveryParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(RecoveryParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#recovery}.
	 * @param ctx the parse tree
	 */
	void enterRecovery(RecoveryParser.RecoveryContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#recovery}.
	 * @param ctx the parse tree
	 */
	void exitRecovery(RecoveryParser.RecoveryContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#simple_statement}.
	 * @param ctx the parse tree
	 */
	void enterSimple_statement(RecoveryParser.Simple_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#simple_statement}.
	 * @param ctx the parse tree
	 */
	void exitSimple_statement(RecoveryParser.Simple_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RecoveryParser#tail_word}.
	 * @param ctx the parse tree
	 */
	void enterTail_word(RecoveryParser.Tail_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link RecoveryParser#tail_word}.
	 * @param ctx the parse tree
	 */
	void exitTail_word(RecoveryParser.Tail_wordContext ctx);
}