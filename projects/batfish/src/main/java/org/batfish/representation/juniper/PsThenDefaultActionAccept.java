package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

public class PsThenDefaultActionAccept extends PsThen {

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(Statements.SetDefaultActionAccept.toStaticStatement());
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PsThenDefaultActionAccept;
  }

  @Override
  public int hashCode() {
    return PsThenDefaultActionAccept.class.hashCode();
  }
}
