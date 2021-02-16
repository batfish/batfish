package org.batfish.grammar.cumulus_nclu;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.cumulus_nclu.CumulusNcluConfiguration;
import org.batfish.vendor.VendorConfiguration;

/**
 * A {@link ControlPlaneExtractor} that produces a {@link CumulusNcluConfiguration} from a parse
 * tree returned by {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluControlPlaneExtractor implements ControlPlaneExtractor {

  private CumulusNcluConfiguration _configuration;
  private final CumulusNcluCombinedParser _parser;
  private final String _text;
  private final Warnings _w;

  public CumulusNcluControlPlaneExtractor(
      String fileText, CumulusNcluCombinedParser combinedParser, Warnings warnings) {
    _text = fileText;
    _parser = combinedParser;
    _w = warnings;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    CumulusNcluConfigurationBuilder cb = new CumulusNcluConfigurationBuilder(_parser, _text, _w);
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
