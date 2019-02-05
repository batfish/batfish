package org.batfish.representation.f5_bigip;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.StructureUsage;

@ParametersAreNonnullByDefault
public enum F5BigipStructureUsage implements StructureUsage {
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  SELF_SELF_REFERENCE("self self-reference"),
  SELF_VLAN("self vlan"),
  VLAN_INTERFACE("vlan interface");

  private final @Nonnull String _description;

  private F5BigipStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
