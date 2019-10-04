package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntMatchExpr;

/**
 * An expression that matches a standard community whose high 16 bits are matched by the provided
 * expression.
 */
public final class StandardCommunityHighMatch extends CommunityMatchExpr {

  public StandardCommunityHighMatch(IntMatchExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitStandardCommunityHighMatch(this, arg);
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull IntMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StandardCommunityHighMatch)) {
      return false;
    }
    return _expr.equals(((StandardCommunityHighMatch) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull StandardCommunityHighMatch create(
      @JsonProperty(PROP_EXPR) @Nullable IntMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new StandardCommunityHighMatch(expr);
  }

  private final @Nonnull IntMatchExpr _expr;
}
