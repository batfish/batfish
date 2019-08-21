package org.batfish.representation.cisco_nxos;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link RouteMapMatch} that matches routes based on protocol name. Arbitrary strings are
 * allowed.
 */
public final class RouteMapMatchSourceProtocol implements RouteMapMatch {

  private final @Nonnull String _sourceProtocol;

  public RouteMapMatchSourceProtocol(String sourceProtocol) {
    _sourceProtocol = sourceProtocol;
  }

  @Override
  public <T> T accept(RouteMapMatchVisitor<T> visitor) {
    return visitor.visitRouteMapMatchSourceProtocol(this);
  }

  public @Nullable String getSourceProtocol() {
    return _sourceProtocol;
  }

  public @Nonnull Optional<Collection<NxosRoutingProtocol>> toRoutingProtocols() {
    switch (_sourceProtocol) {
      case "connected":
        return Optional.of(ImmutableSet.of(NxosRoutingProtocol.DIRECT));
      case "static":
        return Optional.of(ImmutableSet.of(NxosRoutingProtocol.STATIC));
      default:
        return Optional.empty();
    }
  }
}
