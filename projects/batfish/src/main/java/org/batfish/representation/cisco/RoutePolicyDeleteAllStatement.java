package org.batfish.representation.cisco;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetCommunity;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyDeleteAllStatement extends RoutePolicyDeleteStatement {

  private static final long serialVersionUID = 1L;

  @Override
  public RoutePolicyDeleteType getDeleteType() {
    return RoutePolicyDeleteType.ALL;
  }

  @Override
  public Statement toSetStatement(CiscoConfiguration cc, Configuration c, Warnings w) {
    // TODO: this should actually just retain all well-known communities
    return SetCommunity.NONE;
  }
}
