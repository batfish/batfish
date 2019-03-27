package org.batfish.representation.cumulus;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.RangeSet;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Bridge settings for various interface types. */
public class InterfaceBridgeSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Integer _access;
  private @Nonnull RangeSet<Integer> _vids;

  public InterfaceBridgeSettings() {
    _vids = ImmutableRangeSet.of();
  }

  /** Returns access (untagged) VLAN ID if interface is in access mode, or else {@code null}. */
  public @Nullable Integer getAccess() {
    return _access;
  }

  /**
   * Returns trunk (tagged) VLAN IDs if interface is in trunk mode, or else an empty {@link
   * RangeSet}.
   */
  public @Nonnull RangeSet<Integer> getVids() {
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
  public void setVids(RangeSet<Integer> vids) {
    checkState(_access == null, "Cannot set trunk VLAN IDs when access VLAN ID already set.");
    _vids = ImmutableRangeSet.copyOf(vids);
  }
}
