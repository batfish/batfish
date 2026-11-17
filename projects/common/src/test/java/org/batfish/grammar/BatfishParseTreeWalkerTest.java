package org.batfish.grammar;

import static org.batfish.common.matchers.ThrowableMatchers.hasStackTrace;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.grammar.recovery.RecoveryCombinedParser;
import org.batfish.grammar.recovery.RecoveryParserBaseListener;
import org.junit.jupiter.api.Test;

@ParametersAreNonnullByDefault
final class BatfishParseTreeWalkerTest {

  private static final class TestEnterThrowingParseTreeListener extends RecoveryParserBaseListener {
    private static void throwEnter() {
      throw new BatfishException("fail enterEveryRule");
    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
      throwEnter();
    }
  }

  private static final class TestExitThrowingParseTreeListener extends RecoveryParserBaseListener {
    private static void throwExit() {
      throw new BatfishException("fail exitEveryRule");
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
      throwExit();
    }
  }

  private static final GrammarSettings SETTINGS =
      MockGrammarSettings.builder()
          .setDisableUnrecognized(true)
          .setThrowOnLexerError(true)
          .setThrowOnParserError(true)
          .build();

  private static final String TEXT = "simple\n";

  @Test
  void enterRule() {
    BatfishCombinedParser<?, ?> parser = new RecoveryCombinedParser(TEXT, SETTINGS);

    // Make sure an exception in enterRule contains the name of throwing function
    Throwable exception =
        assertThrows(
            BatfishParseException.class,
            () ->
                new BatfishParseTreeWalker(parser)
                    .walk(new TestEnterThrowingParseTreeListener(), parser.parse()));
    assertThat(exception, hasStackTrace(containsString("throwEnter")));
  }

  @Test
  void exitRule() {
    BatfishCombinedParser<?, ?> parser = new RecoveryCombinedParser(TEXT, SETTINGS);

    // Make sure an exception in exitRule contains the name of throwing function
    Throwable exception =
        assertThrows(
            BatfishParseException.class,
            () ->
                new BatfishParseTreeWalker(parser)
                    .walk(new TestExitThrowingParseTreeListener(), parser.parse()));
    assertThat(exception, hasStackTrace(containsString("throwExit")));
  }
}
