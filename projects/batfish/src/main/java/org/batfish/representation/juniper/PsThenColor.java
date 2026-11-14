package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetColor;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the route color
 * (a local route preference value) for a matched route.
 */
public final class PsThenColor extends PsThen {

  private final long _color;

  public PsThenColor(long color) {
    _color = color;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetColor(new LiteralLong(_color)));
  }

  public long getColor() {
    return _color;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenColor)) {
      return false;
    }
    PsThenColor psThenColor = (PsThenColor) o;
    return _color == psThenColor._color;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color);
  }
}
