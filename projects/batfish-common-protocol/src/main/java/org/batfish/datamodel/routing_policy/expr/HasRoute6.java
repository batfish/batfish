package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class HasRoute6 extends BooleanExpr {

  private static final String PROP_EXPR = "expr";

  private final Prefix6SetExpr _expr;

  @JsonCreator
  private static HasRoute6 create(@JsonProperty(PROP_EXPR) Prefix6SetExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new HasRoute6(expr);
  }

  public HasRoute6(Prefix6SetExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitHasRoute6(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for HasRoute6.evaluate()");
  }

  @JsonProperty(PROP_EXPR)
  public Prefix6SetExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HasRoute6)) {
      return false;
    }
    HasRoute6 other = (HasRoute6) obj;
    return Objects.equals(_expr, other._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_expr);
  }
}
