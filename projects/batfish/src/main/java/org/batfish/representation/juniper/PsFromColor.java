package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchColor;

/** Represents a "from color" line in a {@link PsTerm} */
public final class PsFromColor extends PsFrom {

  private final int _color;

  public PsFromColor(int color) {
    _color = color;
  }

  public int getColor() {
    return _color;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchColor(_color);
  }
}
