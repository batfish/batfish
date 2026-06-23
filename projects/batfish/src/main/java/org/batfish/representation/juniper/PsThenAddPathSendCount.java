package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the {@code then add-path send-count} action in a Juniper routing policy.
 *
 * <p>This action only takes effect when add-path send is also enabled at the BGP group/neighbor
 * level (e.g. {@code set protocols bgp group G family inet unicast add-path send path-count N}).
 * Without group-level add-path send, Junos silently ignores the policy directive. The action
 * therefore has no effect on the vendor-independent model; the conditions under which it is dead
 * config are surfaced as a risky warning during BGP conversion (see {@link JuniperConfiguration}).
 */
public final class PsThenAddPathSendCount extends PsThen {

  private final int _sendCount;

  public PsThenAddPathSendCount(int sendCount) {
    _sendCount = sendCount;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // No-op: add-path send is controlled at the BGP group/neighbor level, not via policy.
  }

  public int getSendCount() {
    return _sendCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PsThenAddPathSendCount)) {
      return false;
    }
    PsThenAddPathSendCount that = (PsThenAddPathSendCount) o;
    return _sendCount == that._sendCount;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(_sendCount);
  }
}
