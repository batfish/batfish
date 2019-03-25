package org.batfish.grammar.cumulus_nclu;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;

/**
 * A listener that builds a {@link CumulusNcluConfiguration} while walking a parse tree produced by
 * {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluConfigurationBuilder extends CumulusNcluParserBaseListener {

  private @Nullable CumulusNcluConfiguration _c;
  private final @Nonnull CumulusNcluCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  public CumulusNcluConfigurationBuilder(
      CumulusNcluCombinedParser parser, String text, Warnings w) {
    _parser = parser;
    _text = text;
    _w = w;
  }

  @SuppressWarnings("unused")
  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(
        String.format("Could not convert to %s: %s", returnType.getSimpleName(), getFullText(ctx)));
    return defaultReturnValue;
  }

  @Override
  public void enterCumulus_nclu_configuration(Cumulus_nclu_configurationContext ctx) {
    _c = new CumulusNcluConfiguration();
  }

  /**
   * Returns built {@link CumulusNcluConfiguration}.
   *
   * @throws IllegalStateException if called before walking parse tree produced by {@link
   *     CumulusNcluCombinedParser#parse}
   */
  public @Nonnull CumulusNcluConfiguration getConfiguration() {
    checkState(
        _c != null,
        "Cannot return vendor configuration before walking valid Cumulus NCLU parse tree");
    return _c;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  @SuppressWarnings("unused")
  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }
}
