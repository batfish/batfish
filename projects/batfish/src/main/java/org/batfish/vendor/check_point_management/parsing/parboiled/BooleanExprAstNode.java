package org.batfish.vendor.check_point_management.parsing.parboiled;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a boolean expression. */
@ParametersAreNonnullByDefault
public interface BooleanExprAstNode extends AstNode {

  <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg);

  default @Nonnull BooleanExprAstNode or(BooleanExprAstNode disjunct) {
    return new DisjunctionAstNode(this, disjunct);
  }

  default @Nonnull BooleanExprAstNode and(BooleanExprAstNode conjunct) {
    return new ConjunctionAstNode(this, conjunct);
  }
}
