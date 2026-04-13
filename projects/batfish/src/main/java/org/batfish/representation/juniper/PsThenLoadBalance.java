package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents {@code then load-balance per-packet} in a Junos policy-statement. Has no effect on the
 * VI routing policy (Batfish models all ECMP paths), but is recognized so that forwarding-table
 * export warnings can distinguish it from attribute mutations.
 */
public final class PsThenLoadBalance extends PsThen {

  public static final PsThenLoadBalance INSTANCE = new PsThenLoadBalance();

  private PsThenLoadBalance() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // No-op: Batfish models all ECMP paths regardless of per-packet/per-flow selection.
  }
}
