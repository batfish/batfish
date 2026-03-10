package org.batfish.vendor.arista.representation;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

public class RouteMapMatchTagLine extends RouteMapMatchLine {

  private final Set<Long> _tags;

  public RouteMapMatchTagLine(Set<Long> tags) {
    _tags = tags;
  }

  public Set<Long> getTags() {
    return _tags;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (long tag : _tags) {
      disjuncts.add(new MatchTag(IntComparator.EQ, new LiteralLong(tag)));
    }
    return d.simplify();
  }
}
