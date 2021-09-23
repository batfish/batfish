package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/**
 * An {@link AstNode} representing the condition that the packet direction is classified as {@code
 * incoming}.
 */
public final class IncomingAstNode extends BooleanExprAstNode {

  public static @Nonnull IncomingAstNode instance() {
    return INSTANCE;
  }

  private static final IncomingAstNode INSTANCE = new IncomingAstNode();

  private IncomingAstNode() {}
}
