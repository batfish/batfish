package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.recovery.RecoveryLexer;
import org.batfish.grammar.recovery.RecoveryParser;
import org.junit.Test;

public class ErrorDetailsTest {
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
  public void testErrorDetailsSerialization() throws IOException {
    ParseExceptionContext context = new ParseExceptionContext("content", 1, "context");
    ErrorDetails error = new ErrorDetails("message", context);
    String serialized = BatfishObjectMapper.writeString(error);
    ErrorDetails deserialized =
        BatfishObjectMapper.mapper().readValue(serialized, ErrorDetails.class);

    // Confirm serialization and deserialization produce output equal to input
    assertThat(error, equalTo(deserialized));
  }

  @Test
  public void testParseExceptionContextConstructionFromParserRuleContext() {
    ParseExceptionContext context =
        new ParseExceptionContext(new TestParserRuleContext(), new TestParser());

    // Confirm line number and text are correctly extracted from rule context and parser
    assertThat(context.getLineNumber(), equalTo(LINE_NUMBER));
    assertThat(context.getLineContent(), equalTo(LINE_TEXT));
  }

  @Test
  public void testParseExceptionContextSerialization() throws IOException {
    ParseExceptionContext context = new ParseExceptionContext("content", 1, "context");
    String serialized = BatfishObjectMapper.writeString(context);
    ParseExceptionContext deserialized =
        BatfishObjectMapper.mapper().readValue(serialized, ParseExceptionContext.class);

    // Confirm serialization and deserialization produce output equal to input
    assertThat(context, equalTo(deserialized));
  }
}
