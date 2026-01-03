package org.batfish.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection.SilentSyntaxElem;

/**
 * Interface extending {@link BatfishListener} that provides utility methods for recording silent
 * syntax within a parse tree.
 */
@ParametersAreNonnullByDefault
public interface SilentSyntaxListener extends BatfishListener {

  /**
   * If printing parse tree, also collect silent syntax metadata. Implementers overriding this
   * default method should call it.
   */
  default void tryProcessSilentSyntax(ParserRuleContext ctx) {
    BatfishCombinedParser<?, ?> parser = getParser();
    GrammarSettings settings = parser.getSettings();
    if (!settings.getPrintParseTree() || !isSilentSyntax(ctx) || isGenerated(ctx)) {
      return;
    }
    getSilentSyntax()
        .addElement(
            new SilentSyntaxElem(
                getParser().getRuleName(ctx), ctx.start.getLine(), getFullText(ctx)));
  }

  /**
   * Returns true iff the supplied context is purely generated, rather than part of the original
   * input text. Default implementation returns {@code false} for any context.
   */
  default boolean isGenerated(ParserRuleContext ctx) {
    return false;
  }

  /**
   * Returns true iff the supplied parse tree node is considered to be silent syntax. Default
   * implementation returns true iff the rule name ends in {@code _null}.
   */
  default boolean isSilentSyntax(ParserRuleContext ctx) {
    return getParser().getRuleName(ctx).endsWith("_null");
  }

  /**
   * Returns {@link SilentSyntaxCollection} to be populated by this {@link SilentSyntaxListener}.
   */
  @Nonnull
  SilentSyntaxCollection getSilentSyntax();
}
