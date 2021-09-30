package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the {@code greater-than-or-equals} comparator. */
@ParametersAreNonnullByDefault
public final class GreaterThanOrEqualsAstNode implements ComparatorAstNode {

  public static @Nonnull GreaterThanOrEqualsAstNode instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(ComparatorAstNodeVisitor<T, U> visitor, U value) {
    return visitor.visitGreaterThanOrEqualsAstNode(this, value);
  }

  private static final GreaterThanOrEqualsAstNode INSTANCE = new GreaterThanOrEqualsAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof GreaterThanOrEqualsAstNode;
  }

  @Override
  public int hashCode() {
    return 0x73732991; // randomly generated
  }

  private GreaterThanOrEqualsAstNode() {}
}
