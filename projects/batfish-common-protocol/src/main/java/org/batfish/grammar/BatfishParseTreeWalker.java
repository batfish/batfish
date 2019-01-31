package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;

public class BatfishParseTreeWalker extends ParseTreeWalker {

  BatfishCombinedParser<BatfishParser, BatfishLexer> _parser;

  public BatfishParseTreeWalker(BatfishCombinedParser<BatfishParser, BatfishLexer> parser) {
    super();
    _parser = parser;
  }

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
      throwException(e, ctx);
    }
  }

  @Override
  protected void exitRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    try {
      ctx.exitRule(listener);
      listener.exitEveryRule(ctx);
    } catch (Exception e) {
      throwException(e, ctx);
    }
  }

  private void throwException(Exception e, ParserRuleContext ctx) {
    int line = ctx.start.getLine();
    // Handle applying line translation if applicable (for flattned files)
    if (_parser != null) {
      line = _parser.getLine(ctx.start);
    }
    throw new BatfishParseException(e.getMessage(), e.getCause(), line, ctx.getText());
  }
}
