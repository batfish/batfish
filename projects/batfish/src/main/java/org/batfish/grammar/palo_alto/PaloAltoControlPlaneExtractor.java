package org.batfish.grammar.palo_alto;

import java.util.Set;
import java.util.TreeSet;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class PaloAltoControlPlaneExtractor implements ControlPlaneExtractor {

  private PaloAltoConfiguration _configuration;

  private final PaloAltoCombinedParser _parser;

  private final String _text;

  private final Set<String> _unimplementedFeatures;

  private final boolean _unrecognizedAsRedFlag;

  private final Warnings _w;

  public PaloAltoControlPlaneExtractor(
      String fileText,
      PaloAltoCombinedParser combinedParser,
      Warnings warnings,
      boolean unrecognizedAsRedFlag) {
    _text = fileText;
    _unimplementedFeatures = new TreeSet<>();
    _unrecognizedAsRedFlag = unrecognizedAsRedFlag;
    _parser = combinedParser;
    _w = warnings;
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    PaloAltoConfigurationBuilder cb =
        new PaloAltoConfigurationBuilder(
            _parser, _text, _w, _unimplementedFeatures, _unrecognizedAsRedFlag);
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(cb, tree);
    _configuration = cb.getConfiguration();
  }
}
