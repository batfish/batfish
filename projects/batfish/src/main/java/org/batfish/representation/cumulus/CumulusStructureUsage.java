package org.batfish.representation.cumulus;

import org.batfish.vendor.StructureUsage;

public enum CumulusStructureUsage implements StructureUsage {
  BOND_SELF_REFERENCE("bond self-reference"),
  BOND_SLAVE("bond slave");

  private final String _description;

  private CumulusStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
