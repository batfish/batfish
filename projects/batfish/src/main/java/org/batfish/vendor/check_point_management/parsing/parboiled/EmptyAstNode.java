package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class EmptyAstNode extends BooleanExprAstNode {

  public static @Nonnull EmptyAstNode instance() {
    return INSTANCE;
  }

  private static final EmptyAstNode INSTANCE = new EmptyAstNode();

  private EmptyAstNode() {}
}
