package org.batfish.grammar.f5_bigip_imish;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.F5_bigip_imish_configurationContext;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;

public class F5BigipImishConfigurationBuilder extends F5BigipImishParserBaseListener {

  private final @Nonnull F5BigipConfiguration _c;
  private final @Nonnull F5BigipImishCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  public F5BigipImishConfigurationBuilder(
      F5BigipImishCombinedParser parser,
      String text,
      Warnings w,
      F5BigipConfiguration configuration) {
    _parser = parser;
    _text = text;
    _w = w;
    _c = configuration;
  }

  @Override
  public void enterF5_bigip_imish_configuration(F5_bigip_imish_configurationContext ctx) {
    _c.setImish(true);
  }
}
