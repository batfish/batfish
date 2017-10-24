package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class MatchAsPath extends BooleanExpr {
  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_EXPR = "expr";

  private AsPathSetExpr _expr;

  @JsonCreator
  private MatchAsPath() {}

  public MatchAsPath(AsPathSetExpr expr) {
    _expr = expr;
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
  public Result evaluate(Environment environment) {
    boolean match = _expr.matches(environment);
    Result result = new Result();
    result.setBooleanValue(match);
    return result;
  }

  @JsonProperty(PROP_EXPR)
  public AsPathSetExpr getExpr() {
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
  public void setExpr(AsPathSetExpr expr) {
    _expr = expr;
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_EXPR, _expr).toString();
  }
}
