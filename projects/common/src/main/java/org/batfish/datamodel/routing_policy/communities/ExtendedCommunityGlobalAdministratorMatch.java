package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.LongMatchExpr;

/**
 * An expression that matches an extended community whose global administrator is matched by the
 * provided expression.
 */
public final class ExtendedCommunityGlobalAdministratorMatch extends CommunityMatchExpr {

  public ExtendedCommunityGlobalAdministratorMatch(LongMatchExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitExtendedCommunityGlobalAdministratorMatch(this, arg);
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull LongMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ExtendedCommunityGlobalAdministratorMatch)) {
      return false;
    }
    return _expr.equals(((ExtendedCommunityGlobalAdministratorMatch) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull ExtendedCommunityGlobalAdministratorMatch create(
      @JsonProperty(PROP_EXPR) @Nullable LongMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new ExtendedCommunityGlobalAdministratorMatch(expr);
  }

  private final @Nonnull LongMatchExpr _expr;
}
