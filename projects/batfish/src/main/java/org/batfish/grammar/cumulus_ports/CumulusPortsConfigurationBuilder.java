package org.batfish.grammar.cumulus_ports;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.BreakoutContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.DisabledContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Port_definitionContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.SpeedContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Interface;

/**
 * Populates a {@link CumulusNcluConfiguration} from the data in a {@link
 * org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser cumulus ports file parse tree}.
 */
public class CumulusPortsConfigurationBuilder extends CumulusPortsParserBaseListener {
  private static final Pattern BREAKOUT_PATTERN = Pattern.compile("(\\d+)x(\\d+)G");
  private static final Pattern SPEED_PATTERN = Pattern.compile("^(\\d+)G$");

  private final CumulusNcluConfiguration _config;
  private final CumulusPortsCombinedParser _parser;
  private final Warnings _w;

  private Integer _currentPort;

  public CumulusPortsConfigurationBuilder(
      CumulusNcluConfiguration config, CumulusPortsCombinedParser parser, Warnings w) {
    _config = config;
    _parser = parser;
    _w = w;
  }

  @Nullable
  private Interface tryGetInterface(String ifaceName, ParserRuleContext ctx) {
    Interface iface = _config.getInterfaces().get(ifaceName);
    if (iface == null) {
      _w.addWarning(
          ctx, ctx.getText(), _parser, String.format("interface %s not found", ifaceName));
    }
    return iface;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _config.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }

  // Listener methods

  @Override
  public void enterPort_definition(Port_definitionContext ctx) {
    _currentPort = parseInt(ctx.PORT().getText());
  }

  @Override
  public void exitPort_definition(Port_definitionContext ctx) {
    _currentPort = null;
  }

  @Override
  public void exitBreakout(BreakoutContext ctx) {
    Matcher matcher = BREAKOUT_PATTERN.matcher(ctx.getText());
    checkState(matcher.matches());
    int numSubIfaces = parseInt(matcher.group(1));
    int subIfaceSpeedGbps = parseInt(matcher.group(2));
    int subIfaceSpeedMbps = subIfaceSpeedGbps * 1000;

    for (int i = 0; i < numSubIfaces; i++) {
      String subIfaceName = String.format("swp%ds%d", _currentPort, i);
      Interface iface = tryGetInterface(subIfaceName, ctx);
      if (iface != null) {
        iface.setSpeed(subIfaceSpeedMbps);
      }
    }
  }

  @Override
  public void exitDisabled(DisabledContext ctx) {
    checkState(_currentPort != null);
    String ifaceName = String.format("swp%d", _currentPort);
    Interface iface = tryGetInterface(ifaceName, ctx);
    if (iface != null) {
      iface.setDisabled(true);
    }
  }

  @Override
  public void exitSpeed(SpeedContext ctx) {
    checkState(_currentPort != null);
    String ifaceName = String.format("swp%d", _currentPort);
    Interface iface = tryGetInterface(ifaceName, ctx);
    if (iface != null) {
      Matcher matcher = SPEED_PATTERN.matcher(ctx.getText());
      checkState(matcher.matches());
      int speedGbps = parseInt(matcher.group(1));
      // setSpeed expects Mbps
      iface.setSpeed(speedGbps * 1000);
    }
  }
}
