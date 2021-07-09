package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.BgpPeerAddressNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that sets the next hop to the peer's address. */
public final class PsThenNextHopPeerAddress extends PsThen {

  public static final PsThenNextHopPeerAddress INSTANCE = new PsThenNextHopPeerAddress();

  private PsThenNextHopPeerAddress() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    statements.add(new SetNextHop(BgpPeerAddressNextHop.getInstance()));
  }
}
