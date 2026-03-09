package org.batfish.representation.fortios;

import org.batfish.vendor.StructureType;

public enum FortiosStructureType implements StructureType {
  ACCESS_LIST("access-list"),
  ACCESS_LIST_OR_PREFIX_LIST("access-list or prefix-list"),
  PREFIX_LIST("prefix-list"),
  ADDRESS("address"),
  ADDRGRP("addrgrp"),
  ADDRESS_OR_ADDRGRP("address or addrgrp"),
  INTERFACE("interface"),
  IPPOOL("ippool"),
  INTERFACE_OR_ZONE("interface or zone"),
  POLICY("policy"),
  ROUTE_MAP("route-map"),
  SERVICE_GROUP("service group"),
  SERVICE_CUSTOM("service custom"),
  SERVICE_CUSTOM_OR_SERVICE_GROUP("service custom or service group"),
  ZONE("zone");

  private final String _description;

  FortiosStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
