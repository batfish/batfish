package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class UdpAstNode extends BooleanExprAstNode {

  public static @Nonnull UdpAstNode instance() {
    return INSTANCE;
  }

  private static final UdpAstNode INSTANCE = new UdpAstNode();

  private UdpAstNode() {}
}
