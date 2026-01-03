package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An {@link LongMatchExpr} that matches an integer with the specified comparator relationship to
 * the integer represented by the specified expression.
 */
public final class LongComparison extends LongMatchExpr {

  public LongComparison(IntComparator comparator, LongExpr expr) {
    _comparator = comparator;
    _expr = expr;
  }

  @Override
  public <T, U> T accept(LongMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLongComparison(this, arg);
  }

  @JsonProperty(PROP_COMPARATOR)
  public @Nonnull IntComparator getComparator() {
    return _comparator;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull LongExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LongComparison)) {
      return false;
    }
    LongComparison rhs = (LongComparison) obj;
    return _comparator == rhs._comparator && _expr.equals(rhs._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator.ordinal(), _expr);
  }

  private static final String PROP_COMPARATOR = "comparator";
  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull LongComparison create(
      @JsonProperty(PROP_COMPARATOR) @Nullable IntComparator comparator,
      @JsonProperty(PROP_EXPR) @Nullable LongExpr expr) {
    checkArgument(comparator != null, "Missing %s", PROP_COMPARATOR);
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new LongComparison(comparator, expr);
  }

  private final @Nonnull IntComparator _comparator;
  private final @Nonnull LongExpr _expr;
}
