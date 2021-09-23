package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the {@code less-than} comparator. */
public final class LessThanAstNode implements ComparatorAstNode {

  public static @Nonnull LessThanAstNode instance() {
    return INSTANCE;
  }

  private static final LessThanAstNode INSTANCE = new LessThanAstNode();

  private LessThanAstNode() {}
}
