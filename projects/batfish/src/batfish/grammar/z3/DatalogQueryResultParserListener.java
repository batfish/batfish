// Generated from /home/arifogel/git/batfish/projects/batfish/src/batfish/grammar/z3/DatalogQueryResultParser.g4 by ANTLR 4.4

package batfish.grammar.z3;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DatalogQueryResultParser}.
 */
public interface DatalogQueryResultParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#result}.
	 * @param ctx the parse tree
	 */
	void enterResult(@NotNull DatalogQueryResultParser.ResultContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#result}.
	 * @param ctx the parse tree
	 */
	void exitResult(@NotNull DatalogQueryResultParser.ResultContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#boolean_expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_expr(@NotNull DatalogQueryResultParser.Boolean_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#boolean_expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_expr(@NotNull DatalogQueryResultParser.Boolean_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd_expr(@NotNull DatalogQueryResultParser.And_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#and_expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd_expr(@NotNull DatalogQueryResultParser.And_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void enterOr_expr(@NotNull DatalogQueryResultParser.Or_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#or_expr}.
	 * @param ctx the parse tree
	 */
	void exitOr_expr(@NotNull DatalogQueryResultParser.Or_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#var_int_expr}.
	 * @param ctx the parse tree
	 */
	void enterVar_int_expr(@NotNull DatalogQueryResultParser.Var_int_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#var_int_expr}.
	 * @param ctx the parse tree
	 */
	void exitVar_int_expr(@NotNull DatalogQueryResultParser.Var_int_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#eq_expr}.
	 * @param ctx the parse tree
	 */
	void enterEq_expr(@NotNull DatalogQueryResultParser.Eq_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#eq_expr}.
	 * @param ctx the parse tree
	 */
	void exitEq_expr(@NotNull DatalogQueryResultParser.Eq_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#int_expr}.
	 * @param ctx the parse tree
	 */
	void enterInt_expr(@NotNull DatalogQueryResultParser.Int_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#int_expr}.
	 * @param ctx the parse tree
	 */
	void exitInt_expr(@NotNull DatalogQueryResultParser.Int_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#extract_expr}.
	 * @param ctx the parse tree
	 */
	void enterExtract_expr(@NotNull DatalogQueryResultParser.Extract_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#extract_expr}.
	 * @param ctx the parse tree
	 */
	void exitExtract_expr(@NotNull DatalogQueryResultParser.Extract_exprContext ctx);
	/**
	 * Enter a parse tree produced by {@link DatalogQueryResultParser#lit_int_expr}.
	 * @param ctx the parse tree
	 */
	void enterLit_int_expr(@NotNull DatalogQueryResultParser.Lit_int_exprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DatalogQueryResultParser#lit_int_expr}.
	 * @param ctx the parse tree
	 */
	void exitLit_int_expr(@NotNull DatalogQueryResultParser.Lit_int_exprContext ctx);
}