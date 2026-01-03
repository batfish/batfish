package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.IntMatchExpr;

/**
 * Matches a {@link CommunitySet} iff it has {@link CommunitySet#getCommunities()} with {@link
 * java.util.Set#size} that is matched by the provided {@link IntExpr}.
 */
public final class HasSize extends CommunitySetMatchExpr {

  public HasSize(IntMatchExpr expr) {
    _expr = expr;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull IntMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof HasSize)) {
      return false;
    }
    return _expr.equals(((HasSize) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("expr", _expr).toString();
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitHasSize(this, arg);
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull HasSize create(@JsonProperty(PROP_EXPR) @Nullable IntMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new HasSize(expr);
  }

  private final @Nonnull IntMatchExpr _expr;
}
