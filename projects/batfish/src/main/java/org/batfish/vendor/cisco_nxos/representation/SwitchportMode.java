package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nonnull;

public enum SwitchportMode {
  ACCESS,
  DOT1Q_TUNNEL,
  FEX_FABRIC,
  MONITOR,
  NONE,
  TRUNK;

  public @Nonnull org.batfish.datamodel.SwitchportMode toSwitchportMode() {
    return switch (this) {
      case ACCESS -> org.batfish.datamodel.SwitchportMode.ACCESS;
      case DOT1Q_TUNNEL -> org.batfish.datamodel.SwitchportMode.DOT1Q_TUNNEL;
      case FEX_FABRIC -> org.batfish.datamodel.SwitchportMode.FEX_FABRIC;
      case MONITOR -> org.batfish.datamodel.SwitchportMode.MONITOR;
      case NONE -> org.batfish.datamodel.SwitchportMode.NONE;
      case TRUNK -> org.batfish.datamodel.SwitchportMode.TRUNK;
    };
  }
}
