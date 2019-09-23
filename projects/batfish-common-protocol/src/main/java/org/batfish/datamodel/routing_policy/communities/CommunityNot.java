package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/** Matches a {@link Community} iff it is not matched by the provided {@link CommunityMatchExpr}. */
public final class CommunityNot extends CommunityMatchExpr {

  public CommunityNot(CommunityMatchExpr expr) {
    _expr = expr;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull CommunityMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityNot)) {
      return false;
    }
    return _expr.equals(((CommunityNot) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityNot(this, arg);
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull CommunityNot create(
      @JsonProperty(PROP_EXPR) @Nullable CommunityMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new CommunityNot(expr);
  }

  private final @Nonnull CommunityMatchExpr _expr;
}
