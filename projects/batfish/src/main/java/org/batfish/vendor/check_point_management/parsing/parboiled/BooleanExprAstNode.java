package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a boolean expression. */
@ParametersAreNonnullByDefault
public abstract class BooleanExprAstNode implements AstNode {

  public @Nonnull BooleanExprAstNode or(BooleanExprAstNode disjunct) {
    return new DisjunctionAstNode(this, disjunct);
  }

  public @Nonnull BooleanExprAstNode and(BooleanExprAstNode conjunct) {
    return new ConjunctionAstNode(this, conjunct);
  }
}
