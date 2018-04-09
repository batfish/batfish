package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class Not extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_EXPR = "expr";

  private BooleanExpr _expr;

  @JsonCreator
  private Not() {}

  public Not(BooleanExpr expr) {
    _expr = expr;
  }

  @Override
  public Set<String> collectSources(
      Set<String> parentSources, Map<String, RoutingPolicy> routingPolicies, Warnings w) {
    return _expr.collectSources(parentSources, routingPolicies, w);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Not other = (Not) obj;
    if (_expr == null) {
      if (other._expr != null) {
        return false;
      }
    } else if (!_expr.equals(other._expr)) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    Result result = _expr.evaluate(environment);
    if (!result.getExit()) {
      result.setBooleanValue(!result.getBooleanValue());
    }
    return result;
  }

  @JsonProperty(PROP_EXPR)
  public BooleanExpr getExpr() {
    return _expr;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_expr == null) ? 0 : _expr.hashCode());
    return result;
  }

  @JsonProperty(PROP_EXPR)
  public void setExpr(BooleanExpr expr) {
    _expr = expr;
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_EXPR, _expr).toString();
  }
}
