package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

/** Represents a "from tag" line in a {@link PsTerm} */
public final class PsFromTag extends PsFrom {

  private final long _tag;

  public PsFromTag(long tag) {
    _tag = tag;
  }

  public long getTag() {
    return _tag;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchTag(IntComparator.EQ, new LiteralLong(_tag));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsFromTag)) {
      return false;
    }
    PsFromTag psFromTag = (PsFromTag) o;
    return _tag == psFromTag._tag;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_tag);
  }
}
