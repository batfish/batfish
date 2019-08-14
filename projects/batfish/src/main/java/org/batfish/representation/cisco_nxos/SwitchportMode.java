package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;

public enum SwitchportMode {
  ACCESS,
  DOT1Q_TUNNEL,
  FEX_FABRIC,
  MONITOR,
  NONE,
  TRUNK;

  public @Nonnull org.batfish.datamodel.SwitchportMode toSwitchportMode() {
    switch (this) {
      case ACCESS:
        return org.batfish.datamodel.SwitchportMode.ACCESS;
      case DOT1Q_TUNNEL:
        return org.batfish.datamodel.SwitchportMode.DOT1Q_TUNNEL;
      case FEX_FABRIC:
        return org.batfish.datamodel.SwitchportMode.FEX_FABRIC;
      case MONITOR:
        return org.batfish.datamodel.SwitchportMode.MONITOR;
      case NONE:
        return org.batfish.datamodel.SwitchportMode.NONE;
      case TRUNK:
        return org.batfish.datamodel.SwitchportMode.TRUNK;
      default:
        // should never happen
        throw new IllegalArgumentException(String.format("Unsupported switchport mode: %s", this));
    }
  }
}
