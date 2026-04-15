package org.batfish.grammar;

import com.google.common.base.Throwables;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
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

  /**
   * Override walk to access {@link ParserRuleContext#children} directly instead of going through
   * {@link ParserRuleContext#getChild(int)}, avoiding virtual dispatch and null/bounds checks per
   * call. Logic otherwise identical to {@link ParseTreeWalker#walk}.
   */
  @Override
  public void walk(ParseTreeListener listener, ParseTree t) {
    if (t instanceof ErrorNode) {
      listener.visitErrorNode((ErrorNode) t);
      return;
    } else if (t instanceof TerminalNode) {
      listener.visitTerminal((TerminalNode) t);
      return;
    }
    RuleNode r = (RuleNode) t;
    enterRule(listener, r);
    List<ParseTree> children = ((ParserRuleContext) r).children;
    if (children != null) {
      int n = children.size();
      for (int i = 0; i < n; i++) {
        walk(listener, children.get(i));
      }
    }
    exitRule(listener, r);
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
