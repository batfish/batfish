package org.batfish.representation.cisco;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Cisco IOS device-tracking policy configuration. */
public class DeviceTrackingPolicy implements Serializable {

  private @Nullable Integer _ipv6PerMacLimit;
  private final @Nonnull String _name;
  private @Nullable Boolean _protocolUdp;
  private @Nullable DeviceTrackingSecurityLevel _securityLevel;
  private @Nullable Boolean _trackingEnabled;

  public DeviceTrackingPolicy(String name) {
    _name = name;
  }

  public @Nullable Integer getIpv6PerMacLimit() {
    return _ipv6PerMacLimit;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getProtocolUdp() {
    return _protocolUdp;
  }

  public @Nullable DeviceTrackingSecurityLevel getSecurityLevel() {
    return _securityLevel;
  }

  public @Nullable Boolean getTrackingEnabled() {
    return _trackingEnabled;
  }

  public void setIpv6PerMacLimit(@Nullable Integer ipv6PerMacLimit) {
    _ipv6PerMacLimit = ipv6PerMacLimit;
  }

  public void setProtocolUdp(@Nullable Boolean protocolUdp) {
    _protocolUdp = protocolUdp;
  }

  public void setSecurityLevel(@Nullable DeviceTrackingSecurityLevel securityLevel) {
    _securityLevel = securityLevel;
  }

  public void setTrackingEnabled(@Nullable Boolean trackingEnabled) {
    _trackingEnabled = trackingEnabled;
  }
}
