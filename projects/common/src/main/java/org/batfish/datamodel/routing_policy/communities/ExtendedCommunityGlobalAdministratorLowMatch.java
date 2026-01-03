package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntMatchExpr;

/**
 * An expression that matches an extended community whose global administrator's low 16 bits are
 * matched by the provided expression.
 */
public final class ExtendedCommunityGlobalAdministratorLowMatch extends CommunityMatchExpr {

  public ExtendedCommunityGlobalAdministratorLowMatch(IntMatchExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitExtendedCommunityGlobalAdministratorLowMatch(this, arg);
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
    if (!(obj instanceof ExtendedCommunityGlobalAdministratorLowMatch)) {
      return false;
    }
    return _expr.equals(((ExtendedCommunityGlobalAdministratorLowMatch) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull ExtendedCommunityGlobalAdministratorLowMatch create(
      @JsonProperty(PROP_EXPR) @Nullable IntMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new ExtendedCommunityGlobalAdministratorLowMatch(expr);
  }

  private final @Nonnull IntMatchExpr _expr;
}
