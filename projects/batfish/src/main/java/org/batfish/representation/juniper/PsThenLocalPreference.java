package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class PsThenLocalPreference extends PsThen {

  /** */
  private static final long serialVersionUID = 1L;

  private final int _localPreference;

  public PsThenLocalPreference(int localPreference) {
    _localPreference = localPreference;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetLocalPreference(new LiteralInt(_localPreference)));
  }

  public int getLocalPreference() {
    return _localPreference;
  }
}
