package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.CumulusNcluConfiguration.DEFAULT_STATIC_ROUTE_ADMINISTRATIVE_DISTANCE;
import static org.batfish.representation.cumulus.CumulusNcluConfiguration.DEFAULT_STATIC_ROUTE_METRIC;
import static org.batfish.representation.cumulus.Interface.NULL_INTERFACE_NAME;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** A statically-configured route */
public class StaticRoute implements Serializable {

  private final @Nonnull Prefix _network;
  private final @Nullable Ip _nextHopIp;
  private final @Nullable String _nextHopInterface;

  public StaticRoute(Prefix network, @Nullable Ip nextHopIp, @Nullable String nextHopInterface) {
    assert nextHopInterface != null || nextHopIp != null; // grammar invariant
    _network = network;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
  }

  public @Nonnull Prefix getNetwork() {
    return _network;
  }

  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nullable
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StaticRoute)) {
      return false;
    }
    StaticRoute rhs = (StaticRoute) obj;
    return _network.equals(rhs._network)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _nextHopIp, _nextHopInterface);
  }

  /** Convert this static route to a VI static route */
  @Nonnull
  org.batfish.datamodel.StaticRoute convert() {
    return org.batfish.datamodel.StaticRoute.builder()
        .setAdmin(DEFAULT_STATIC_ROUTE_ADMINISTRATIVE_DISTANCE)
        .setMetric(DEFAULT_STATIC_ROUTE_METRIC)
        .setNetwork(_network)
        .setNextHopIp(_nextHopIp)
        .setNextHopInterface(
            // canonicalize null interface name if needed
            NULL_INTERFACE_NAME.equalsIgnoreCase(_nextHopInterface)
                ? org.batfish.datamodel.Interface.NULL_INTERFACE_NAME
                : _nextHopInterface)
        .build();
  }
}
