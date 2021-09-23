package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing the condition that the packet direction is classified as {@code
 * incoming}.
 */
@ParametersAreNonnullByDefault
public final class IncomingAstNode extends BooleanExprAstNode {

  public static @Nonnull IncomingAstNode instance() {
    return INSTANCE;
  }

  private static final IncomingAstNode INSTANCE = new IncomingAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof IncomingAstNode;
  }

  @Override
  public int hashCode() {
    return 0xB6D251E7; // randomly generated
  }

  private IncomingAstNode() {}
}
