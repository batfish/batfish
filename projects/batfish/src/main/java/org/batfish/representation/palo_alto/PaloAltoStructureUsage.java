package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureUsage;

public enum PaloAltoStructureUsage implements StructureUsage {
  RULEBASE_DESTINATION_ADDRESS("rulebase security rules destination"),
  RULEBASE_SERVICE("rulebase security rules service"),
  RULEBASE_SOURCE_ADDRESS("rulebase security rules source"),
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
