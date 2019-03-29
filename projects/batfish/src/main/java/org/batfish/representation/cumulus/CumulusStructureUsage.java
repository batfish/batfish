package org.batfish.representation.cumulus;

import org.batfish.vendor.StructureUsage;

public enum CumulusStructureUsage implements StructureUsage {
  BOND_SELF_REFERENCE("bond self-reference"),
  BOND_SLAVE("bond slave"),
  INTERFACE_CLAG_BACKUP_IP_VRF("interface clag backup-ip vrf"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VRF("interface vrf"),
  VRF_SELF_REFERENCE("vrf self-reference");

  private final String _description;

  private CumulusStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
