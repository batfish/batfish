package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_STATIC_ROUTE_ADMINISTRATIVE_DISTANCE;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_STATIC_ROUTE_METRIC;
import static org.batfish.representation.cumulus.Interface.NULL_INTERFACE_PATTERN;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/** A statically-configured route */
public class StaticRoute implements Serializable {

  private final @Nonnull Prefix _network;
  private final @Nullable Ip _nextHopIp;
  private final @Nullable String _nextHopInterface;
  private final @Nullable Integer _distance;

  public StaticRoute(
      Prefix network,
      @Nullable Ip nextHopIp,
      @Nullable String nextHopInterface,
      @Nullable Integer distance) {
    assert nextHopInterface != null || nextHopIp != null; // grammar invariant
    _network = network;
    _nextHopIp = nextHopIp;
    _nextHopInterface = nextHopInterface;
    _distance = distance;
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

  public @Nullable Integer getDistance() {
    return _distance;
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
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface)
        && Objects.equals(_distance, rhs._distance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _nextHopIp, _nextHopInterface, _distance);
  }

  /** Convert this static route to a VI static route */
  @Nonnull
  org.batfish.datamodel.StaticRoute convert() {
    String nhInt = _nextHopInterface != null ? canonicalizeInterfaceName(_nextHopInterface) : null;
    return org.batfish.datamodel.StaticRoute.builder()
        .setAdmin(_distance != null ? _distance : DEFAULT_STATIC_ROUTE_ADMINISTRATIVE_DISTANCE)
        .setMetric(DEFAULT_STATIC_ROUTE_METRIC)
        .setNetwork(_network)
        .setNextHop(
            org.batfish.datamodel.Interface.NULL_INTERFACE_NAME.equals(nhInt)
                ? NextHopDiscard.instance()
                : NextHop.legacyConverter(nhInt, _nextHopIp))
        .build();
  }

  /**
   * Canonicalizes the many FRR discard interface names into just the standard one supported by the
   * Batfish VI model - "null_interface"
   */
  private String canonicalizeInterfaceName(String nextHopInterface) {
    Matcher matcher = NULL_INTERFACE_PATTERN.matcher(nextHopInterface);
    if (matcher.matches()) {
      return org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
    }
    return nextHopInterface;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("_network", _network)
        .add("_nextHopIp", _nextHopIp)
        .add("_nextHopInterface", _nextHopInterface)
        .add("_distance", _distance)
        .toString();
  }
}
