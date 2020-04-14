package org.batfish.grammar.arista;

import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.arista.AristaLexer;
import org.batfish.grammar.arista.AristaParser;
import org.batfish.grammar.arista.AristaParser.Cisco_configurationContext;

public class AristaCombinedParser extends BatfishCombinedParser<AristaParser, AristaLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategyFactory(AristaLexer.NEWLINE, "\n");

  public static final String DEBUG_FLAG_NO_USE_ARISTA_BGP = "noaristabgp";

  public AristaCombinedParser(String input, GrammarSettings settings, ConfigurationFormat format) {
    super(
        AristaParser.class,
        AristaLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
    boolean eos = format == ConfigurationFormat.ARISTA;
    boolean cadant = format == ConfigurationFormat.CADANT;
    _lexer.setAsa(format == ConfigurationFormat.CISCO_ASA);
    _lexer.setCadant(cadant);
    _lexer.setEos(eos);
    _lexer.setFoundry(format == ConfigurationFormat.FOUNDRY);
    _lexer.setIos(format == ConfigurationFormat.CISCO_IOS);
    _parser.setAristaBgp(settings.getUseAristaBgp() && format == ConfigurationFormat.ARISTA);
    _parser.setAsa(format == ConfigurationFormat.CISCO_ASA);
    _parser.setEos(eos);
    _parser.setCadant(cadant);
    _parser.setMultilineBgpNeighbors(false);
  }

  @Override
  public Cisco_configurationContext parse() {
    return _parser.cisco_configuration();
  }
}
