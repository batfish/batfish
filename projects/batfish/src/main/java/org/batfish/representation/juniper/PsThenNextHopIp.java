package org.batfish.representation.juniper;

import java.util.Collections;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.routing_policy.expr.IpNextHop;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;

public final class PsThenNextHopIp extends PsThen {

  private final Ip _nextHopIp;

  public PsThenNextHopIp(Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  @Override
  public void applyTo(
      List<Statement> statements,
      JuniperConfiguration juniperVendorConfiguration,
      Configuration c,
      Warnings warnings) {
    // todo: something with destination-vrf
    statements.add(new SetNextHop(new IpNextHop(Collections.singletonList(_nextHopIp))));
  }

  public Ip getNextHopIp() {
    return _nextHopIp;
  }
}
