package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.AbstractRoute.checkAdmin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;

/**
 * Base builder for all route types that carry IPv4 reachability information (i.e., {@link
 * AbstractRoute}).
 */
public abstract class AbstractRouteBuilder<
    S extends AbstractRouteBuilder<S, T>, T extends AbstractRoute> {

  private int _admin;
  private long _metric;
  private @Nullable Prefix _network;
  protected @Nullable NextHop _nextHop;
  // It's fine for fields below to default to false/unset in the common case
  private boolean _nonForwarding;
  private boolean _nonRouting;
  private long _tag = Route.UNSET_ROUTE_TAG;

  public @Nonnull abstract T build();

  public final int getAdmin() {
    return _admin;
  }

  public S setAdmin(int admin) {
    // TODO: too many tests set -1 via Route#UNSET_ADMIN here, so we don't check lower bound.
    checkAdmin(admin, Integer.MIN_VALUE);
    _admin = admin;
    return getThis();
  }

  // To handle the class casting exception while returning S in chaining methods
  protected @Nonnull abstract S getThis();

  public final long getMetric() {
    return _metric;
  }

  public final @Nonnull S setMetric(long metric) {
    _metric = metric;
    return getThis();
  }

  public final @Nullable Prefix getNetwork() {
    return _network;
  }

  public final @Nonnull S setNetwork(@Nonnull Prefix network) {
    _network = network;
    return getThis();
  }

  public final @Nonnull Ip getNextHopIp() {
    if (_nextHop == null) {
      return Route.UNSET_ROUTE_NEXT_HOP_IP;
    }
    return LegacyNextHops.getNextHopIp(_nextHop).orElse(Route.UNSET_ROUTE_NEXT_HOP_IP);
  }

  public final @Nonnull S setNextHopIp(@Nullable Ip nextHopIp) {
    if (nextHopIp == null) {
      _nextHop = null;
      return getThis();
    }
    if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
      _nextHop = NextHopDiscard.instance();
      return getThis();
    }
    if (_nextHop instanceof NextHopInterface) {
      // Merge NH INT + NH IP
      _nextHop = NextHopInterface.of(((NextHopInterface) _nextHop).getInterfaceName(), nextHopIp);
    } else {
      _nextHop = NextHopIp.of(nextHopIp);
    }
    return getThis();
  }

  public final S setNextHopInterface(@Nullable String iface) {
    if (iface == null || iface.equals(Route.UNSET_NEXT_HOP_INTERFACE)) {
      _nextHop = null;
      return getThis();
    }
    if (Interface.NULL_INTERFACE_NAME.equals(iface)) {
      _nextHop = NextHopDiscard.instance();
      return getThis();
    }
    if (_nextHop instanceof NextHopIp) {
      // Merge NH INT + NH IP
      _nextHop = NextHopInterface.of(iface, ((NextHopIp) _nextHop).getIp());
    } else if (_nextHop instanceof NextHopInterface) {
      _nextHop = NextHopInterface.of(iface, ((NextHopInterface) _nextHop).getIp());
    } else {
      _nextHop = NextHopInterface.of(iface);
    }
    return getThis();
  }

  public final String getNextHopInterface() {
    if (_nextHop == null) {
      return Route.UNSET_NEXT_HOP_INTERFACE;
    }
    return LegacyNextHops.getNextHopInterface(_nextHop).orElse(Route.UNSET_NEXT_HOP_INTERFACE);
  }

  /**
   * Set the next hop. Overrides any data set by {@link #setNextHopIp} or {@link
   * #setNextHopInterface(String)}}
   */
  public final S setNextHop(@Nonnull NextHop nextHop) {
    _nextHop = nextHop;
    return getThis();
  }

  public final S clearNextHop() {
    _nextHop = null;
    return getThis();
  }

  public final boolean getNonForwarding() {
    return _nonForwarding;
  }

  public @Nonnull S setNonForwarding(boolean nonForwarding) {
    _nonForwarding = nonForwarding;
    return getThis();
  }

  public final boolean getNonRouting() {
    return _nonRouting;
  }

  public @Nonnull S setNonRouting(boolean nonRouting) {
    _nonRouting = nonRouting;
    return getThis();
  }

  public final long getTag() {
    return _tag;
  }

  public final @Nonnull S setTag(@Nullable Long tag) {
    _tag = firstNonNull(tag, Route.UNSET_ROUTE_TAG);
    return getThis();
  }

  public final @Nonnull S setTag(long tag) {
    _tag = tag;
    return getThis();
  }
}
