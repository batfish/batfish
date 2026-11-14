package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetTag2;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the action in Juniper's routing policy (policy statement) which sets the second tag
 * (tag2) for a matched route.
 */
public final class PsThenTag2 extends PsThen {

  private final long _tag2;

  public PsThenTag2(long tag2) {
    _tag2 = tag2;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetTag2(new LiteralLong(_tag2)));
  }

  public long getTag2() {
    return _tag2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenTag2)) {
      return false;
    }
    PsThenTag2 psThenTag2 = (PsThenTag2) o;
    return _tag2 == psThenTag2._tag2;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_tag2);
  }
}
