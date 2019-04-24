package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.datamodel.RoutingProtocol;

public enum CumulusRoutingProtocol {
  CONNECTED,
  STATIC;

  @Nonnull
  public static RoutingProtocol toViProtocol(@Nonnull CumulusRoutingProtocol protocol) {
    switch (protocol) {
      case CONNECTED:
        return RoutingProtocol.CONNECTED;
      case STATIC:
        return RoutingProtocol.STATIC;
      default:
        throw new IllegalArgumentException(
            String.format("Unrecognized Cumulus routing protocol: %s", protocol));
    }
  }
}
