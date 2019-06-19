package org.batfish.grammar.cisco;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cisco.CiscoParser.Cisco_configurationContext;

public class CiscoCombinedParser extends BatfishCombinedParser<CiscoParser, CiscoLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(CiscoLexer.NEWLINE, "\n");

  public CiscoCombinedParser(String input, GrammarSettings settings, ConfigurationFormat format) {
    super(
        CiscoParser.class,
        CiscoLexer.class,
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
      case CISCO_NX:
        multilineBgpNeighbors = true;
        break;

        // $CASES-OMITTED$
      default:
        throw new BatfishException("Should not be possible");
    }
    _lexer.setAsa(format == ConfigurationFormat.CISCO_ASA);
    boolean cadant = format == ConfigurationFormat.CADANT;
    _lexer.setCadant(cadant);
    _lexer.setFoundry(format == ConfigurationFormat.FOUNDRY);
    _parser.setAsa(format == ConfigurationFormat.CISCO_ASA);
    _parser.setEos(format == ConfigurationFormat.ARISTA);
    _parser.setCadant(cadant);
    _parser.setNxos(format == ConfigurationFormat.CISCO_NX);
    _parser.setMultilineBgpNeighbors(multilineBgpNeighbors);
  }

  @Override
  public Cisco_configurationContext parse() {
    return _parser.cisco_configuration();
  }
}
