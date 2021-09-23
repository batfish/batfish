package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

public final class GreaterThanOrEqualsAstNode implements ComparatorAstNode {

  public static @Nonnull GreaterThanOrEqualsAstNode instance() {
    return INSTANCE;
  }

  private static final GreaterThanOrEqualsAstNode INSTANCE = new GreaterThanOrEqualsAstNode();

  private GreaterThanOrEqualsAstNode() {}
}
