package org.batfish.vendor.huawei.grammar;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Huawei_configurationContext;

public class HuaweiCombinedParser extends BatfishCombinedParser<HuaweiParser, HuaweiLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(HuaweiLexer.NEWLINE, "\n");

  public HuaweiCombinedParser(String input, GrammarSettings settings) {
    super(
        HuaweiParser.class,
        HuaweiLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Huawei_configurationContext parse() {
    return _parser.huawei_configuration();
  }
}
