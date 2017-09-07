package org.batfish.grammar.netscreen;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.config.Settings;
import org.batfish.grammar.BatfishCombinedParser;

public class NetscreenCombinedParser
    extends BatfishCombinedParser<NetscreenParser, NetscreenLexer> {

  public NetscreenCombinedParser(String input, Settings settings) {
    super(NetscreenParser.class, NetscreenLexer.class, input, settings);
  }

  @Override
  public ParserRuleContext parse() {
    return _parser.netscreen_configuration();
  }
}
