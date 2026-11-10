package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag2;

/** Represents a "from tag2" line in a {@link PsTerm} */
public final class PsFromTag2 extends PsFrom {

  private final long _tag2;

  public PsFromTag2(long tag2) {
    _tag2 = tag2;
  }

  public long getTag2() {
    return _tag2;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchTag2(IntComparator.EQ, new LiteralLong(_tag2));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsFromTag2)) {
      return false;
    }
    PsFromTag2 psFromTag2 = (PsFromTag2) o;
    return _tag2 == psFromTag2._tag2;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_tag2);
  }
}
