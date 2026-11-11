package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetColor2;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the route color2
 * (a local route preference value) for a matched route.
 */
public final class PsThenColor2 extends PsThen {

  private final long _color2;

  public PsThenColor2(long color2) {
    _color2 = color2;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetColor2(new LiteralLong(_color2)));
  }

  public long getColor2() {
    return _color2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenColor2)) {
      return false;
    }
    PsThenColor2 psThenColor2 = (PsThenColor2) o;
    return _color2 == psThenColor2._color2;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_color2);
  }
}
