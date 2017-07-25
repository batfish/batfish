package org.batfish.datamodel;

public enum InterfaceType {
  AGGREGATED,
  LOOPBACK,
  NULL,
  PHYSICAL,
  REDUNDANT,
  TUNNEL,
  UNKNOWN, // for use as sentinel value
  VLAN,
  VPN,
}
