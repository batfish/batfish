package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/** Boolean expression that evaluates to the opposite of some given {@link BooleanExpr}. */
@ParametersAreNonnullByDefault
public final class Not extends BooleanExpr {

  private static final String PROP_EXPR = "expr";
  private final BooleanExpr _expr;

  @JsonCreator
  private static Not create(@JsonProperty(PROP_EXPR) @Nullable BooleanExpr expr) {
    checkArgument(expr != null, "%s must be provided", PROP_EXPR);
    return new Not(expr);
  }

  public Not(BooleanExpr expr) {
    _expr = expr;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitNot(this, arg);
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    return _expr.collectSources(parentSources, routingPolicies, w);
  }

  @Override
  public Result evaluate(Environment environment) {
    Result result = _expr.evaluate(environment);
    return result.getExit() ? result : new Result(!result.getBooleanValue());
  }

  @JsonProperty(PROP_EXPR)
  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Not)) {
      return false;
    }
    Not other = (Not) obj;
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
