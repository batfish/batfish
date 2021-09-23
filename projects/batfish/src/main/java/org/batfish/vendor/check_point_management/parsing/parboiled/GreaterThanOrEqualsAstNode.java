package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the {@code greater-than-or-equals} comparator. */
public final class GreaterThanOrEqualsAstNode implements ComparatorAstNode {

  public static @Nonnull GreaterThanOrEqualsAstNode instance() {
    return INSTANCE;
  }

  private static final GreaterThanOrEqualsAstNode INSTANCE = new GreaterThanOrEqualsAstNode();

  private GreaterThanOrEqualsAstNode() {}
}
