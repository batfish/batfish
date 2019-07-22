package org.batfish.representation.cisco_nxos;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;

/**
 * Represents the BGP configuration for a single address family at the VRF level.
 *
 * <p>Configuration commands entered at the CLI {@code config-router-af} or {@code
 * config-router-vrf-af} levels.
 */
public abstract class BgpVrfIpAddressFamilyConfiguration extends BgpVrfAddressFamilyConfiguration {

  public BgpVrfIpAddressFamilyConfiguration() {
    _clientToClientReflection = false; // disabled by default
    _defaultMetric = null; // there is no default, and metric must be set to redistribute routes
    _defaultInformationOriginate = false; // disabled by default
    _distanceEbgp = 20; // documented under "address-family (BGP router)" and NX-OS CLI
    _distanceIbgp = 200; // documented under "address-family (BGP router)" and NX-OS CLI
    _distanceLocal = 220; // documented under "address-family (BGP router)" and NX-OS CLI
    _maximumPathsEbgp = 1; // multipath disabled by default
    _maximumPathsIbgp = 1; // multipath disabled by default
    _redistributionPolicies = new HashMap<>();
    _suppressInactive = false; // inactive routes not suppressed by default
  }

  public boolean getClientToClientReflection() {
    return _clientToClientReflection;
  }

  public void setClientToClientReflection(boolean clientToClientReflection) {
    this._clientToClientReflection = clientToClientReflection;
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public void setDefaultInformationOriginate(boolean defaultInformationOriginate) {
    this._defaultInformationOriginate = defaultInformationOriginate;
  }

  @Nullable
  public Long getDefaultMetric() {
    return _defaultMetric;
  }

  public void setDefaultMetric(@Nullable Long defaultMetric) {
    this._defaultMetric = defaultMetric;
  }

  public int getDistanceEbgp() {
    return _distanceEbgp;
  }

  public void setDistanceEbgp(int distanceEbgp) {
    this._distanceEbgp = distanceEbgp;
  }

  public int getDistanceIbgp() {
    return _distanceIbgp;
  }

  public void setDistanceIbgp(int distanceIbgp) {
    this._distanceIbgp = distanceIbgp;
  }

  public int getDistanceLocal() {
    return _distanceLocal;
  }

  public void setDistanceLocal(int distanceLocal) {
    this._distanceLocal = distanceLocal;
  }

  public int getMaximumPathsEbgp() {
    return _maximumPathsEbgp;
  }

  public void setMaximumPathsEbgp(int maximumPathsEbgp) {
    this._maximumPathsEbgp = maximumPathsEbgp;
  }

  public int getMaximumPathsIbgp() {
    return _maximumPathsIbgp;
  }

  public void setMaximumPathsIbgp(int maximumPathsIbgp) {
    this._maximumPathsIbgp = maximumPathsIbgp;
  }

  @Nullable
  public BgpRedistributionPolicy getRedistributionPolicy(RoutingProtocol protocol) {
    return _redistributionPolicies.get(protocol);
  }

  public void setRedistributionPolicy(
      RoutingProtocol protocol, String routeMap, @Nullable String sourceTag) {
    BgpRedistributionPolicy policy = new BgpRedistributionPolicy(routeMap);
    policy.setSourceTag(sourceTag);
    _redistributionPolicies.put(protocol, policy);
  }

  public boolean getSuppressInactive() {
    return _suppressInactive;
  }

  public void setSuppressInactive(boolean suppressInactive) {
    _suppressInactive = suppressInactive;
  }

  private boolean _clientToClientReflection;
  @Nullable private Long _defaultMetric;
  private boolean _defaultInformationOriginate;
  private int _distanceEbgp;
  private int _distanceIbgp;
  private int _distanceLocal;
  private int _maximumPathsEbgp;
  private int _maximumPathsIbgp;
  private final Map<RoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;
  private boolean _suppressInactive;
}
