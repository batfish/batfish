package org.batfish.grammar.cumulus_interfaces;

import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;

public final class CumulusInterfacesCombinedParser
    extends BatfishCombinedParser<CumulusInterfacesParser, CumulusInterfacesLexer> {
  public CumulusInterfacesCombinedParser(
      String input, GrammarSettings settings, int line, int offset) {
    super(CumulusInterfacesParser.class, CumulusInterfacesLexer.class, input, settings);
    _lexer.getInterpreter().setLine(line);
    _lexer.getInputStream().seek(offset);
  }

  @Override
  public Cumulus_interfaces_configurationContext parse() {
    return _parser.cumulus_interfaces_configuration();
  }
}
