package org.batfish.representation.cisco;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicySetOspfMetricType extends RoutePolicyStatement {

  private static final long serialVersionUID = 1L;

  private OspfMetricType _type;

  public RoutePolicySetOspfMetricType(OspfMetricType type) {
    _type = type;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetOspfMetricType(_type));
  }
}
