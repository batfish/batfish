package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchColor2;

/** Represents a "from color" line in a {@link PsTerm} */
public final class PsFromColor2 extends PsFrom {

  private final long _color2;

  public PsFromColor2(long color2) {
    _color2 = color2;
  }

  public long getColor2() {
    return _color2;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchColor2(_color2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsFromColor2)) {
      return false;
    }
    PsFromColor2 that = (PsFromColor2) o;
    return _color2 == that._color2;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color2);
  }
}
