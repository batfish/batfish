package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the {@code less-than} comparator. */
@ParametersAreNonnullByDefault
public final class LessThanAstNode implements ComparatorAstNode {

  public static @Nonnull LessThanAstNode instance() {
    return INSTANCE;
  }

  private static final LessThanAstNode INSTANCE = new LessThanAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof LessThanAstNode;
  }

  @Override
  public int hashCode() {
    return 0x59B7C219; // randomly generated
  }

  private LessThanAstNode() {}
}
