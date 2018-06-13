package org.batfish.grammar.palo_alto;

import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_hostnameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_ntp_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsd_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsn_ntp_server_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_ethernetContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_commentContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_link_statusContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_mtuContext;
import org.batfish.representation.palo_alto.Interface;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener {

  private PaloAltoConfiguration _configuration;

  private Interface _currentInterface;

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

  @SuppressWarnings("unused")
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

  /** Return token text with enclosing quotes removed, if applicable */
  private String getText(ParserRuleContext ctx) {
    return unquote(ctx.getText());
  }

  private String unquote(String text) {
    if (text.length() < 2) {
      return text;
    }
    char leading = text.charAt(0);
    char trailing = text.charAt(text.length() - 1);
    if (leading == '\'' || leading == '"') {
      if (leading == trailing) {
        return text.substring(1, text.length() - 1);
      } else {
        _w.redFlag("Improperly-quoted string: " + text);
      }
    }
    return text;
  }

  @Override
  public void enterPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    _configuration = new PaloAltoConfiguration(_unimplementedFeatures);
  }

  @Override
  public void exitSds_hostname(Sds_hostnameContext ctx) {
    _configuration.setHostname(ctx.name.getText());
  }

  @Override
  public void enterSds_ntp_servers(Sds_ntp_serversContext ctx) {
    _currentNtpServerPrimary = ctx.PRIMARY_NTP_SERVER() != null;
  }

  @Override
  public void exitSdsn_ntp_server_address(Sdsn_ntp_server_addressContext ctx) {
    if (_currentNtpServerPrimary) {
      _configuration.setNtpServerPrimary(ctx.address.getText());
    } else {
      _configuration.setNtpServerSecondary(ctx.address.getText());
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

  @Override
  public void enterSni_ethernet(Sni_ethernetContext ctx) {
    String name = ctx.name.getText();
    _currentInterface = _configuration.getInterfaces().computeIfAbsent(name, Interface::new);
  }

  @Override
  public void exitSni_ethernet(Sni_ethernetContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSnie_comment(Snie_commentContext ctx) {
    _currentInterface.setComment(getText(ctx.text));
  }

  @Override
  public void exitSnie_link_status(Snie_link_statusContext ctx) {
    _currentInterface.setActive((ctx.DOWN() == null));
  }

  @Override
  public void exitSniel3_ip(Sniel3_ipContext ctx) {
    InterfaceAddress address = new InterfaceAddress(ctx.address.getText());
    _currentInterface.setAddress(address);
    _currentInterface.getAllAddresses().add(address);
  }

  @Override
  public void exitSniel3_mtu(Sniel3_mtuContext ctx) {
    _currentInterface.setMtu(Integer.parseInt(ctx.mtu.getText()));
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
