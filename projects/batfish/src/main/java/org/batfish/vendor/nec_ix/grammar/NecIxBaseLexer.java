package org.batfish.vendor.nec_ix.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** NEC IX lexer base class providing additional functionality on top of {@link BatfishLexer}. */
@ParametersAreNonnullByDefault
public abstract class NecIxBaseLexer extends BatfishLexer {

  private int _lastTokenType = -1;

  public NecIxBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _lastTokenType = token.getType();
    }
  }

  /**
   * Type of the last token emitted on the default channel, or {@code -1} if no token has been
   * emitted yet. Lines emitted on the hidden channel (comments, banners, and prompts) are ignored.
   */
  protected final int lastTokenType() {
    return _lastTokenType;
  }
}
