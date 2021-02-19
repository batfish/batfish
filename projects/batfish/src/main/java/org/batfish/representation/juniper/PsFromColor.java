package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchColor;

/** Represents a "from color" line in a {@link PsTerm} */
public final class PsFromColor extends PsFrom {

  private final long _color;

  public PsFromColor(long color) {
    _color = color;
  }

  public long getColor() {
    return _color;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchColor(_color);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsFromColor)) {
      return false;
    }
    PsFromColor that = (PsFromColor) o;
    return _color == that._color;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color);
  }
}
