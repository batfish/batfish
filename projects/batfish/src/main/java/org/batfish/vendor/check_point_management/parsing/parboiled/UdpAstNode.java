package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the condition that the packet protocol is UDP. */
public final class UdpAstNode extends BooleanExprAstNode {

  public static @Nonnull UdpAstNode instance() {
    return INSTANCE;
  }

  private static final UdpAstNode INSTANCE = new UdpAstNode();

  private UdpAstNode() {}
}
