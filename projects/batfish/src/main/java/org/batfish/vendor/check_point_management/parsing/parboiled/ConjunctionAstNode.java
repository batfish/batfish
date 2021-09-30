package org.batfish.vendor.check_point_management.parsing.parboiled;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a conjunction of boolean expressions. */
@ParametersAreNonnullByDefault
public final class ConjunctionAstNode implements BooleanExprAstNode {

  @VisibleForTesting
  public ConjunctionAstNode(BooleanExprAstNode... conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  ConjunctionAstNode(Iterable<BooleanExprAstNode> conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  @Override
  public <T, U> T accept(BooleanExprAstNodeVisitor<T, U> visitor, U arg) {
    return visitor.visitConjunctionAstNode(this, arg);
  }

  @Nonnull
  @Override
  public BooleanExprAstNode and(BooleanExprAstNode conjunct) {
    return new ConjunctionAstNode(
        ImmutableList.<BooleanExprAstNode>builder().addAll(_conjuncts).add(conjunct).build());
  }

  public @Nonnull List<BooleanExprAstNode> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ConjunctionAstNode)) {
      return false;
    }
    ConjunctionAstNode that = (ConjunctionAstNode) o;
    return _conjuncts.equals(that._conjuncts);
  }

  @Override
  public int hashCode() {
    return _conjuncts.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_conjuncts", _conjuncts).toString();
  }

  private final @Nonnull List<BooleanExprAstNode> _conjuncts;
}
