package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * An SR-OS static route entry under {@code router "<name>" static-routes route <prefix> route-type
 * <unicast|multicast>}. The YANG list is keyed by {@code (ip-prefix, route-type)}; this models the
 * unicast IPv4 case.
 *
 * <p>The destination is reached either via a directly-connected {@code next-hop <ip>} or via a
 * {@code blackhole} (discard). Both the next-hop and the blackhole contexts carry an {@code
 * admin-state}: SR-OS does <em>not</em> install a static route into the RIB unless that admin-state
 * is {@code enable} (an absent or {@code disable} admin-state leaves the route configured but not
 * installed — confirmed on SR-SIM 26.3.R1). Once installed, the YANG defaults are {@code preference
 * 5} (Batfish admin distance) and {@code metric 1}.
 */
public final class StaticRoute implements Serializable {

  /** YANG default {@code preference} for a static route next-hop/blackhole. */
  public static final int DEFAULT_PREFERENCE = 5;

  /** YANG default {@code metric} for a static route next-hop/blackhole. */
  public static final int DEFAULT_METRIC = 1;

  public StaticRoute(Prefix prefix) {
    _prefix = prefix;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  /** The next-hop IP, or {@code null} if this is a blackhole route (or no next-hop is set). */
  public @Nullable Ip getNextHopIp() {
    return _nextHopIp;
  }

  public void setNextHopIp(@Nullable Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  /** Whether this route is a {@code blackhole} (discard) route. */
  public boolean getBlackhole() {
    return _blackhole;
  }

  public void setBlackhole(boolean blackhole) {
    _blackhole = blackhole;
  }

  /**
   * Whether the next-hop / blackhole context is {@code admin-state enable}. A route is installed
   * into the RIB only when this is true (SR-OS leaves a route with no/disabled admin-state
   * uninstalled).
   */
  public boolean getAdminStateEnable() {
    return _adminStateEnable;
  }

  public void setAdminStateEnable(boolean adminStateEnable) {
    _adminStateEnable = adminStateEnable;
  }

  /** The {@code preference} (Batfish admin distance); defaults to {@link #DEFAULT_PREFERENCE}. */
  public int getPreference() {
    return _preference;
  }

  public void setPreference(int preference) {
    _preference = preference;
  }

  /** The {@code metric}; defaults to {@link #DEFAULT_METRIC}. */
  public int getMetric() {
    return _metric;
  }

  public void setMetric(int metric) {
    _metric = metric;
  }

  private final @Nonnull Prefix _prefix;
  private @Nullable Ip _nextHopIp;
  private boolean _blackhole;
  private boolean _adminStateEnable;
  private int _preference = DEFAULT_PREFERENCE;
  private int _metric = DEFAULT_METRIC;
}
