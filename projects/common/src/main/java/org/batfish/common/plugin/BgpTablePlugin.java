package org.batfish.common.plugin;

import org.batfish.common.Warnings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BgpTableExtractor;
import org.batfish.grammar.BgpTableFormat;
import org.batfish.grammar.GrammarSettings;

public abstract class BgpTablePlugin extends BatfishPlugin implements IBgpTablePlugin {

  private final BgpTableFormat _format;

  public BgpTablePlugin(BgpTableFormat format) {
    _format = format;
  }

  @Override
  protected void batfishPluginInitialize() {
    bgpTablePluginInitialize();
    _batfish.registerBgpTablePlugin(_format, this);
  }

  protected abstract void bgpTablePluginInitialize();

  public abstract BgpTableExtractor extractor(
      String hostname,
      String fileText,
      BatfishCombinedParser<?, ?> combinedParser,
      Warnings warnings);

  public abstract BatfishCombinedParser<?, ?> parser(String fileText, GrammarSettings settings);
}
