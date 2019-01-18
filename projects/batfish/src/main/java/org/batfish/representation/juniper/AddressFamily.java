package org.batfish.representation.juniper;

/**
 * Juniper address families. Based on family-name options here:
 * https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-match-conditions-in-routing-policy-terms.html
 */
public enum AddressFamily {
  IPV4,
  IPV4_MULTICAST,
  IPV4_MULTICAST_VPN,
  IPV4_VPN,
  IPV6,
  IPV6_MULTICAST,
  IPV6_MULTICAST_VPN,
  IPV6_VPN,
  ISO,
  ROUTE_TARGET
}
