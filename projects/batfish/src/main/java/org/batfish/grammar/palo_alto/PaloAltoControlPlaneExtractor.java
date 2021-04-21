package org.batfish.grammar.palo_alto;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.ImplementedRules;
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
  public Set<String> implementedRuleNames() {
    // TODO: do we need to add things like InsertDeleteApplicator? I think not, since those don't
    // make it to the final output.
    return ImplementedRules.getImplementedRules(PaloAltoConfigurationBuilder.class);
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
