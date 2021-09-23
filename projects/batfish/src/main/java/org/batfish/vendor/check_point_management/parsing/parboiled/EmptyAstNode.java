package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/**
 * An {@link AstNode} that is the result of parsing an empty match expression. Should be treated as
 * {@code true} in evaluation.
 */
public final class EmptyAstNode extends BooleanExprAstNode {

  public static @Nonnull EmptyAstNode instance() {
    return INSTANCE;
  }

  private static final EmptyAstNode INSTANCE = new EmptyAstNode();

  private EmptyAstNode() {}
}
