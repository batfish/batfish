package org.batfish.grammar.cumulus_ports;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Integer.parseInt;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Port_configContext;
import org.batfish.grammar.cumulus_ports.CumulusPortsParser.Port_definitionContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Interface;

/**
 * Populates a {@link CumulusNcluConfiguration} from the data in a {@link
 * org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser cumulus ports file parse tree}.
 */
public class CumulusPortsConfigurationBuilder extends CumulusPortsParserBaseListener {
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

  @Override
  public void enterPort_definition(Port_definitionContext ctx) {
    _currentPort = parseInt(ctx.PORT().getText());
  }

  @Override
  public void exitPort_definition(Port_definitionContext ctx) {
    _currentPort = null;
  }

  @Override
  public void exitPort_config(Port_configContext ctx) {
    checkState(_currentPort != null);

    if (ctx.DISABLED() != null) {
      String ifaceName = String.format("swp%d", _currentPort);
      Interface iface = tryGetInterface(ifaceName, ctx);
      if (iface != null) {
        iface.setDisabled(true);
      }
    }
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
}
