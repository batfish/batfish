package org.batfish.grammar.cisco_xr;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cisco_xr.CiscoXrParser.Cisco_xr_configurationContext;

public class CiscoXrCombinedParser extends BatfishCombinedParser<CiscoXrParser, CiscoXrLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CiscoXrLexer.NEWLINE, "\n");

  public static final String DEBUG_FLAG_USE_ARISTA_BGP = "aristabgp";

  public CiscoXrCombinedParser(String input, GrammarSettings settings, ConfigurationFormat format) {
    super(
        CiscoXrParser.class,
        CiscoXrLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
    boolean multilineBgpNeighbors;
    switch (format) {
      case FOUNDRY:
      case ARISTA:
      case CADANT:
      case CISCO_ASA:
      case CISCO_IOS:
      case FORCE10:
      case ARUBAOS: // aruba controllers don't support bgp so can't define multiline bgp neighbors
        multilineBgpNeighbors = false;
        break;

      case CISCO_IOS_XR:
        multilineBgpNeighbors = true;
        break;

        // $CASES-OMITTED$
      default:
        throw new BatfishException("Should not be possible");
    }
    boolean eos = format == ConfigurationFormat.ARISTA;
    boolean cadant = format == ConfigurationFormat.CADANT;
    _lexer.setAsa(format == ConfigurationFormat.CISCO_ASA);
    _lexer.setCadant(cadant);
    _lexer.setEos(eos);
    _lexer.setFoundry(format == ConfigurationFormat.FOUNDRY);
    _lexer.setIos(format == ConfigurationFormat.CISCO_IOS);
    _lexer.setIosXr(format == ConfigurationFormat.CISCO_IOS_XR);
    _parser.setAristaBgp(settings.getUseAristaBgp() && format == ConfigurationFormat.ARISTA);
    _parser.setAsa(format == ConfigurationFormat.CISCO_ASA);
    _parser.setEos(eos);
    _parser.setCadant(cadant);
    _parser.setMultilineBgpNeighbors(multilineBgpNeighbors);
  }

  @Override
  public Cisco_xr_configurationContext parse() {
    return _parser.cisco_xr_configuration();
  }
}
