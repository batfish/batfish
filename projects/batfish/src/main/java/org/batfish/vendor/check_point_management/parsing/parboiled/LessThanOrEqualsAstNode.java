package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the {@code less-than-or-equals} comparator. */
@ParametersAreNonnullByDefault
public final class LessThanOrEqualsAstNode implements ComparatorAstNode {

  public static @Nonnull LessThanOrEqualsAstNode instance() {
    return INSTANCE;
  }

  private static final LessThanOrEqualsAstNode INSTANCE = new LessThanOrEqualsAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof LessThanOrEqualsAstNode;
  }

  @Override
  public int hashCode() {
    return 0xB6CB113F; // randomly generated
  }

  private LessThanOrEqualsAstNode() {}
}
