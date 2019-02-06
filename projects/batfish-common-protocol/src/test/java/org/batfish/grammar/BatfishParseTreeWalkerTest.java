package org.batfish.grammar;

import static org.hamcrest.Matchers.containsString;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
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

    private final String _text;

    TestRuleNode(String text) {
      super();
      _text = text;
    }

    @Override
    public RuleContext getRuleContext() {
      return new TestParserRuleContext(_text);
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

  private final class TestParserRuleContext extends ParserRuleContext {
    private final String _text;

    TestParserRuleContext(String text) {
      super();
      _text = text;
    }

    @Override
    public String getText() {
      return _text;
    }
  }

  private static final String LINE_TEXT = "Line text";

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void enterRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();

    // Make sure an exception in enterRule contains the context's text
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(LINE_TEXT));
    swimmer.enterRule(new TestParseTreeListener(), new TestRuleNode(LINE_TEXT));
  }

  @Test
  public void exitRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();

    // Make sure an exception in exitRule contains the context's text
    _thrown.expect(BatfishException.class);
    _thrown.expectMessage(containsString(LINE_TEXT));
    swimmer.exitRule(new TestParseTreeListener(), new TestRuleNode(LINE_TEXT));
  }
}
