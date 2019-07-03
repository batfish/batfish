package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum CiscoNxosStructureType implements StructureType {
  INTERFACE("interface"),
  PORT_CHANNEL("port-channel"),
  VLAN("vlan");

  private final @Nonnull String _description;

  private CiscoNxosStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
