package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/** An {@link AstNode} representing the {@code equals} comparator. */
public final class EqualsAstNode implements ComparatorAstNode {

  public static @Nonnull EqualsAstNode instance() {
    return INSTANCE;
  }

  private static final EqualsAstNode INSTANCE = new EqualsAstNode();

  private EqualsAstNode() {}
}
