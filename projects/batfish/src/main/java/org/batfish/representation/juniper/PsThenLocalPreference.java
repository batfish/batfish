package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.Statement;

@ParametersAreNonnullByDefault
public final class PsThenLocalPreference extends PsThen {

  private static final long serialVersionUID = 1L;

  private final long _localPreference;

  public PsThenLocalPreference(long localPreference) {
    _localPreference = localPreference;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    statements.add(new SetLocalPreference(new LiteralLong(_localPreference)));
  }

  public long getLocalPreference() {
    return _localPreference;
  }
}
