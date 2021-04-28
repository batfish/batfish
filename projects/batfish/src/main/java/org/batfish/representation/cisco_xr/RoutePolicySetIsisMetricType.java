package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.isis.IsisMetricType;
import org.batfish.datamodel.routing_policy.statement.SetIsisMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public class RoutePolicySetIsisMetricType extends RoutePolicyStatement {

  private IsisMetricType _type;

  public RoutePolicySetIsisMetricType(IsisMetricType type) {
    _type = type;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    statements.add(new SetIsisMetricType(_type));
    // Modified routes are not subject to default-drop disposition
    statements.add(Statements.SetDefaultActionAccept.toStaticStatement());
  }
}
