package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Configuration;
import org.batfish.representation.PolicyMapSetLine;

public abstract class RouteMapSetLine implements Serializable {

   private static final long serialVersionUID = 1L;

   public abstract RouteMapSetType getType();

   public abstract PolicyMapSetLine toPolicyMapSetLine(Configuration c);

}
