package org.batfish.vendor.huawei.representation;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * Represents a static route on a Huawei VRP device.
 *
 * <p>This is a stub class for future static route implementation. It will store static route
 * configuration including destination network, next hop, and route preference.
 */
public class HuaweiStaticRoute implements Serializable {

  private static final long serialVersionUID = 1L;

  /** Destination network prefix */
  @Nonnull private Prefix _destination;

  /** Next hop IP address */
  @Nullable private Ip _nextHopIp;

  /** Output interface */
  @Nullable private String _nextHopInterface;

  /** Administrative distance/preference */
  private int _preference;

  /** Whether this is a default route */
  private boolean _defaultRoute;

  /** VRF name for this route */
  @Nullable private String _vrfName;

  public HuaweiStaticRoute(@Nonnull Prefix destination) {
    _destination = destination;
    _preference = 60; // Default preference for Huawei static routes
    _defaultRoute = false;
  }

  /**
   * Gets the destination network prefix.
   *
   * @return The destination prefix
   */
  @Nonnull
  public Prefix getDestination() {
    return _destination;
  }

  /**
   * Sets the destination network prefix.
   *
   * @param destination The destination prefix to set
   */
  public void setDestination(@Nonnull Prefix destination) {
    _destination = destination;
  }

  /**
   * Gets the next hop IP address.
   *
   * @return The next hop IP, or null if not set
   */
  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  /**
   * Sets the next hop IP address.
   *
   * @param nextHopIp The next hop IP to set
   */
  public void setNextHopIp(@Nullable Ip nextHopIp) {
    _nextHopIp = nextHopIp;
  }

  /**
   * Gets the next hop interface.
   *
   * @return The interface name, or null if not set
   */
  @Nullable
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  /**
   * Sets the next hop interface.
   *
   * @param nextHopInterface The interface name to set
   */
  public void setNextHopInterface(@Nullable String nextHopInterface) {
    _nextHopInterface = nextHopInterface;
  }

  /**
   * Gets the route preference.
   *
   * @return The preference value
   */
  public int getPreference() {
    return _preference;
  }

  /**
   * Sets the route preference.
   *
   * @param preference The preference value to set
   */
  public void setPreference(int preference) {
    _preference = preference;
  }

  /**
   * Checks if this is a default route.
   *
   * @return true if this is a default route, false otherwise
   */
  public boolean isDefaultRoute() {
    return _defaultRoute;
  }

  /**
   * Sets whether this is a default route.
   *
   * @param defaultRoute true if this is a default route, false otherwise
   */
  public void setDefaultRoute(boolean defaultRoute) {
    _defaultRoute = defaultRoute;
  }

  /**
   * Gets the VRF name for this route.
   *
   * @return The VRF name, or null if in default VRF
   */
  @Nullable
  public String getVrfName() {
    return _vrfName;
  }

  /**
   * Sets the VRF name for this route.
   *
   * @param vrfName The VRF name to set
   */
  public void setVrfName(@Nullable String vrfName) {
    _vrfName = vrfName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("destination", _destination)
        .add("nextHopIp", _nextHopIp)
        .add("nextHopInterface", _nextHopInterface)
        .add("preference", _preference)
        .add("defaultRoute", _defaultRoute)
        .add("vrfName", _vrfName)
        .toString();
  }
}
