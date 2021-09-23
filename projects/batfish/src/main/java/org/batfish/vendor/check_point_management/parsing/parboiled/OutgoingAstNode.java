package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class OutgoingAstNode extends BooleanExprAstNode {

  public static @Nonnull OutgoingAstNode instance() {
    return INSTANCE;
  }

  private static final OutgoingAstNode INSTANCE = new OutgoingAstNode();

  private OutgoingAstNode() {}
}
