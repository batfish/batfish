package org.batfish.vendor.cool_nos;

import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/**
 * Indicates traffic matching a route should be forwarded to a gateway with a given IPv4 address.
 */
public final class NextHopGateway implements NextHop {

  public NextHopGateway(Ip ip) {
    _ip = ip;
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopGateway(this);
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  private final @Nonnull Ip _ip;
}
