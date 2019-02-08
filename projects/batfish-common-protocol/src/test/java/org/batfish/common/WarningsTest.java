package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.batfish.common.Warnings.ParseExceptionContext;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.recovery.RecoveryLexer;
import org.batfish.grammar.recovery.RecoveryParser;
import org.junit.Test;

public class WarningsTest {
  private static final Integer LINE_NUMBER = 1234;
  private static final String LINE_TEXT = "tokenText";

  private final class TestParser extends BatfishCombinedParser<RecoveryParser, RecoveryLexer> {
    TestParser() {
      super(
          RecoveryParser.class,
          RecoveryLexer.class,
          "",
          new MockGrammarSettings(false, 0, 0, 0, false, false, true, true),
          new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
              RecoveryLexer.NEWLINE, "\n"),
          BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
    }

    @Override
    public ParserRuleContext parse() {
      return null;
    }

    @Override
    public int getLine(@Nonnull Token t) {
      return LINE_NUMBER;
    }
  }

  private final class TestParserRuleContext extends ParserRuleContext {
    @Override
    public Token getStart() {
      return null;
    }

    @Override
    public String getText() {
      return LINE_TEXT;
    }
  }

  @Test
  public void testParseExceptionContextConstructionFromParserRuleContext() {
    ParseExceptionContext pec =
        new ParseExceptionContext(new TestParserRuleContext(), new TestParser());

    // Confirm line number and text are correctly extracted from rule context and parser
    assertThat(pec.getLineNumber(), equalTo(LINE_NUMBER));
    assertThat(pec.getLineContent(), equalTo(LINE_TEXT));
  }

  @Test
  public void testParseExceptionContextSerialization() throws IOException {
    ParseExceptionContext pec = new ParseExceptionContext("content", 1);
    String serialzed = BatfishObjectMapper.writeString(pec);
    ParseExceptionContext deserialized = BatfishObjectMapper.mapper().readValue(serialzed, ParseExceptionContext.class);

    assertThat(pec.getLineContent(), equalTo(deserialized.getLineContent()));
    assertThat(pec.getLineNumber(), equalTo(deserialized.getLineNumber()));
  }
}
