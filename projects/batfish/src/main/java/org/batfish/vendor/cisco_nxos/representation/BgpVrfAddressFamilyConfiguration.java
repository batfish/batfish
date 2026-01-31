package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;

/**
 * Represents the BGP configuration for a single address family at the VRF level.
 *
 * <p>Configuration commands entered at the CLI {@code config-router-af} or {@code
 * config-router-vrf-af} levels.
 *
 * <p>Currently a parent with little functionality; see child classes such as {@link
 * BgpVrfIpv4AddressFamilyConfiguration}.
 */
public abstract class BgpVrfAddressFamilyConfiguration implements Serializable {
  public enum Type {
    IPV4_MULTICAST,
    IPV4_MVPN,
    IPV4_UNICAST,
    IPV6_MULTICAST,
    IPV6_MVPN,
    IPV6_UNICAST,
    LINK_STATE,
    L2VPN_EVPN,
    VPNV4,
    VPNV6,
  }
}
