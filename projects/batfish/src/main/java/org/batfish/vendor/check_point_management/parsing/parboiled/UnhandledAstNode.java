package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link AstNode} representing a boolean constraint that Batfish is incapable of evaluating. May
 * be evaluated as {@code true} or {@code false} depending on context.
 */
@ParametersAreNonnullByDefault
public final class UnhandledAstNode extends BooleanExprAstNode {

  public static @Nonnull UnhandledAstNode instance() {
    return INSTANCE;
  }

  private static final UnhandledAstNode INSTANCE = new UnhandledAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof UnhandledAstNode;
  }

  @Override
  public int hashCode() {
    return 0xD26A9570; // randomly generated
  }

  private UnhandledAstNode() {}
}
