package org.batfish.grammar.cumulus_frr;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_vrfContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Vrf;

public class CumulusFrrConfigurationBuilder extends CumulusFrrParserBaseListener {
  private final Warnings _w;
  private CumulusNcluConfiguration _c;
  private @Nullable Vrf _currentVrf;

  CumulusFrrConfigurationBuilder(CumulusNcluConfiguration configuration, Warnings w) {
    _w = w;
    _c = configuration;
  }

  CumulusNcluConfiguration getVendorConfiguration() {
    return _c;
  }

  @Override
  public void enterS_vrf(S_vrfContext ctx) {
    String name = ctx.name.getText();
    _currentVrf = new Vrf(name);
    _c.getVrfs().put(name, _currentVrf);
  }

  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    _currentVrf = null;
  }
}
