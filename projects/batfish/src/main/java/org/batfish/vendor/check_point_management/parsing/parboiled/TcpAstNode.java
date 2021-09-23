package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the condition that the packet protocol is TCP. */
@ParametersAreNonnullByDefault
public final class TcpAstNode extends BooleanExprAstNode {

  public static @Nonnull TcpAstNode instance() {
    return INSTANCE;
  }

  private static final TcpAstNode INSTANCE = new TcpAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof TcpAstNode;
  }

  @Override
  public int hashCode() {
    return 0xE11BD082; // randomly generated
  }

  private TcpAstNode() {}
}
