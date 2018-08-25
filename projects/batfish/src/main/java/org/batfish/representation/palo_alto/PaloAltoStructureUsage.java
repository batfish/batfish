package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureUsage;

public enum PaloAltoStructureUsage implements StructureUsage {
  RULE_FROM_ZONE("rulebase security rules from"),
  RULE_SELF_REF("rulebase security rules"),
  RULE_TO_ZONE("rulebase security rules to"),
  RULEBASE_SERVICE("rulebase security rules service"),
  SERVICE_GROUP_MEMBER("service-group members"),
  VIRTUAL_ROUTER_INTERFACE("virtual-router interface"),
  ZONE_INTERFACE("zone network layer3");

  private final String _description;

  PaloAltoStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
