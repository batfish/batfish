package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Represents the VRF-specific EIGRP configuration for an EIGRP process in Cisco NX-OS.
 *
 * <p>Configuration commands entered at the {@code config-router} level that can also be run at the
 * {@code config-router-vrf} level.
 */
public final class EigrpVrfConfiguration implements Serializable {

  public EigrpVrfConfiguration() {
    _vrfIpv4AddressFamilyConfiguration = new EigrpVrfIpv4AddressFamilyConfiguration();
  }

  public @Nullable Integer getAsn() {
    return _asn;
  }

  public void setAsn(@Nullable Integer asn) {
    _asn = asn;
  }

  public @Nullable Integer getDistanceInternal() {
    return _distanceInternal;
  }

  public void setDistanceInternal(@Nullable Integer distanceInternal) {
    _distanceInternal = distanceInternal;
  }

  public @Nullable Integer getDistanceExternal() {
    return _distanceExternal;
  }

  public void setDistanceExternal(@Nullable Integer distanceExternal) {
    _distanceExternal = distanceExternal;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  /**
   * On NX-OS, you can enter many of the configurations for the IPv4 Unicast address family directly
   * in the VRF or in the {@code address-family ipv4 unicast} subsection. The rules about which
   * combinations are allowed is ugly, and we didn't handle it yet.
   *
   * <p>This is for commands entered at {@code config-router-af} or {@code * config-router-vrf-af}.
   *
   * @see #getVrfIpv4AddressFamily() for commands entered at {@code config-router} or {@code
   *     config-router-vrf}.
   */
  public @Nullable EigrpVrfIpv4AddressFamilyConfiguration getV4AddressFamily() {
    return _v4AddressFamily;
  }

  public @Nonnull EigrpVrfIpv4AddressFamilyConfiguration getOrCreateV4AddressFamily() {
    if (_v4AddressFamily == null) {
      _v4AddressFamily = new EigrpVrfIpv4AddressFamilyConfiguration();
    }
    return _v4AddressFamily;
  }

  public @Nullable EigrpVrfIpv6AddressFamilyConfiguration getV6AddressFamily() {
    return _v6AddressFamily;
  }

  public @Nonnull EigrpVrfIpv6AddressFamilyConfiguration getOrCreateV6AddressFamily() {
    if (_v6AddressFamily == null) {
      _v6AddressFamily = new EigrpVrfIpv6AddressFamilyConfiguration();
    }
    return _v6AddressFamily;
  }

  /**
   * On NX-OS, you can enter many of the configurations for the IPv4 Unicast address family directly
   * in the VRF or in the {@code address-family ipv4 unicast} subsection. The rules about which
   * combinations are allowed is ugly, and we didn't handle it yet.
   *
   * <p>This is for commands entered at {@code config-router} or {@code config-router-vrf}.
   *
   * @see #getV4AddressFamily() for commands entered at {@code config-router-af} or {@code
   *     config-router-vrf-af}.
   */
  public @Nonnull EigrpVrfIpv4AddressFamilyConfiguration getVrfIpv4AddressFamily() {
    return _vrfIpv4AddressFamilyConfiguration;
  }

  ///////////////////////////////////
  // Private implementation details
  ///////////////////////////////////

  private @Nullable Integer _asn;
  private @Nullable Integer _distanceInternal;
  private @Nullable Integer _distanceExternal;
  private @Nullable Ip _routerId;
  private @Nullable EigrpVrfIpv4AddressFamilyConfiguration _v4AddressFamily;
  private @Nullable EigrpVrfIpv6AddressFamilyConfiguration _v6AddressFamily;
  private final @Nonnull EigrpVrfIpv4AddressFamilyConfiguration _vrfIpv4AddressFamilyConfiguration;
}
