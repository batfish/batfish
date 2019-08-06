package org.batfish.grammar.cumulus_interfaces;

import com.google.common.annotations.VisibleForTesting;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_autoContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_ifaceContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus_interfaces.Interfaces;

/**
 * Populates an {@link Interfaces} from a parse tree from {@link
 * org.batfish.grammar.cumulus_interfaces.CumulusInterfacesCombinedParser}.
 */
public final class CumulusInterfacesConfigurationBuilder
    extends CumulusInterfacesParserBaseListener {
  private final CumulusNcluConfiguration _config;
  private final Interfaces _interfaces = new Interfaces();
  private final Warnings _w;

  public CumulusInterfacesConfigurationBuilder(CumulusNcluConfiguration config, Warnings w) {
    _config = config;
    _w = w;
  }

  @VisibleForTesting
  Interfaces getInterfaces() {
    return _interfaces;
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
  public void exitCumulus_interfaces_configuration(Cumulus_interfaces_configurationContext ctxt) {
    // TODO migrate _interfaces into _config
  }

  @Override
  public void enterS_auto(S_autoContext ctx) {
    String name = ctx.interface_name().getText();
    _interfaces.setAuto(name);
  }

  @Override
  public void enterS_iface(S_ifaceContext ctx) {
    String name = ctx.interface_name().getText();
    _interfaces.createOrGetInterface(name);
    _config.defineStructure(CumulusStructureType.INTERFACE, name, ctx.getStart().getLine());
  }
}
