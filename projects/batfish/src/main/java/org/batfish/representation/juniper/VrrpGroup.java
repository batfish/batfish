package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/** Configuration for a VRID on a router. */
@ParametersAreNonnullByDefault
public final class VrrpGroup implements Serializable {

  public VrrpGroup(ConcreteInterfaceAddress sourceAddress) {
    _sourceAddress = sourceAddress;
  }

  public boolean getPreempt() {
    return _preempt;
  }

  public void setPreempt(boolean preempt) {
    _preempt = preempt;
  }

  public int getPriority() {
    return _priority;
  }

  public void setPriority(int priority) {
    _priority = priority;
  }

  public @Nonnull ConcreteInterfaceAddress getSourceAddress() {
    return _sourceAddress;
  }

  public @Nullable Ip getVirtualAddress() {
    return _virtualAddress;
  }

  public void setVirtualAddress(@Nullable Ip virtualAddress) {
    _virtualAddress = virtualAddress;
  }

  private boolean _preempt;
  private int _priority;
  private final @Nonnull ConcreteInterfaceAddress _sourceAddress;
  private @Nullable Ip _virtualAddress;
}
