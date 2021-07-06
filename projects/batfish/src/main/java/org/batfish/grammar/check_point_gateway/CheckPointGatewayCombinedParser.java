package org.batfish.grammar.check_point_gateway;

import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.GrammarSettings;
import org.batfish.grammar.check_point_gateway.CheckPointGatewayParser.Check_point_gateway_configurationContext;

public class CheckPointGatewayCombinedParser
    extends BatfishCombinedParser<CheckPointGatewayParser, CheckPointGatewayLexer> {

  private static final BatfishANTLRErrorStrategyFactory NEWLINE_BASED_RECOVERY =
      new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
          CheckPointGatewayLexer.NEWLINE, "\n");

  public CheckPointGatewayCombinedParser(String input, GrammarSettings settings) {
    super(
        CheckPointGatewayParser.class,
        CheckPointGatewayLexer.class,
        input,
        settings,
        NEWLINE_BASED_RECOVERY,
        BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
  }

  @Override
  public Check_point_gateway_configurationContext parse() {
    return _parser.check_point_gateway_configuration();
  }
}
