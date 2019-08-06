package org.batfish.grammar.cumulus_ports;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;

/**
 * Populates a {@link CumulusNcluConfiguration} from the data in a {@link
 * org.batfish.grammar.cumulus_ports.CumulusPortsCombinedParser cumulus ports file parse tree}.
 */
public class CumulusPortsConfigurationBuilder extends CumulusPortsParserBaseListener {
  private final CumulusNcluConfiguration _config;
  private final Warnings _w;

  public CumulusPortsConfigurationBuilder(CumulusNcluConfiguration config, Warnings w) {
    _config = config;
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
}
