package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} that is the result of parsing an empty match expression. Should be treated as
 * {@code true} in evaluation.
 */
@ParametersAreNonnullByDefault
public final class EmptyAstNode implements BooleanExprAstNode {

  public static @Nonnull EmptyAstNode instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitEmptyAstNode(this, arg);
  }

  private static final EmptyAstNode INSTANCE = new EmptyAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof EmptyAstNode;
  }

  @Override
  public int hashCode() {
    return 0xF3858ECF; // randomly generated
  }

  private EmptyAstNode() {}
}
