package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.main.Warnings;

public abstract class RouteMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract RouteMapSetType getType();

   public abstract PolicyMapSetLine toPolicyMapSetLine(CiscoConfiguration v,
         Configuration c, Warnings w);

}
