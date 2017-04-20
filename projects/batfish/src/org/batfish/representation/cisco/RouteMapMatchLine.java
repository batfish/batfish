package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.common.Warnings;

public abstract class RouteMapMatchLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract BooleanExpr toBooleanExpr(Configuration c,
         CiscoConfiguration cc, Warnings w);

}
