package org.batfish.vendor.rtx.grammar;

import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.batfish.grammar.BatfishLexer;

/** Yamaha RTX lexer base class providing additional functionality on top of {@link BatfishLexer}. */
@ParametersAreNonnullByDefault
public abstract class RtxBaseLexer extends BatfishLexer {

  private int _lastTokenType = -1;
  private int _secondToLastTokenType = -1;

  public RtxBaseLexer(CharStream input) {
    super(input);
  }

  @Override
  public final void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
      _secondToLastTokenType = _lastTokenType;
      _lastTokenType = token.getType();
    }
  }

  /**
   * Type of the last token emitted on the default channel, or {@code -1} if no token has been
   * emitted yet. Used to disambiguate keywords that mean different things depending on the command
   * they appear in, e.g. {@code filter} in {@code ip filter} versus {@code ip <if> secure filter}.
   */
  protected final int lastTokenType() {
    return _lastTokenType;
  }

  /**
   * Type of the token emitted before {@link #lastTokenType}, or {@code -1} if fewer than two
   * tokens have been emitted. Used to tell a line-initial {@code nat descriptor} (which defines a
   * descriptor) from the {@code nat descriptor} in {@code ip <if> nat descriptor <n>}.
   */
  protected final int secondToLastTokenType() {
    return _secondToLastTokenType;
  }
}
