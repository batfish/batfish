package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum CiscoNxosStructureUsage implements StructureUsage {
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VLAN("interface vlan");

  private final @Nonnull String _description;

  private CiscoNxosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
