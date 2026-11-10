package org.batfish.vendor.rtx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** A VRRP group configured on an interface by {@code ip <interface> vrrp}. */
public final class RtxVrrpGroup implements Serializable {

  /** Default priority when the {@code priority=} parameter is omitted. */
  public static final int DEFAULT_PRIORITY = 100;

  /** Default preempt mode when the {@code preempt=} parameter is omitted. */
  public static final boolean DEFAULT_PREEMPT = true;

  private final int _vrid;
  private final @Nonnull Ip _virtualAddress;
  private int _priority;
  private boolean _preempt;

  public RtxVrrpGroup(int vrid, Ip virtualAddress) {
    _vrid = vrid;
    _virtualAddress = virtualAddress;
    _priority = DEFAULT_PRIORITY;
    _preempt = DEFAULT_PREEMPT;
  }

  public int getVrid() {
    return _vrid;
  }

  public @Nonnull Ip getVirtualAddress() {
    return _virtualAddress;
  }

  public int getPriority() {
    return _priority;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }

  public boolean getPreempt() {
    return _preempt;
  }

  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }
}
