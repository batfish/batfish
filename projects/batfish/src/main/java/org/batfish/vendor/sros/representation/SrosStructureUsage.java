package org.batfish.vendor.sros.representation;

import org.batfish.vendor.StructureUsage;

/** Ways a named structure can be referenced in a Nokia SR-OS configuration. */
public enum SrosStructureUsage implements StructureUsage {
  BGP_GROUP_IMPORT_POLICY("bgp group import policy"),
  BGP_GROUP_EXPORT_POLICY("bgp group export policy"),
  BGP_NEIGHBOR_GROUP("bgp neighbor group"),
  BGP_NEIGHBOR_IMPORT_POLICY("bgp neighbor import policy"),
  BGP_NEIGHBOR_EXPORT_POLICY("bgp neighbor export policy"),
  POLICY_STATEMENT_FROM_PREFIX_LIST("policy-statement entry from prefix-list"),
  POLICY_STATEMENT_ACTION_COMMUNITY("policy-statement entry action community add");

  private final String _description;

  SrosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
