package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;

/** Custom ParseTreeWalker that adds some additional context when exceptions occur */
public class BatfishParseTreeWalker extends ParseTreeWalker {

  public BatfishParseTreeWalker() {
    super();
  }

  ParserRuleContext _currentCtx;

  @Override
  protected void enterRule(ParseTreeListener listener, RuleNode r) {
    _currentCtx = (ParserRuleContext) r.getRuleContext();
    listener.enterEveryRule(_currentCtx);
    _currentCtx.enterRule(listener);
  }

  @Override
  protected void exitRule(ParseTreeListener listener, RuleNode r) {
    _currentCtx = (ParserRuleContext) r.getRuleContext();
    _currentCtx.exitRule(listener);
    listener.exitEveryRule(_currentCtx);
  }

  public ParserRuleContext getCurrentCtx() {
    return _currentCtx;
  }
}
