package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;

/** Interface providing common parse-tree listener utility methods with default implementations. */
@ParametersAreNonnullByDefault
public interface BatfishListener extends ParseTreeListener {

  /**
   * Returns the full text of of the provided {@link ParserRuleContext}. If the parse tree has been
   * preprocessed, a client may want to override this method to produce the correct text for each
   * parse tree node.
   */
  default @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return getInputText().substring(start, end + 1);
  }

  /**
   * Returns a version of the text of the provided {@link ParserRuleContext} suitable for a warning.
   * By default, just returns {@link #getFullText(ParserRuleContext)}. Clients may want to override
   * this method to produce something more concise.
   */
  default @Nonnull String getWarningText(ParserRuleContext ctx) {
    return getFullText(ctx);
  }

  /** Return the input text processed by this listener. */
  @Nonnull
  String getInputText();

  /** Return the parser used to parse the input text. */
  @Nonnull
  BatfishCombinedParser<?, ?> getParser();

  /** Return the {@link Warnings} this listener may want to update as it processes the input. */
  @Nonnull
  Warnings getWarnings();

  /**
   * Helper for adding todo warning for {@link ParserRuleContext}. Generally should not be
   * overridden.
   */
  default void todo(ParserRuleContext ctx) {
    getWarnings().todo(ctx, getWarningText(ctx), getParser());
  }

  /**
   * Helper for adding generic warning for {@link ParserRuleContext}. Generally should not be
   * overridden.
   */
  default void warn(ParserRuleContext ctx, String message) {
    getWarnings().addWarning(ctx, getWarningText(ctx), getParser(), message);
  }

  /**
   * Helper for adding generic warning for {@link ParserRuleContext}, using text from the given
   * {@link TerminalNode}. Generally should not be overridden.
   */
  default void warn(ParserRuleContext ctx, TerminalNode node, String message) {
    String text = node.getText();
    getWarnings().addWarningOnLine(node.getSymbol().getLine(), ctx, text, getParser(), message);
  }

  /**
   * Warn helper for risky warnings that indicate a syntax that can parse but may result in
   * unexpected behavior.
   */
  default void warnRisky(ParserRuleContext ctx, String message) {
    getWarnings().addRiskyWarning(ctx, getWarningText(ctx), getParser(), message);
  }

  /**
   * Warn helper for risky warnings that indicate a syntax that can parse but may result in
   * unexpected behavior. Uses text from the given {@link TerminalNode}.
   */
  default void warnRisky(ParserRuleContext ctx, TerminalNode node, String message) {
    String text = node.getText();
    getWarnings()
        .addRiskyWarningOnLine(node.getSymbol().getLine(), ctx, text, getParser(), message);
  }
}
