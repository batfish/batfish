package org.batfish.grammar.cisco_nxos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cisco_nxos_configurationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_hostnameContext;
import org.batfish.representation.cisco_nxos.CiscoNxosConfiguration;
import org.batfish.vendor.VendorConfiguration;

@ParametersAreNonnullByDefault
public final class CiscoNxosControlPlaneExtractor extends CiscoNxosParserBaseListener
    implements ControlPlaneExtractor {

  private @Nullable CiscoNxosConfiguration _configuration;
  private final CiscoNxosCombinedParser _parser;
  private @Nonnull final String _text;
  private @Nonnull final Warnings _w;

  public CiscoNxosControlPlaneExtractor(
      String text, CiscoNxosCombinedParser parser, Warnings warnings) {
    _text = text;
    _parser = parser;
    _w = warnings;
  }

  @Override
  public void enterCisco_nxos_configuration(Cisco_nxos_configurationContext ctx) {
    _configuration = new CiscoNxosConfiguration();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.hostname.getText());
  }

  @Override
  public @Nullable VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(this, tree);
  }
}
