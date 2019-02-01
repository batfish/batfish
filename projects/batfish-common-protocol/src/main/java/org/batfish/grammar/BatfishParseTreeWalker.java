package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;
import org.batfish.common.BatfishException;

/** Custom ParseTreeWalker that adds some additional context when exceptions occur */
public class BatfishParseTreeWalker extends ParseTreeWalker {

  public BatfishParseTreeWalker() {
    super();
  }

  @Override
  protected void enterRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    try {
      listener.enterEveryRule(ctx);
      ctx.enterRule(listener);
    } catch (Exception e) {
      throw new BatfishException(
          String.format("Exception walking line '%s': %s", ctx.getText(), e.getMessage()),
          e.getCause());
    }
  }

  @Override
  protected void exitRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    try {
      ctx.exitRule(listener);
      listener.exitEveryRule(ctx);
    } catch (Exception e) {
      throw new BatfishException(
          String.format("Exception walking line '%s': %s", ctx.getText(), e.getMessage()),
          e.getCause());
    }
  }
}
