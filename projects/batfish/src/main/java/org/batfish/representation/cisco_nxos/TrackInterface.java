package org.batfish.representation.cisco_nxos;

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

  @Nonnull
  public String getInterface() {
    return _interface;
  }

  @Nonnull
  public Mode getMode() {
    return _mode;
  }

  @Nonnull private final String _interface;
  @Nonnull private final Mode _mode;
}
