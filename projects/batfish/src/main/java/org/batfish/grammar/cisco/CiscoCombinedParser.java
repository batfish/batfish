package org.batfish.grammar.cisco;

import org.batfish.common.BatfishException;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.cisco.CiscoParser.Cisco_configurationContext;

public class CiscoCombinedParser extends BatfishCombinedParser<CiscoParser, CiscoLexer> {

  @SuppressWarnings("fallthrough")
  public CiscoCombinedParser(String input, Settings settings, ConfigurationFormat format) {
    super(CiscoParser.class, CiscoLexer.class, input, settings);
    boolean multilineBgpNeighbors;
    boolean foundry = false;

    // do not rearrange cases
    switch (format) {
      case FOUNDRY:
        foundry = true;
        // fall through
      case ARISTA:
      case CISCO_IOS:
      case FORCE10:
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
    _lexer.setFoundry(foundry);
    _parser.setMultilineBgpNeighbors(multilineBgpNeighbors);
    _parser.setDisableUnrecognized(settings.getDisableUnrecognized());
  }

  @Override
  public Cisco_configurationContext parse() {
    return _parser.cisco_configuration();
  }
}
