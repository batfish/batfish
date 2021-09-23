package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the {@code greater-than} comparator. */
public final class GreaterThanAstNode implements ComparatorAstNode {

  public static @Nonnull GreaterThanAstNode instance() {
    return INSTANCE;
  }

  private static final GreaterThanAstNode INSTANCE = new GreaterThanAstNode();

  private GreaterThanAstNode() {}
}
