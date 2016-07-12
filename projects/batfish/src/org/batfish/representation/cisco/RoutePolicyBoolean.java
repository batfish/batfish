package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.main.Warnings;

public abstract class RoutePolicyBoolean implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract RoutePolicyBooleanType getType();

   public abstract BooleanExpr toBooleanExpr(CiscoConfiguration cc,
         Configuration c, Warnings w);

}
