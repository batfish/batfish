package org.batfish.representation.cumulus_nclu;

import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

/** A {@link RouteMapMatch} that matches routes with exact tag */
public class RouteMapMatchTag implements RouteMapMatch {

  private final long _tag;

  public RouteMapMatchTag(long tag) {
    _tag = tag;
  }

  public long getTag() {
    return _tag;
  }

  @Nonnull
  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CumulusNcluConfiguration vc, Warnings w) {
    return new MatchTag(IntComparator.EQ, new LiteralLong(_tag));
  }
}
