package org.batfish.representation.cumulus_nclu;

import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.IntegerSpace;

/** Bridge settings for various interface types. */
public class InterfaceBridgeSettings implements Serializable {

  private @Nullable Integer _access;
  private @Nullable Integer _pvid;
  private @Nonnull IntegerSpace _vids;

  public InterfaceBridgeSettings() {
    _vids = IntegerSpace.EMPTY;
  }

  /** Returns access (untagged) VLAN ID if interface is in access mode, or else {@code null}. */
  public @Nullable Integer getAccess() {
    return _access;
  }

  /**
   * Native trunk (untagged) VLAN ID if interface is in trunk mode. If {@link #getAccess} and {@link
   * #getPvid} are both {@code null}, and interface is in {@link Bridge#getPorts}, then value from
   * {@link Bridge#getPvid()} should be used.
   */
  public Integer getPvid() {
    return _pvid;
  }

  /**
   * Returns trunk (tagged) VLAN IDs if interface is in trunk mode, or else an empty {@link
   * IntegerSpace}. If {@link #getAccess} is {@code null}, {@link #getVids} is empty, and interface
   * is in {@link Bridge#getPorts}, then value from {@link Bridge#getVids()} should be used.
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
    checkState(_pvid == null, "Cannot set access VLAN ID when pvid (native VLAN) already set.");
    checkState(_vids.isEmpty(), "Cannot set access VLAN ID when trunk VIDs already present.");
    _access = access;
  }

  /**
   * Sets native trunk (untagged) VLAN ID.
   *
   * @throws IllegalStateException if access VLAN ID already set.
   */
  public void setPvid(Integer pvid) {
    checkState(_access == null, "Cannot set native VLAN ID when access VLAN ID already set.");
    _pvid = pvid;
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
