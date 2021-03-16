package org.batfish.representation.fortios;

import org.batfish.vendor.StructureUsage;

public enum FortiosStructureUsage implements StructureUsage {
  INTERFACE_SELF_REF("system interface"),
  POLICY_DSTADDR("firewall policy dstaddr"),
  POLICY_DSTINTF("firewall policy dstintf"),
  POLICY_SELF_REF("firewall policy"),
  POLICY_SERVICE("firewall policy service"),
  POLICY_SRCADDR("firewall policy srcaddr"),
  POLICY_SRCINTF("firewall policy srcintf"),
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
