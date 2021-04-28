package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public class RoutePolicySetOspfMetricType extends RoutePolicyStatement {

  private OspfMetricType _type;

  public RoutePolicySetOspfMetricType(OspfMetricType type) {
    _type = type;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetOspfMetricType(_type));
    // Modified routes are not subject to default-drop disposition
    statements.add(Statements.SetDefaultActionAccept.toStaticStatement());
  }
}
