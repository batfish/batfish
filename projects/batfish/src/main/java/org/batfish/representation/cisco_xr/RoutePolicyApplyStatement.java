package org.batfish.representation.cisco_xr;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyApplyStatement extends RoutePolicyStatement {

  private String _applyName;

  public RoutePolicyApplyStatement(String name) {
    _applyName = name;
  }

  @Override
  public void applyTo(
      List<Statement> statements, CiscoXrConfiguration cc, Configuration c, Warnings w) {
    statements.add(new CallStatement(_applyName));
  }

  public String getName() {
    return _applyName;
  }
}
