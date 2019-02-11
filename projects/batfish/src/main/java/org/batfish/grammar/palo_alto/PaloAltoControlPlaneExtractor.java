package org.batfish.grammar.palo_alto;

import com.google.common.base.Throwables;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
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
  public void processParseTree(ParserRuleContext tree) {
    PaloAltoConfigurationBuilder cb = new PaloAltoConfigurationBuilder(_parser, _text, _w);
    BatfishParseTreeWalker walker = new BatfishParseTreeWalker();
    try {
      walker.walk(cb, tree);
    } catch (Exception e) {
      _w.setErrorDetails(
          new ErrorDetails(
              Throwables.getStackTraceAsString(e),
              new ParseExceptionContext(walker.getCurrentCtx(), _parser, _text)));
      throw e;
    }
    _configuration = cb.getConfiguration();
  }
}
