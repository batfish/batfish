package org.batfish.grammar;

import static org.hamcrest.Matchers.containsString;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.grammar.recovery.RecoveryLexer;
import org.batfish.grammar.recovery.RecoveryParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BatfishParseTreeWalkerTest {
  private final class TestParseTreeListener implements ParseTreeListener {
    @Override
    public void visitTerminal(TerminalNode terminalNode) {}

    @Override
    public void visitErrorNode(ErrorNode errorNode) {}

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
      throw new BatfishException("fail enterEveryRule");
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
      throw new BatfishException("fail exitEveryRule");
    }
  }

  private final class TestRuleNode implements RuleNode {

    private final RuleContext _ruleContext;

    TestRuleNode(RuleContext ruleContext) {
      super();
      _ruleContext = ruleContext;
    }

    @Override
    public RuleContext getRuleContext() {
      return _ruleContext;
    }

    @Override
    public ParseTree getParent() {
      return null;
    }

    @Override
    public ParseTree getChild(int i) {
      return null;
    }

    @Override
    public void setParent(RuleContext ruleContext) {}

    @Override
    public <T> T accept(ParseTreeVisitor<? extends T> parseTreeVisitor) {
      return null;
    }

    @Override
    public String getText() {
      return null;
    }

    @Override
    public String toStringTree(Parser parser) {
      return null;
    }

    @Override
    public Interval getSourceInterval() {
      return null;
    }

    @Override
    public Object getPayload() {
      return null;
    }

    @Override
    public int getChildCount() {
      return 0;
    }

    @Override
    public String toStringTree() {
      return null;
    }
  }

  private final class TestParser extends BatfishCombinedParser<RecoveryParser, RecoveryLexer> {
    Map<Integer, Integer> _lineMap;

    TestParser(Map<Integer, Integer> lineMap) {
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
      int line = t.getLine();
      return (_lineMap == null) ? line : _lineMap.get(line);
    }
  }

  private final class TestParserRuleContext extends ParserRuleContext {
    private final Token _start;

    private final String _text;

    TestParserRuleContext(String text, Token start) {
      super();
      _text = text;
      _start = start;
    }

    @Override
    public Token getStart() {
      return _start;
    }

    @Override
    public String getText() {
      return _text;
    }
  }

  private final class TestToken implements Token {

    private final int _line;

    TestToken(int line) {
      _line = line;
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
      return _line;
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

  private static final String LINE_TEXT = "Line text";

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testEnterRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();

    // Make sure an exception in enterRule contains the context's text
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(LINE_TEXT));
    swimmer.enterRule(
        new TestParseTreeListener(),
        new TestRuleNode(new TestParserRuleContext(LINE_TEXT, new TestToken(1))));
  }

  @Test
  public void testExitRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();

    // Make sure an exception in exitRule contains the context's text
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(LINE_TEXT));
    swimmer.exitRule(
        new TestParseTreeListener(),
        new TestRuleNode(new TestParserRuleContext(LINE_TEXT, new TestToken(1))));
  }

  @Test
  public void testThrowException() {
    int line = 1234;
    TestParser parser = new TestParser();
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker(parser);
    ParserRuleContext ctx = new ParserRuleContext();
    ctx.start = new TestToken(line);

    // Make sure thrown exception includes line number
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(String.format("line number %d", line)));
    swimmer.throwException(new BatfishException("inner exception"), ctx);
  }

  @Test
  public void testThrowExceptionWithLineMapping() {
    int flattenedLine = 1234;
    int originalLine = 5678;
    TestParser parser = new TestParser(ImmutableMap.of(flattenedLine, originalLine));
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker(parser);
    ParserRuleContext ctx = new ParserRuleContext();
    ctx.start = new TestToken(1234);

    // Make sure thrown exception includes original line number, not flattened line number
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(String.format("line number %d", originalLine)));
    swimmer.throwException(new BatfishException("inner exception"), ctx);
  }
}
