package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class UnhandledAstNode extends BooleanExprAstNode {

  public static @Nonnull UnhandledAstNode instance() {
    return INSTANCE;
  }

  private static final UnhandledAstNode INSTANCE = new UnhandledAstNode();

  private UnhandledAstNode() {}
}
