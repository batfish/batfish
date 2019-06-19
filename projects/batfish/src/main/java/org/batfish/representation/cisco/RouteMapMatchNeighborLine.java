package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;

public class RouteMapMatchNeighborLine extends RouteMapMatchLine {

  private static final long serialVersionUID = 1L;

  private String _neighborIp;

  public RouteMapMatchNeighborLine(String neighborIP) {
    _neighborIp = neighborIP;
  }

  public String getNeighborIp() {
    return _neighborIp;
  }

  @Override
  public BooleanExpr toBooleanExpr(Configuration c, CiscoConfiguration cc, Warnings w) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }
}
