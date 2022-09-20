package org.batfish.vendor.cool_nos;

import javax.annotation.Nonnull;

/** Indicates traffic matching a route should be forwarded out a given interface. */
public final class NextHopInterface implements NextHop {

  public NextHopInterface(String iface) {
    _interface = iface;
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopInterface(this);
  }

  public @Nonnull String getInterface() {
    return _interface;
  }

  private final @Nonnull String _interface;
}
