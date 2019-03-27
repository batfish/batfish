package org.batfish.representation.cumulus;

import org.batfish.vendor.StructureUsage;

public enum CumulusNcluStructureUsage implements StructureUsage {
  BOND_SLAVE("bond slave");

  private final String _description;

  private CumulusNcluStructureUsage(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
