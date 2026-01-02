package org.batfish.grammar;

import javax.annotation.Nullable;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public abstract class BatfishParser extends Parser {

  public BatfishParser(TokenStream input) {
    super(input);
  }

  @Override
  public Token consume() {
    Token o = getCurrentToken();
    if (o.getType() != EOF) {
      getInputStream().consume();
      _lastConsumedToken = o.getType();
    }
    boolean hasListener = _parseListeners != null && !_parseListeners.isEmpty();
    if ((_buildParseTrees || hasListener) && !_errHandler.inErrorRecoveryMode(this)) {
      TerminalNode node = _ctx.addChild(createTerminalNode(_ctx, o));
      if (_parseListeners != null) {
        for (ParseTreeListener listener : _parseListeners) {
          listener.visitTerminal(node);
        }
      }
    }
    return o;
  }

  /**
   * Returns the ID of the last consumed token. This is needed by recovery to tell whether current
   * token is the first word on the line (i.e., last token was a NEWLINE or equivalent).
   */
  public int getLastConsumedToken() {
    return _lastConsumedToken;
  }

  public @Nullable String getStateInfo() {
    return null;
  }

  public void initErrorListener(BatfishCombinedParser<?, ?> parser) {
    BatfishParserErrorListener errorListener =
        new BatfishParserErrorListener(getClass().getSimpleName(), parser);
    removeErrorListeners();
    addErrorListener(errorListener);
    parser.setParserErrorListener(errorListener);
  }

  public void createErrorNodeLine() {
    ((BatfishANTLRErrorStrategy) _errHandler).recoverInCurrentNode(this);
  }

  private int _lastConsumedToken;
}
