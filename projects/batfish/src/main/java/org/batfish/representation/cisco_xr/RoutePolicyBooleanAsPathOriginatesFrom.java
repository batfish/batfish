package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.OriginatesFromAsPath;
import org.batfish.datamodel.routing_policy.expr.SubRangeExpr;

public class RoutePolicyBooleanAsPathOriginatesFrom extends RoutePolicyBoolean {

  private List<SubRangeExpr> _asRange;

  private boolean _exact;

  public RoutePolicyBooleanAsPathOriginatesFrom(List<SubRangeExpr> asRange, boolean exact) {
    _asRange = asRange;
    _exact = exact;
  }

  @Override
  public BooleanExpr toBooleanExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new OriginatesFromAsPath(_asRange, _exact);
  }
}
