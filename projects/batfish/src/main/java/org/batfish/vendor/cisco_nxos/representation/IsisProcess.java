package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.isis.IsisLevel;

/**
 * Represents an IS-IS routing process (a single {@code router isis <tag>} instance) in Cisco NX-OS.
 */
public final class IsisProcess implements Serializable {

  public IsisProcess(String tag) {
    _tag = tag;
    // NX-OS defaults to level-1-2 when is-type is not specified.
    _level = IsisLevel.LEVEL_1_2;
  }

  public @Nonnull String getTag() {
    return _tag;
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

  private final @Nonnull String _tag;
  private @Nonnull IsisLevel _level;
  private @Nullable IsoAddress _netAddress;
}
