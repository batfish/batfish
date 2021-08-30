package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link CommunitySet} iff it is not matched by the provided {@link
 * CommunitySetMatchExpr}.
 */
public final class CommunitySetNot extends CommunitySetMatchExpr {

  public static CommunitySetMatchExpr not(CommunitySetMatchExpr expr) {
    if (expr instanceof CommunitySetNot) {
      return ((CommunitySetNot) expr)._expr;
    }
    return new CommunitySetNot(expr);
  }

  @VisibleForTesting
  public CommunitySetNot(CommunitySetMatchExpr expr) {
    _expr = expr;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull CommunitySetMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetNot)) {
      return false;
    }
    return _expr.equals(((CommunitySetNot) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetNot(this, arg);
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull CommunitySetNot create(
      @JsonProperty(PROP_EXPR) @Nullable CommunitySetMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new CommunitySetNot(expr);
  }

  private final @Nonnull CommunitySetMatchExpr _expr;
}
