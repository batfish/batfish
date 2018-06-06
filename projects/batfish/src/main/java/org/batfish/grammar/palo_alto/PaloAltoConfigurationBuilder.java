package org.batfish.grammar.palo_alto;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_ntp_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsd_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsn_ntp_server_addressContext;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener {

  private PaloAltoConfiguration _configuration;

  private boolean _currentNtpServerPrimary;

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
  }

  @Override
  public void enterSds_ntp_servers(Sds_ntp_serversContext ctx) {
    _currentNtpServerPrimary = ctx.PRIMARY_NTP_SERVER() != null;
  }

  @Override
  public void exitSdsn_ntp_server_address(Sdsn_ntp_server_addressContext ctx) {
    if (_currentNtpServerPrimary) {
      _configuration.setDnsServerPrimary(ctx.IP_ADDRESS().getText());
    } else {
      _configuration.setDnsServerSecondary(ctx.IP_ADDRESS().getText());
    }
  }

  @Override
  public void exitSdsd_servers(Sdsd_serversContext ctx) {
    if (ctx.primary_name != null) {
      _configuration.setDnsServerPrimary(ctx.primary_name.getText());
    } else if (ctx.secondary_name != null) {
      _configuration.setDnsServerSecondary(ctx.secondary_name.getText());
    }
  }

  public PaloAltoConfiguration getConfiguration() {
    return _configuration;
  }

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
