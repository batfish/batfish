package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the {@code greater-than} comparator. */
@ParametersAreNonnullByDefault
public final class GreaterThanAstNode implements ComparatorAstNode {

  public static @Nonnull GreaterThanAstNode instance() {
    return INSTANCE;
  }

  private static final GreaterThanAstNode INSTANCE = new GreaterThanAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof GreaterThanAstNode;
  }

  @Override
  public int hashCode() {
    return 0x8E7FAD85; // randomly generated
  }

  private GreaterThanAstNode() {}
}
