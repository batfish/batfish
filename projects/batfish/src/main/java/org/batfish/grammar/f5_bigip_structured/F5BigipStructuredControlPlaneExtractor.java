package org.batfish.grammar.f5_bigip_structured;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Throwables;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class F5BigipStructuredControlPlaneExtractor implements ControlPlaneExtractor {

  private final F5BigipStructuredCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private F5BigipConfiguration _configuration;

  public F5BigipStructuredControlPlaneExtractor(
      String fileText, F5BigipStructuredCombinedParser combinedParser, Warnings warnings) {
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
    F5BigipStructuredConfigurationBuilder cb =
        new F5BigipStructuredConfigurationBuilder(_parser, _text, _w);
    BatfishParseTreeWalker walker = new BatfishParseTreeWalker();
    try {
      walker.walk(cb, tree);
    } catch (Exception e) {
      _w.setErrorDetails(
          new ErrorDetails(
              Throwables.getStackTraceAsString(firstNonNull(e.getCause(), e)),
              new ParseExceptionContext(walker.getCurrentCtx(), _parser)));
      throw e;
    }
    _configuration = cb.getConfiguration();
  }
}
