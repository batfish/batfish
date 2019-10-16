package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

/** Redist profile's filter */
public final class RedistProfileFilter implements Serializable {
  public RedistProfileFilter() {
    _routingProtocols = new HashSet<>();
    _destinationPrefixes = new HashSet<>();
  }

  @Nonnull
  public Set<RoutingProtocol> getRoutingProtocols() {
    return _routingProtocols;
  }

  @Nonnull
  public Set<Prefix> getDestinationPrefixes() {
    return _destinationPrefixes;
  }

  private final @Nonnull Set<RoutingProtocol> _routingProtocols;
  private final @Nonnull Set<Prefix> _destinationPrefixes;
}
