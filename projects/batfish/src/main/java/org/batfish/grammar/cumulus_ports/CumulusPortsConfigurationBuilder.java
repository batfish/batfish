package org.batfish.grammar.cumulus_ports;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.BreakoutContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.DisabledContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Port_definitionContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.SpeedContext;
import org.batfish.representation.cumulus.CumulusConcatenatedConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;

/**
 * Populates a {@link CumulusConcatenatedConfiguration} from the data in a {@link
 * org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser cumulus ports file parse tree}.
 */
public class CumulusPortsConfigurationBuilder extends CumulusPortsParserBaseListener {
  private static final Pattern BREAKOUT_PATTERN = Pattern.compile("(\\d+)x(\\d+)G");
  private static final Pattern SPEED_PATTERN = Pattern.compile("^(\\d+)G$");

  private final CumulusConcatenatedConfiguration _config;
  private final CumulusPortsCombinedParser _parser;
  private final Warnings _w;

  private Integer _currentPort;

  public CumulusPortsConfigurationBuilder(
      CumulusConcatenatedConfiguration config, CumulusPortsCombinedParser parser, Warnings w) {
    _config = config;
    _parser = parser;
    _w = w;
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
      _config.getPortsConfiguration().setSpeed(subIfaceName, subIfaceSpeedMbps);

      _config.referenceStructure(
          CumulusStructureType.INTERFACE,
          subIfaceName,
          CumulusStructureUsage.PORT_BREAKOUT,
          ctx.getStart().getLine());
    }
  }

  @Override
  public void exitDisabled(DisabledContext ctx) {
    checkState(_currentPort != null);
    String ifaceName = String.format("swp%d", _currentPort);
    _config.getPortsConfiguration().setDisabled(ifaceName, true);

    _config.referenceStructure(
        CumulusStructureType.INTERFACE,
        ifaceName,
        CumulusStructureUsage.PORT_DISABLED,
        ctx.getStart().getLine());
  }

  @Override
  public void exitSpeed(SpeedContext ctx) {
    checkState(_currentPort != null);
    String ifaceName = String.format("swp%d", _currentPort);
    Matcher matcher = SPEED_PATTERN.matcher(ctx.getText());
    checkState(matcher.matches());
    int speedGbps = parseInt(matcher.group(1));
    // setSpeed expects Mbps
    _config.getPortsConfiguration().setSpeed(ifaceName, speedGbps * 1000);

    _config.referenceStructure(
        CumulusStructureType.INTERFACE,
        ifaceName,
        CumulusStructureUsage.PORT_SPEED,
        ctx.getStart().getLine());
  }

  public CumulusPortsCombinedParser getParser() {
    return _parser;
  }
}
