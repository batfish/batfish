package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

public abstract class RouteMapMatchLine implements Serializable {

  public abstract BooleanExpr toBooleanExpr(Configuration c, AristaConfiguration cc, Warnings w);
}
