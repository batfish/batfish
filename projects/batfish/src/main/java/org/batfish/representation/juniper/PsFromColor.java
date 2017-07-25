package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.MatchColor;

public final class PsFromColor extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

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
