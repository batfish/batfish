package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the BGP configuration for IPv4 or IPv6 address families at the VRF level.
 *
 * <p>Child classes such as {@link BgpVrfIpv4AddressFamilyConfiguration} contain the v4/v6-specific
 * code.
 */
public abstract class BgpVrfIpAddressFamilyConfiguration extends BgpVrfAddressFamilyConfiguration {
  // documented under "address-family (BGP router)" and NX-OS CLI
  public static final int DEFAULT_DISTANCE_EBGP = 20;
  // documented under "address-family (BGP router)" and NX-OS CLI
  public static final int DEFAULT_DISTANCE_IBGP = 200;
  // documented under "address-family (BGP router)" and NX-OS CLI
  public static final int DEFAULT_DISTANCE_LOCAL_BGP = 220;

  public BgpVrfIpAddressFamilyConfiguration() {
    _clientToClientReflection = true; // enabled by default
    _defaultMetric = null; // there is no default, and metric must be set to redistribute routes
    _defaultInformationOriginate = false; // disabled by default
    _distanceEbgp = DEFAULT_DISTANCE_EBGP;
    _distanceIbgp = DEFAULT_DISTANCE_IBGP;
    _distanceLocal = DEFAULT_DISTANCE_LOCAL_BGP;
    _maximumPathsEbgp = 1; // multipath disabled by default
    _maximumPathsIbgp = 1; // multipath disabled by default
    _redistributionPolicies = new HashMap<>();
    _suppressInactive = false; // inactive routes not suppressed by default
  }

  public final boolean getClientToClientReflection() {
    return _clientToClientReflection;
  }

  public final void setClientToClientReflection(boolean clientToClientReflection) {
    _clientToClientReflection = clientToClientReflection;
  }

  public final boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public final void setDefaultInformationOriginate(boolean defaultInformationOriginate) {
    _defaultInformationOriginate = defaultInformationOriginate;
  }

  public final @Nullable Long getDefaultMetric() {
    return _defaultMetric;
  }

  public final void setDefaultMetric(@Nullable Long defaultMetric) {
    _defaultMetric = defaultMetric;
  }

  public final int getDistanceEbgp() {
    return _distanceEbgp;
  }

  public final void setDistanceEbgp(int distanceEbgp) {
    _distanceEbgp = distanceEbgp;
  }

  public final int getDistanceIbgp() {
    return _distanceIbgp;
  }

  public final void setDistanceIbgp(int distanceIbgp) {
    _distanceIbgp = distanceIbgp;
  }

  public final int getDistanceLocal() {
    return _distanceLocal;
  }

  public final void setDistanceLocal(int distanceLocal) {
    _distanceLocal = distanceLocal;
  }

  public final int getMaximumPathsEbgp() {
    return _maximumPathsEbgp;
  }

  public final void setMaximumPathsEbgp(int maximumPathsEbgp) {
    _maximumPathsEbgp = maximumPathsEbgp;
  }

  public final int getMaximumPathsIbgp() {
    return _maximumPathsIbgp;
  }

  public final void setMaximumPathsIbgp(int maximumPathsIbgp) {
    _maximumPathsIbgp = maximumPathsIbgp;
  }

  /**
   * Route-map used to determine whether a BGP RIB route is resolvable, applied against the main
   * RIB's direct resolvers for the BGP RIB route's next hop.
   *
   * <p>See <a
   * href="https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/6-x/unicast/configuration/guide/l3_cli_nxos/l3_advbgp.html">NXOS
   * documentation</a> (search for {@code nexthop route-map})
   */
  public @Nullable String getNexthopRouteMap() {
    return _nexthopRouteMap;
  }

  public void setNexthopRouteMap(@Nullable String nexthopRouteMap) {
    _nexthopRouteMap = nexthopRouteMap;
  }

  /** Return all redistribution policies. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies() {
    return ImmutableList.copyOf(_redistributionPolicies.values());
  }

  /** Return all redistribution policies for the given protocol. */
  public final @Nonnull List<RedistributionPolicy> getRedistributionPolicies(
      NxosRoutingProtocol protocol) {
    return _redistributionPolicies.values().stream()
        .filter(rp -> rp.getInstance().getProtocol() == protocol)
        .collect(ImmutableList.toImmutableList());
  }

  /** Return the redistribution policy for the given instance, if one has been configured. */
  public final @Nullable RedistributionPolicy getRedistributionPolicy(
      RoutingProtocolInstance protocol) {
    return _redistributionPolicies.get(protocol);
  }

  /** Set the redistribution policy for the given instance. */
  public final void setRedistributionPolicy(RoutingProtocolInstance instance, String routeMap) {
    _redistributionPolicies.put(instance, new RedistributionPolicy(instance, routeMap));
  }

  public final boolean getSuppressInactive() {
    return _suppressInactive;
  }

  public final void setSuppressInactive(boolean suppressInactive) {
    _suppressInactive = suppressInactive;
  }

  private boolean _clientToClientReflection;
  private @Nullable Long _defaultMetric;
  private boolean _defaultInformationOriginate;
  private int _distanceEbgp;
  private int _distanceIbgp;
  private int _distanceLocal;
  private int _maximumPathsEbgp;
  private int _maximumPathsIbgp;
  private @Nullable String _nexthopRouteMap;

  private final Map<RoutingProtocolInstance, RedistributionPolicy> _redistributionPolicies;
  private boolean _suppressInactive;
}
