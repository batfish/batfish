package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchColor extends BooleanExpr {

  /** */
  private static final long serialVersionUID = 1L;

  private int _color;

  @JsonCreator
  private MatchColor() {}

  public MatchColor(int color) {
    _color = color;
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
    MatchColor other = (MatchColor) obj;
    if (_color != other._color) {
      return false;
    }
    return true;
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  public int getColor() {
    return _color;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _color;
    return result;
  }

  public void setColor(int color) {
    _color = color;
  }
}
