package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class HasRoute extends BooleanExpr {
  private static final String PROP_EXPR = "expr";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private PrefixSetExpr _expr;

  @JsonCreator
  private static HasRoute jsonCreator(@Nullable @JsonProperty(PROP_EXPR) PrefixSetExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new HasRoute(expr);
  }

  public HasRoute(PrefixSetExpr expr) {
    _expr = expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof HasRoute)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HasRoute other = (HasRoute) obj;
    return _expr.equals(other._expr);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @JsonProperty(PROP_EXPR)
  @Nonnull
  public PrefixSetExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _expr.hashCode();
    return result;
  }

  public void setExpr(PrefixSetExpr expr) {
    _expr = expr;
  }
}
