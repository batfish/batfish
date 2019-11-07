package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.NeighborIsAsPath;
import org.batfish.datamodel.routing_policy.expr.SubRangeExpr;

public class RoutePolicyBooleanAsPathNeighborIs extends RoutePolicyBoolean {

  private boolean _exact;

  private List<SubRangeExpr> _range;

  public RoutePolicyBooleanAsPathNeighborIs(List<SubRangeExpr> range, boolean exact) {
    _range = range;
    _exact = exact;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new NeighborIsAsPath(_range, _exact);
  }
}
