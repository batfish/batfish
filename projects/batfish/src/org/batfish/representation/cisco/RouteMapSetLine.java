package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.main.Warnings;

public abstract class RouteMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract void applyTo(List<Statement> statements,
         CiscoConfiguration cc, Configuration c, Warnings w);

   public abstract RouteMapSetType getType();

   public abstract PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w);

}
