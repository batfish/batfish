package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Represents the {@code then add-path send-count} action in a Juniper routing policy, which caps
 * the number of additional paths advertised per prefix to routes matched by the term.
 *
 * <p>The directive is only honored when add-path send is also enabled at the BGP group/neighbor
 * level (via {@code family <af> add-path send path-count}); otherwise Junos ignores it.
 *
 * <p>Batfish does not yet model the number of additional paths to advertise (the same gap exists
 * for the group-level {@code add-path send path-count}), so this action is currently a no-op in the
 * vendor-independent model.
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
