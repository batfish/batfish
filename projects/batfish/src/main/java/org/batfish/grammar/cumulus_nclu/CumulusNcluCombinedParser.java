package org.batfish.grammar.cumulus_nclu;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;

/** A {@link BatfishCombinedParser} for Cumulus NCLU configuration files. */
public final class CumulusNcluCombinedParser
    extends BatfishCombinedParser<CumulusNcluParser, CumulusNcluLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
          CumulusNcluLexer.NEWLINE, "\n");

  public CumulusNcluCombinedParser(String input, GrammarSettings settings) {
    super(
        CumulusNcluParser.class,
        CumulusNcluLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Cumulus_nclu_configurationContext parse() {
    return _parser.cumulus_nclu_configuration();
  }
}
