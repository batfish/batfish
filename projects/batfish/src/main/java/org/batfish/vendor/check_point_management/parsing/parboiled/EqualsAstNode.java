package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing the {@code equals} comparator. */
@ParametersAreNonnullByDefault
public final class EqualsAstNode implements ComparatorAstNode {

  public static @Nonnull EqualsAstNode instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(ComparatorAstNodeVisitor<T, U> visitor, U value) {
    return visitor.visitEqualsAstNode(this, value);
  }

  private static final EqualsAstNode INSTANCE = new EqualsAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof EqualsAstNode;
  }

  @Override
  public int hashCode() {
    return 0x5C7938D4; // randomly generated
  }

  private EqualsAstNode() {}
}
