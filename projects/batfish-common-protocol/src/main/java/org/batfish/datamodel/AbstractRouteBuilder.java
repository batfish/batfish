package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base builder for all route types that carry IPv4 reachability information (i.e., {@link
 * AbstractRoute}).
 */
public abstract class AbstractRouteBuilder<
    S extends AbstractRouteBuilder<S, T>, T extends AbstractRoute> {

  private int _admin;
  private long _metric;
  @Nullable private Prefix _network;
  @Nonnull private Ip _nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
  // It's fine for fields below to default to false/unset in the common case
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

  @Nonnull
  public final S setMetric(long metric) {
    _metric = metric;
    return getThis();
  }

  @Nullable
  public final Prefix getNetwork() {
    return _network;
  }

  @Nonnull
  public final S setNetwork(@Nonnull Prefix network) {
    _network = network;
    return getThis();
  }

  @Nonnull
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nonnull
  public final S setNextHopIp(@Nullable Ip nextHopIp) {
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    return getThis();
  }

  public final boolean getNonForwarding() {
    return _nonForwarding;
  }

  @Nonnull
  public final S setNonForwarding(boolean nonForwarding) {
    _nonForwarding = nonForwarding;
    return getThis();
  }

  public final boolean getNonRouting() {
    return _nonRouting;
  }

  @Nonnull
  public final S setNonRouting(boolean nonRouting) {
    _nonRouting = nonRouting;
    return getThis();
  }

  public final int getTag() {
    return _tag;
  }

  @Nonnull
  public final S setTag(@Nullable Integer tag) {
    _tag = firstNonNull(tag, Route.UNSET_ROUTE_TAG);
    return getThis();
  }
}
