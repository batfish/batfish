package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public final class MatchColor extends BooleanExpr {

  private static final String PROP_COLOR = "color";

  private final long _color;

  @JsonCreator
  private static MatchColor create(@JsonProperty(PROP_COLOR) int color) {
    return new MatchColor(color);
  }

  public MatchColor(long color) {
    _color = color;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchColor(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for MatchColor.evaluate()");
  }

  @JsonProperty(PROP_COLOR)
  public long getColor() {
    return _color;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchColor)) {
      return false;
    }
    MatchColor other = (MatchColor) obj;
    return _color == other._color;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color);
  }
}
