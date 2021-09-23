package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class ErrorAstNode extends BooleanExprAstNode {

  public static @Nonnull ErrorAstNode instance() {
    return INSTANCE;
  }

  private static final ErrorAstNode INSTANCE = new ErrorAstNode();

  private ErrorAstNode() {}
}
