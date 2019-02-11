package org.batfish.grammar;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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
import org.junit.Test;

public final class BatfishParseTreeWalkerTest {
  private static final class TestParseTreeListener implements ParseTreeListener {
    @Override
    public void visitTerminal(TerminalNode terminalNode) {}

    @Override
    public void visitErrorNode(ErrorNode errorNode) {}

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {}

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {}
  }

  private static final class TestRuleNode implements RuleNode {

    private final String _text;

    private final RuleContext _ruleContext;

    TestRuleNode(String text) {
      super();
      _text = text;
      _ruleContext = new TestParserRuleContext(_text);
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

  private static final class TestParserRuleContext extends ParserRuleContext {
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

  @Test
  public void enterRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();
    TestRuleNode testRuleNode = new TestRuleNode(LINE_TEXT);

    swimmer.enterRule(new TestParseTreeListener(), testRuleNode);
    // Make sure current context is updated correctly for enterRule
    assertThat(swimmer.getCurrentCtx(), equalTo(testRuleNode.getRuleContext()));
  }

  @Test
  public void exitRule() {
    BatfishParseTreeWalker swimmer = new BatfishParseTreeWalker();
    TestRuleNode testRuleNode = new TestRuleNode(LINE_TEXT);

    swimmer.exitRule(new TestParseTreeListener(), testRuleNode);
    // Make sure current context is updated correctly for exitRule
    assertThat(swimmer.getCurrentCtx(), equalTo(testRuleNode.getRuleContext()));
  }
}
