package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

/** Redist profile's filter */
public final class RedistProfileFilter implements Serializable {
  public RedistProfileFilter() {
    _routingProtocols = EnumSet.noneOf(RoutingProtocol.class);
    _destinationPrefixes = new HashSet<>();
  }

  public @Nonnull Set<RoutingProtocol> getRoutingProtocols() {
    return _routingProtocols;
  }

  public @Nonnull Set<Prefix> getDestinationPrefixes() {
    return _destinationPrefixes;
  }

  private final @Nonnull Set<RoutingProtocol> _routingProtocols;
  private final @Nonnull Set<Prefix> _destinationPrefixes;
}
