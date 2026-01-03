package org.batfish.grammar;

import com.google.common.base.Throwables;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ErrorDetails.ParseExceptionContext;
import org.batfish.common.WillNotCommitException;

/** Custom ParseTreeWalker that adds some additional context when exceptions occur */
public class BatfishParseTreeWalker extends ParseTreeWalker {

  private final BatfishCombinedParser<?, ?> _parser;

  public BatfishParseTreeWalker(BatfishCombinedParser<?, ?> parser) {
    super();
    _parser = parser;
  }

  @Override
  protected void enterRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    try {
      listener.enterEveryRule(ctx);
      ctx.enterRule(listener);
    } catch (Exception e) {
      throw new BatfishParseException(
          String.format("Exception while walking parse tree: %s", e.getMessage()),
          e,
          new ErrorDetails(
              Throwables.getStackTraceAsString(e),
              new ParseExceptionContext(ctx, _parser, _parser.getInput())));
    }
  }

  @Override
  protected void exitRule(ParseTreeListener listener, RuleNode r) {
    ParserRuleContext ctx = (ParserRuleContext) r.getRuleContext();
    try {
      ctx.exitRule(listener);
      listener.exitEveryRule(ctx);
    } catch (WillNotCommitException e) {
      // Re-throw WillNotCommit to get the special parse status
      throw e;
    } catch (Exception e) {
      throw new BatfishParseException(
          String.format("Exception while walking parse tree: %s", e.getMessage()),
          e,
          new ErrorDetails(
              Throwables.getStackTraceAsString(e),
              new ParseExceptionContext(ctx, _parser, _parser.getInput())));
    }
  }
}
