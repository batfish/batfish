package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.isis.IsisMetricType;
import org.batfish.datamodel.routing_policy.statement.SetIsisMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetIsisMetricType extends RoutePolicySetStatement {

  private IsisMetricType _type;

  public RoutePolicySetIsisMetricType(IsisMetricType type) {
    _type = type;
  }

  @Override
  public Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetIsisMetricType(_type);
  }
}
