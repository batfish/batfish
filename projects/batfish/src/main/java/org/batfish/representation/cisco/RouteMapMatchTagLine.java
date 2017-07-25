package org.batfish.representation.cisco;

import java.util.List;
import java.util.Set;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.MatchTag;

public class RouteMapMatchTagLine extends RouteMapMatchLine {

  private static final long serialVersionUID = 1L;

  private Set<Integer> _tags;

  public RouteMapMatchTagLine(Set<Integer> tags) {
    _tags = tags;
  }

  public Set<Integer> getTags() {
    return _tags;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    Disjunction d = new Disjunction();
    List<BooleanExpr> disjuncts = d.getDisjuncts();
    for (int tag : _tags) {
      disjuncts.add(new MatchTag(IntComparator.EQ, new LiteralInt(tag)));
    }
    return d.simplify();
  }
}
