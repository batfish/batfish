package org.batfish.representation.palo_alto;

import org.batfish.vendor.StructureUsage;

public enum PaloAltoStructureUsage implements StructureUsage {
  RULEBASE_DESTINATION_ADDRESS("rulebase security rules destination"),
  RULEBASE_FROM_ZONE("rulebase security rules from"),
  RULEBASE_SOURCE_ADDRESS("rulebase security rules source"),
  RULEBASE_TO_ZONE("rulebase security rules to"),
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
