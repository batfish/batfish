package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class IsisSettings implements Serializable {

  private Set<String> _exportPolicies;

  private final IsisLevelSettings _level1Settings;

  private final IsisLevelSettings _level2Settings;

  private boolean _noIpv4Routing;

  private boolean _overload;

  private Integer _overloadTimeout;

  private Double _referenceBandwidth;

  private boolean _trafficEngineeringCredibilityProtocolPreference;

  private boolean _trafficEngineeringShortcuts;

  public IsisSettings() {
    _exportPolicies = new LinkedHashSet<>();
    _level1Settings = new IsisLevelSettings();
    _level2Settings = new IsisLevelSettings();
  }

  public Set<String> getExportPolicies() {
    return _exportPolicies;
  }

  public IsisLevelSettings getLevel1Settings() {
    return _level1Settings;
  }

  public IsisLevelSettings getLevel2Settings() {
    return _level2Settings;
  }

  public boolean getNoIpv4Routing() {
    return _noIpv4Routing;
  }

  public boolean getOverload() {
    return _overload;
  }

  public Integer getOverloadTimeout() {
    return _overloadTimeout;
  }

  public Double getReferenceBandwidth() {
    return _referenceBandwidth;
  }

  public boolean getTrafficEngineeringCredibilityProtocolPreference() {
    return _trafficEngineeringCredibilityProtocolPreference;
  }

  public boolean getTrafficEngineeringShortcuts() {
    return _trafficEngineeringShortcuts;
  }

  public void setNoIpv4Routing(boolean noIpv4Routing) {
    _noIpv4Routing = noIpv4Routing;
  }

  public void setOverload(boolean overload) {
    _overload = overload;
  }

  public void setOverloadTimeout(int overloadTimeout) {
    _overloadTimeout = overloadTimeout;
  }

  public void setReferenceBandwidth(double referenceBandwidth) {
    _referenceBandwidth = referenceBandwidth;
  }

  public void setTrafficEngineeringCredibilityProtocolPreference(
      boolean trafficEngineeringCredibilityProtocolPreference) {
    _trafficEngineeringCredibilityProtocolPreference =
        trafficEngineeringCredibilityProtocolPreference;
  }

  public void setTrafficEngineeringShortcuts(boolean trafficEngineeringShortcuts) {
    _trafficEngineeringShortcuts = trafficEngineeringShortcuts;
  }
}
