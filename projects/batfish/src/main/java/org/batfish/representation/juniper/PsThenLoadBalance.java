package org.batfish.representation.juniper;

import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents {@code then load-balance <method>} in a Junos policy-statement. Has no effect on the
 * VI routing policy (Batfish models all ECMP paths), but is recognized so that forwarding-table
 * export warnings can distinguish it from attribute mutations.
 */
public final class PsThenLoadBalance extends PsThen {

  public enum LoadBalanceMethod {
    ADAPTIVE,
    CONSISTENT_HASH,
    DESTINATION_IP_ONLY,
    PER_FLOW,
    PER_PACKET,
    PER_PREFIX,
    PROFILE1,
    PROFILE2,
    RANDOM,
    SOURCE_IP_ONLY,
    SYMMETRIC_CONSISTENT_HASH,
  }

  private final @Nonnull LoadBalanceMethod _method;

  public PsThenLoadBalance(LoadBalanceMethod method) {
    _method = method;
  }

  public @Nonnull LoadBalanceMethod getMethod() {
    return _method;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // No-op: Batfish models all ECMP paths regardless of per-packet/per-flow selection.
  }
}
