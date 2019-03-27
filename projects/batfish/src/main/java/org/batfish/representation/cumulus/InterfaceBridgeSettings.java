package org.batfish.representation.cumulus;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.RangeSet;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

/** Bridge settings for various interface types. */
public class InterfaceBridgeSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Integer _access;
  private @Nonnull IntegerSpace _vids;

  public InterfaceBridgeSettings() {
    _vids = IntegerSpace.EMPTY;
  }

  /** Returns access (untagged) VLAN ID if interface is in access mode, or else {@code null}. */
  public @Nullable Integer getAccess() {
    return _access;
  }

  /**
   * Returns trunk (tagged) VLAN IDs if interface is in trunk mode, or else an empty {@link
   * RangeSet}.
   */
  public @Nonnull IntegerSpace getVids() {
    return _vids;
  }

  /**
   * Sets access (untagged) VLAN ID.
   *
   * @throws IllegalStateException if trunk VIDs present
   */
  public void setAccess(@Nullable Integer access) {
    checkState(_vids.isEmpty(), "Cannot set access VLAN ID when trunk VIDs already present.");
    _access = access;
  }

  /**
   * Sets trunk (tagged) VLAN IDs.
   *
   * @throws IllegalStateException if access VLAN ID already set.
   */
  public void setVids(IntegerSpace vids) {
    checkState(_access == null, "Cannot set trunk VLAN IDs when access VLAN ID already set.");
    _vids = vids;
  }
}
