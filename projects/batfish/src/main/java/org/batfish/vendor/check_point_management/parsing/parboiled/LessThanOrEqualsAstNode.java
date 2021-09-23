package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the {@code less-than-or-equals} comparator. */
public final class LessThanOrEqualsAstNode implements ComparatorAstNode {

  public static @Nonnull LessThanOrEqualsAstNode instance() {
    return INSTANCE;
  }

  private static final LessThanOrEqualsAstNode INSTANCE = new LessThanOrEqualsAstNode();

  private LessThanOrEqualsAstNode() {}
}
