package org.batfish.grammar;

import static org.hamcrest.Matchers.containsString;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.batfish.common.BatfishException;
import org.batfish.grammar.recovery.RecoveryLexer;
import org.batfish.grammar.recovery.RecoveryParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test of {@link BatfishParseTreeWalker} */
public class BatfishParseTreeWalkerTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static final Integer BASE_LINE_NUMBER = 1;
  private static final Integer MAPPED_LINE_NUMBER = 9;

  @Test
  public void testExceptionNoLineMapping() {
    TestParser parser = new TestParser();
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker(parser);
    ParserRuleContext ctx = new ParserRuleContext();
    ctx.start = new TestToken();

    _thrown.expect(BatfishParseException.class);
    _thrown.expectMessage(containsString(String.format("line %d:", BASE_LINE_NUMBER)));
    swimmer.throwException(new BatfishException("message"), ctx);
  }

  @Test
  public void testExceptionWithLineMapping() {
    TestParser parser = new TestParser(ImmutableMap.of(BASE_LINE_NUMBER, MAPPED_LINE_NUMBER));
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker(parser);
    ParserRuleContext ctx = new ParserRuleContext();
    ctx.start = new TestToken();

    _thrown.expect(BatfishParseException.class);
    _thrown.expectMessage(containsString(String.format("line %d:", MAPPED_LINE_NUMBER)));
    swimmer.throwException(new BatfishException("message"), ctx);
  }

  private final class TestParser extends BatfishCombinedParser<RecoveryParser, RecoveryLexer> {
    Map<Integer, Integer> _lineMap;

    public TestParser(Map<Integer, Integer> lineMap) {
      super(
          RecoveryParser.class,
          RecoveryLexer.class,
          "",
          new MockGrammarSettings(false, 0, 0, 0, false, false, true, true),
          new BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory(
              RecoveryLexer.NEWLINE, "\n"),
          BatfishLexerRecoveryStrategy.WHITESPACE_AND_NEWLINES);
      _lineMap = lineMap;
    }

    public TestParser() {
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
      int line = t.getLine();
      return (_lineMap == null) ? line : _lineMap.get(line);
    }
  }

  private final class TestToken implements Token {
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
      return BASE_LINE_NUMBER;
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
      return 0;
    }

    @Override
    public int getStopIndex() {
      return 0;
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
}
