package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

public abstract class RouteMapSetLine implements Serializable {

  public abstract void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w);
}
