package org.batfish.grammar.palo_alto;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_deviceconfigContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdcs_hostnameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_lineContext;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener {

  private PaloAltoConfiguration _configuration;

  private PaloAltoCombinedParser _parser;

  private final String _text;

  private final Set<String> _unimplementedFeatures;

  private final boolean _unrecognizedAsRedFlag;

  private final Warnings _w;

  public PaloAltoConfigurationBuilder(
      PaloAltoCombinedParser parser,
      String text,
      Warnings warnings,
      Set<String> unimplementedFeatures,
      boolean unrecognizedAsRedFlag) {
    _parser = parser;
    _text = text;
    _configuration = new PaloAltoConfiguration(unimplementedFeatures);
    _unimplementedFeatures = unimplementedFeatures;
    _unrecognizedAsRedFlag = unrecognizedAsRedFlag;
    _w = warnings;
  }

  private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
    String typeName = type.getSimpleName();
    String txt = getFullText(ctx);
    return new BatfishException("Could not convert to " + typeName + ": " + txt);
  }

  private String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  @Override
  public void enterPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    _configuration = new PaloAltoConfiguration(_unimplementedFeatures);
    // String bogus = "basic-parsing";
    // _configuration.setHostname(bogus);
  }

  @Override
  public void exitSdcs_hostname(Sdcs_hostnameContext ctx) {
    _configuration.setHostname(ctx.name.getText());
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    // _configuration.setHostname("bogus1");
  }

  @Override
  public void exitS_deviceconfig(S_deviceconfigContext ctx) {
    // _configuration.setHostname("bogus2");
  }

  public PaloAltoConfiguration getConfiguration() {
    return _configuration;
  }

  /** ONLY OVERRIDE THIS IF GRAMMAR SUPPORTS NEWLINE-BASED RECOVERY */
  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    int line = token.getLine();
    String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
    if (_unrecognizedAsRedFlag) {
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
      _configuration.setUnrecognized(true);
    } else {
      _parser.getErrors().add(msg);
    }
  }
}
