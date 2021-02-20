package org.batfish.grammar.palo_alto;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class PaloAltoControlPlaneExtractor implements ControlPlaneExtractor {

  private final PaloAltoCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private PaloAltoConfiguration _configuration;

  public PaloAltoControlPlaneExtractor(
      String fileText, PaloAltoCombinedParser combinedParser, Warnings warnings) {
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
    PreprocessPaloAltoExtractor.preprocess(tree, _parser, _w);
    PaloAltoConfigurationBuilder cb = new PaloAltoConfigurationBuilder(_parser, _text, _w);
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
