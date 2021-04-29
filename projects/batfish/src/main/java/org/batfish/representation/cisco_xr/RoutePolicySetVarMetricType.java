package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetVarMetricType extends RoutePolicySetStatement {

  private String _var;

  public RoutePolicySetVarMetricType(String var) {
    _var = var;
  }

  @Override
  public Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new SetVarMetricType(_var);
  }
}
