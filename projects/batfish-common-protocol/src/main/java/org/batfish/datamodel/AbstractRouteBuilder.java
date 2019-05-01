package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;

public abstract class AbstractRouteBuilder<
    S extends AbstractRouteBuilder<S, T>, T extends AbstractRoute> {

  private int _admin;
  private long _metric;
  private Prefix _network;
  private Ip _nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
  private boolean _nonForwarding;
  private boolean _nonRouting;
  private int _tag = Route.UNSET_ROUTE_TAG;

  @Nonnull
  public abstract T build();

  public final int getAdmin() {
    return _admin;
  }

  public final S setAdmin(int admin) {
    _admin = admin;
    return getThis();
  }

  // To handle the class casting exception while returning S in chaining methods
  @Nonnull
  protected abstract S getThis();

  public final long getMetric() {
    return _metric;
  }

  public final S setMetric(long metric) {
    _metric = metric;
    return getThis();
  }

  public final Prefix getNetwork() {
    return _network;
  }

  public final S setNetwork(Prefix network) {
    if (network == null) {
      throw new BatfishException("Cannot construct AbstractRoute with null network");
    }
    _network = network;
    return getThis();
  }

  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  public final S setNextHopIp(@Nullable Ip nextHopIp) {
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    return getThis();
  }

  public final boolean getNonForwarding() {
    return _nonForwarding;
  }

  public final S setNonForwarding(boolean nonForwarding) {
    _nonForwarding = nonForwarding;
    return getThis();
  }

  public final boolean getNonRouting() {
    return _nonRouting;
  }

  public final S setNonRouting(boolean nonRouting) {
    _nonRouting = nonRouting;
    return getThis();
  }

  public int getTag() {
    return _tag;
  }

  public final S setTag(@Nullable Integer tag) {
    _tag = firstNonNull(tag, Route.UNSET_ROUTE_TAG);
    return getThis();
  }
}
