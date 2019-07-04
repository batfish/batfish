package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum CiscoNxosStructureUsage implements StructureUsage {
  INTERFACE_CHANNEL_GROUP("interface channel-group"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VLAN("interface vlan"),
  INTERFACE_VRF_MEMBER("interface vrf member");

  private final @Nonnull String _description;

  private CiscoNxosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
