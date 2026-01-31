package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;

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

  public @Nonnull Optional<Collection<RoutingProtocol>> toRoutingProtocols() {
    switch (_sourceProtocol) {
      case "connected":
        return Optional.of(ImmutableSet.of(RoutingProtocol.CONNECTED));
      case "static":
        return Optional.of(ImmutableSet.of(RoutingProtocol.STATIC));
      default:
        return Optional.empty();
    }
  }
}
