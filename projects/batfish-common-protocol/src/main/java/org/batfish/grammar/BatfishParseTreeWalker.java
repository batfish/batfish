package org.batfish.grammar;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;
import org.batfish.common.BatfishException;

/** Custom ParseTreeWalker that adds some additional context when exceptions occur */
public class BatfishParseTreeWalker extends ParseTreeWalker {

  @Nullable
  private final BatfishCombinedParser<? extends BatfishParser, ? extends BatfishLexer> _parser;

  public BatfishParseTreeWalker(
      @Nullable BatfishCombinedParser<? extends BatfishParser, ? extends BatfishLexer> parser) {
    super();
    _parser = parser;
  }

  public BatfishParseTreeWalker() {
    super();
    _parser = null;
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

  @VisibleForTesting
  void throwException(Exception e, ParserRuleContext ctx) {
    int line = ctx.getStart().getLine();
    // Handle applying line translation if applicable (for flattened or otherwise modified files)
    if (_parser != null) {
      line = _parser.getLine(ctx.getStart());
    }
    throw new BatfishException(
        String.format(
            "Exception walking line number %d ('%s'): %s", line, ctx.getText(), e.getMessage()),
        e.getCause());
  }
}
