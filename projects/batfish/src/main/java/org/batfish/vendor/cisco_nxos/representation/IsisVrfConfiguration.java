package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevel;

/**
 * Represents the VRF-specific IS-IS configuration for an IS-IS process in Cisco NX-OS.
 *
 * <p>Holds the {@code net} and {@code is-type} settings, which are configurable both at the {@code
 * config-router} level (default VRF) and inside a {@code vrf} block.
 */
public final class IsisVrfConfiguration implements Serializable {

  public IsisVrfConfiguration() {
    // NX-OS defaults to level-1-2 when is-type is not specified.
    _level = IsisLevel.LEVEL_1_2;
  }

  public @Nonnull IsisLevel getLevel() {
    return _level;
  }

  public void setLevel(@Nonnull IsisLevel level) {
    _level = level;
  }

  public @Nullable IsoAddress getNetAddress() {
    return _netAddress;
  }

  public void setNetAddress(@Nullable IsoAddress netAddress) {
    _netAddress = netAddress;
  }

  private @Nonnull IsisLevel _level;
  private @Nullable IsoAddress _netAddress;
}
