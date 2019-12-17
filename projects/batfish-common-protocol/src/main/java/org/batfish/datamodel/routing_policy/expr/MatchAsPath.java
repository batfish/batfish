package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that tests whether an {@link Environment} contains a BGP route with an AS path
 * that matches a given {@link AsPathSetExpr}.
 */
public final class MatchAsPath extends BooleanExpr {

  private static final String PROP_EXPR = "expr";
  private final AsPathSetExpr _expr;

  @JsonCreator
  private static MatchAsPath create(@JsonProperty(PROP_EXPR) AsPathSetExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new MatchAsPath(expr);
  }

  public MatchAsPath(AsPathSetExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchAsPath(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    return new Result(_expr.matches(environment));
  }

  @JsonProperty(PROP_EXPR)
  public AsPathSetExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchAsPath)) {
      return false;
    }
    MatchAsPath other = (MatchAsPath) obj;
    return Objects.equals(_expr, other._expr);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_expr);
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_EXPR, _expr).toString();
  }
}
