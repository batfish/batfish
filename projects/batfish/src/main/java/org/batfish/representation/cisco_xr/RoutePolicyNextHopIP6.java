package org.batfish.representation.cisco_xr;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.routing_policy.expr.NextHopExpr;

public class RoutePolicyNextHopIP6 extends RoutePolicyNextHop {

  private final @Nonnull Ip6 _address;

  public RoutePolicyNextHopIP6(@Nonnull Ip6 address) {
    _address = address;
  }

  public @Nonnull Ip6 getAddress() {
    return _address;
  }

  @Override
  public Optional<NextHopExpr> toNextHopExpr(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return Optional.empty();
  }
}
