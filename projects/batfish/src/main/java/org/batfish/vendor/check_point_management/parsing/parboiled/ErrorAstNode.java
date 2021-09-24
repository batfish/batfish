package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} resulting from a failure to parse the match expression. */
@ParametersAreNonnullByDefault
public final class ErrorAstNode extends BooleanExprAstNode {

  public static @Nonnull ErrorAstNode instance() {
    return INSTANCE;
  }

  private static final ErrorAstNode INSTANCE = new ErrorAstNode();

  @Override
  public boolean equals(@Nullable Object obj) {
    return this == obj || obj instanceof ErrorAstNode;
  }

  @Override
  public int hashCode() {
    return 0xF40A5BCD; // randomly generated
  }

  private ErrorAstNode() {}
}
