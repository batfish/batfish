package org.batfish.grammar.cumulus_frr;

import org.batfish.common.Warnings;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_vrfContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.Vrf;

public class CumulusFrrConfigurationBuilder extends CumulusFrrParserBaseListener {
  private final Warnings _w;
  private CumulusNcluConfiguration _c;

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
    _c.getVrfs().computeIfAbsent(name, k -> new Vrf(name));
  }
}
