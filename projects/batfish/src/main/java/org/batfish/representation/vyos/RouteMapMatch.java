package org.batfish.representation.vyos;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.common.Warnings;

public interface RouteMapMatch extends Serializable {

   BooleanExpr toBooleanExpr(VyosConfiguration vc, Configuration c, Warnings w);

}
