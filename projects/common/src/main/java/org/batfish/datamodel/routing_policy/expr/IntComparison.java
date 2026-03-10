package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link IntMatchExpr} that matches an integer with the specified comparator relationship to the
 * integer represented by the specified expression.
 */
public final class IntComparison extends IntMatchExpr {

  public IntComparison(IntComparator comparator, IntExpr expr) {
    _comparator = comparator;
    _expr = expr;
  }

  @Override
  public <T, U> T accept(IntMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitIntComparison(this, arg);
  }

  @JsonProperty(PROP_COMPARATOR)
  public @Nonnull IntComparator getComparator() {
    return _comparator;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull IntExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IntComparison)) {
      return false;
    }
    IntComparison rhs = (IntComparison) obj;
    return _comparator == rhs._comparator && _expr.equals(rhs._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator.ordinal(), _expr);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("comparator", _comparator)
        .add("expr", _expr)
        .toString();
  }

  private static final String PROP_COMPARATOR = "comparator";
  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull IntComparison create(
      @JsonProperty(PROP_COMPARATOR) @Nullable IntComparator comparator,
      @JsonProperty(PROP_EXPR) @Nullable IntExpr expr) {
    checkArgument(comparator != null, "Missing %s", PROP_COMPARATOR);
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new IntComparison(comparator, expr);
  }

  private final @Nonnull IntComparator _comparator;
  private final @Nonnull IntExpr _expr;
}
