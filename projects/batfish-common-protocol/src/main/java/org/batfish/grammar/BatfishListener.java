package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;

/** Interface providing common parse-tree listener utility methods with default implementations. */
@ParametersAreNonnullByDefault
public interface BatfishListener {

  default @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return getInputText().substring(start, end + 1);
  }

  @Nonnull
  String getInputText();

  @Nonnull
  BatfishCombinedParser<?, ?> getParser();

  @Nonnull
  Warnings getWarnings();

  default void todo(ParserRuleContext ctx) {
    getWarnings().todo(ctx, getFullText(ctx), getParser());
  }

  default void warn(ParserRuleContext ctx, String message) {
    getWarnings().addWarning(ctx, getFullText(ctx), getParser(), message);
  }
}
