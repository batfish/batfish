package org.batfish.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.grammar.BatfishANTLRErrorStrategy;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishLexerRecoveryStrategy;
import org.batfish.grammar.MockGrammarSettings;
import org.batfish.grammar.recovery.RecoveryLexer;
import org.batfish.grammar.recovery.RecoveryParser;
import org.junit.Test;

@ParametersAreNonnullByDefault
public final class ErrorDetailsTest {

  private static final Integer LINE_NUMBER = 1234;
  private static final String LINE_TEXT = "tokenText";

  private final class TestParser extends BatfishCombinedParser<RecoveryParser, RecoveryLexer> {
    TestParser() {
      super(
          RecoveryParser.class,
          RecoveryLexer.class,
          "",
          MockGrammarSettings.builder()
              .setThrowOnLexerError(true)
              .setThrowOnParserError(true)
              .build(),
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

    private final Token _token;

    public TestParserRuleContext(Token token) {
      _token = token;
    }

    @Override
    public Token getStart() {
      return _token;
    }

    @Override
    public Token getStop() {
      return _token;
    }

    @Override
    public String getText() {
      return LINE_TEXT;
    }
  }

  private final class TestToken implements Token {

    private final int _startIndex;

    private final int _stopIndex;

    public TestToken(int startIndex, int stopIndex) {
      _startIndex = startIndex;
      _stopIndex = stopIndex;
    }

    @Override
    public String getText() {
      return null;
    }

    @Override
    public int getType() {
      return 0;
    }

    @Override
    public int getLine() {
      return 0;
    }

    @Override
    public int getCharPositionInLine() {
      return 0;
    }

    @Override
    public int getChannel() {
      return 0;
    }

    @Override
    public int getTokenIndex() {
      return 0;
    }

    @Override
    public int getStartIndex() {
      return _startIndex;
    }

    @Override
    public int getStopIndex() {
      return _stopIndex;
    }

    @Override
    public TokenSource getTokenSource() {
      return null;
    }

    @Override
    public CharStream getInputStream() {
      return null;
    }
  }

  @Test
  public void testErrorDetailsSerialization() throws IOException {
    ParseExceptionContext context = new ParseExceptionContext("content", 1, "context");
    ErrorDetails error = new ErrorDetails("message", context);
    String serialized = BatfishObjectMapper.writeString(error);
    ErrorDetails deserialized =
        BatfishObjectMapper.mapper().readValue(serialized, ErrorDetails.class);

    // Confirm serialization and deserialization produce output equal to original object
    assertThat(error, equalTo(deserialized));
  }

  @Test
  public void testParseExceptionContextConstructionFromParserRuleContext() {
    ParseExceptionContext context =
        new ParseExceptionContext(
            new TestParserRuleContext(new TestToken(0, LINE_TEXT.length() - 1)),
            new TestParser(),
            LINE_TEXT);

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

    // Confirm serialization and deserialization produce output equal to original object
    assertThat(context, equalTo(deserialized));
  }
}
