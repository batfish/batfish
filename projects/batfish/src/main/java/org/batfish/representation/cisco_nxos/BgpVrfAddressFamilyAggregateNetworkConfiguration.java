package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Represents the BGP configuration for an aggregate network configured for an address family in a
 * VRF.
 *
 * <p>Configuration entered using the {@code aggregate-address} command at the CLI {@code
 * config-router-af} or {@code config-router-vrf-af} levels.
 */
public class BgpVrfAddressFamilyAggregateNetworkConfiguration implements Serializable {

  public BgpVrfAddressFamilyAggregateNetworkConfiguration() {
    _asSet = false;
    _summaryOnly = false;
  }

  @Nullable
  public String getAdvertiseMap() {
    return _advertiseMap;
  }

  public void setAdvertiseMap(@Nullable String advertiseMap) {
    _advertiseMap = advertiseMap;
  }

  public boolean getAsSet() {
    return _asSet;
  }

  public void setAsSet(boolean asSet) {
    _asSet = asSet;
  }

  @Nullable
  public String getAttributeMap() {
    return _attributeMap;
  }

  public void setAttributeMap(@Nullable String attributeMap) {
    _attributeMap = attributeMap;
  }

  public boolean getSummaryOnly() {
    return _summaryOnly;
  }

  public void setSummaryOnly(boolean summaryOnly) {
    _summaryOnly = summaryOnly;
  }

  @Nullable
  public String getSuppressMap() {
    return _suppressMap;
  }

  public void setSuppressMap(@Nullable String suppressMap) {
    _suppressMap = suppressMap;
  }

  private boolean _asSet;
  @Nullable private String _advertiseMap;
  @Nullable private String _attributeMap;
  private boolean _summaryOnly;
  @Nullable private String _suppressMap;
}
