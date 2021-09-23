package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/**
 * An {@link AstNode} representing the condition that the packet direction is classified as {@code
 * outgoing}.
 */
public final class OutgoingAstNode extends BooleanExprAstNode {

  public static @Nonnull OutgoingAstNode instance() {
    return INSTANCE;
  }

  private static final OutgoingAstNode INSTANCE = new OutgoingAstNode();

  private OutgoingAstNode() {}
}
