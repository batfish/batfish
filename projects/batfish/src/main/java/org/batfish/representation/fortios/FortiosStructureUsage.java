package org.batfish.representation.fortios;

import org.batfish.vendor.StructureUsage;

public enum FortiosStructureUsage implements StructureUsage {
  ADDRGRP_EXCLUDE_MEMBER("addrgrp exclude-member"),
  ADDRGRP_MEMBER("addrgrp member"),
  BGP_UPDATE_SOURCE_INTERFACE("bgp update-source"),
  INTERFACE_SELF_REF("system interface"),
  /** VLAN interface's parent interface */
  INTERFACE_INTERFACE("system interface set interface"),
  POLICY_CLONE("firewall policy clone"),
  POLICY_DSTADDR("firewall policy dstaddr"),
  POLICY_DSTINTF("firewall policy dstintf"),
  POLICY_SELF_REF("firewall policy"),
  POLICY_DELETE("firewall policy delete"),
  POLICY_MOVE("firewall policy move"),
  POLICY_MOVE_PIVOT("firewall policy move before or after"),
  POLICY_SERVICE("firewall policy service"),
  POLICY_SRCADDR("firewall policy srcaddr"),
  POLICY_SRCINTF("firewall policy srcintf"),
  ROUTE_MAP_MATCH_IP_ADDRESS("route-map match-ip-address"),
  STATIC_ROUTE_DEVICE("router static device"),
  SERVICE_GROUP_MEMBER("service group member"),
  ZONE_INTERFACE("system zone interface"),
  ZONE_SELF_REF("system zone");

  private final String _description;

  FortiosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
