package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that sets the next hop to reject. */
public final class PsThenNextHopReject extends PsThen {

  public static final PsThenNextHopReject INSTANCE = new PsThenNextHopReject();

  private PsThenNextHopReject() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    statements.add(new SetNextHop(DiscardNextHop.INSTANCE));
  }
}
