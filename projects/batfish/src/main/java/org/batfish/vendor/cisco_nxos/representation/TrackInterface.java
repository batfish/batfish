package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents interface tracking configuration. */
@ParametersAreNonnullByDefault
public class TrackInterface implements Track {
  public enum Mode {
    LINE_PROTOCOL,
    IP_ROUTING,
    IPV6_ROUTING
  }

  public TrackInterface(String iface, Mode mode) {
    _interface = iface;
    _mode = mode;
  }

  public @Nonnull String getInterface() {
    return _interface;
  }

  public @Nonnull Mode getMode() {
    return _mode;
  }

  private final @Nonnull String _interface;
  private final @Nonnull Mode _mode;
}
