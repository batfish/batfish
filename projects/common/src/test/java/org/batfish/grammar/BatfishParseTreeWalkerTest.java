package org.batfish.grammar;

import static org.batfish.common.matchers.ThrowableMatchers.hasStackTrace;
import static org.hamcrest.Matchers.containsString;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.grammar.recovery.RecoveryCombinedParser;
import org.batfish.grammar.recovery.RecoveryParserBaseListener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@ParametersAreNonnullByDefault
public final class BatfishParseTreeWalkerTest {

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

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void enterRule() {
    BatfishCombinedParser<?, ?> parser = new RecoveryCombinedParser(TEXT, SETTINGS);

    // Make sure an exception in enterRule contains the name of throwing function
    _thrown.expect(BatfishParseException.class);
    _thrown.expect(hasStackTrace(containsString("throwEnter")));
    new BatfishParseTreeWalker(parser)
        .walk(new TestEnterThrowingParseTreeListener(), parser.parse());
  }

  @Test
  public void exitRule() {
    BatfishCombinedParser<?, ?> parser = new RecoveryCombinedParser(TEXT, SETTINGS);

    // Make sure an exception in exitRule contains the name of throwing function
    _thrown.expect(BatfishParseException.class);
    _thrown.expect(hasStackTrace(containsString("throwExit")));
    new BatfishParseTreeWalker(parser)
        .walk(new TestExitThrowingParseTreeListener(), parser.parse());
  }
}
