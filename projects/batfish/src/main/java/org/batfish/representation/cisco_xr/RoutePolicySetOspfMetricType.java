package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetOspfMetricType extends RoutePolicySetStatement {

  private OspfMetricType _type;

  public RoutePolicySetOspfMetricType(OspfMetricType type) {
    _type = type;
  }

  @Override
  public Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetOspfMetricType(_type);
  }
}
