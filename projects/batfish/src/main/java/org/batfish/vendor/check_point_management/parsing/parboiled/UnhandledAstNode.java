package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;

/**
 * An {@link AstNode} representing a boolean constraint that Batfish is incapable of evaluating. May
 * be evaluated as {@code true} or {@code false} depending on context.
 */
public final class UnhandledAstNode extends BooleanExprAstNode {

  public static @Nonnull UnhandledAstNode instance() {
    return INSTANCE;
  }

  private static final UnhandledAstNode INSTANCE = new UnhandledAstNode();

  private UnhandledAstNode() {}
}
