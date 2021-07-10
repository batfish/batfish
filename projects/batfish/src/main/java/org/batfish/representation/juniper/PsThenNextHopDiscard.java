package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.expr.DiscardNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

/** A {@link Statement} that sets the next hop to discard. */
public final class PsThenNextHopDiscard extends PsThen {

  public static final PsThenNextHopDiscard INSTANCE = new PsThenNextHopDiscard();

  private PsThenNextHopDiscard() {}

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings w) {
    statements.add(new SetNextHop(DiscardNextHop.INSTANCE));
  }
}
