package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing the condition that the packet direction is classified as {@code
 * outgoing}.
 */
@ParametersAreNonnullByDefault
public final class OutgoingAstNode implements BooleanExprAstNode {

  public static @Nonnull OutgoingAstNode instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitOutgoingAstNode(this, arg);
  }

  private static final OutgoingAstNode INSTANCE = new OutgoingAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof OutgoingAstNode;
  }

  @Override
  public int hashCode() {
    return 0x3FA69E8B; // randomly generated
  }

  private OutgoingAstNode() {}
}
