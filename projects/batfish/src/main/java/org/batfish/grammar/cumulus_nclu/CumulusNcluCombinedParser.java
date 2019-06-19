package org.batfish.grammar.cumulus_nclu;

import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;

/** A {@link BatfishCombinedParser} for Cumulus NCLU configuration files. */
public final class CumulusNcluCombinedParser
    extends BatfishCombinedParser<CumulusNcluParser, CumulusNcluLexer> {

  public CumulusNcluCombinedParser(String input, Settings settings) {
    super(CumulusNcluParser.class, CumulusNcluLexer.class, input, settings);
  }

  @Override
  public Cumulus_nclu_configurationContext parse() {
    return _parser.cumulus_nclu_configuration();
  }
}
