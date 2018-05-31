package org.batfish.representation.juniper;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

public class PsFromTag extends PsFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _tag;

  public PsFromTag(int tag) {
    _tag = tag;
  }

  public int getTag() {
    return _tag;
  }

  @Override
  public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
    return new MatchTag(IntComparator.EQ, new LiteralInt(_tag));
  }
}
