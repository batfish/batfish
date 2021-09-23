package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the condition that the packet protocol is TCP. */
public final class TcpAstNode extends BooleanExprAstNode {

  public static @Nonnull TcpAstNode instance() {
    return INSTANCE;
  }

  private static final TcpAstNode INSTANCE = new TcpAstNode();

  private TcpAstNode() {}
}
