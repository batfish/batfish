package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/** Configuration for a VRID on a router. */
@ParametersAreNonnullByDefault
public final class VrrpGroup implements Serializable {

  public VrrpGroup(ConcreteInterfaceAddress sourceAddress) {
    _virtualAddresses = ImmutableSet.of();
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

  public @Nonnull Set<Ip> getVirtualAddresses() {
    return _virtualAddresses;
  }

  public void addVirtualAddress(Ip virtualAddress) {
    if (_virtualAddresses.contains(virtualAddress)) {
      return;
    }
    _virtualAddresses =
        ImmutableSet.<Ip>builderWithExpectedSize(_virtualAddresses.size() + 1)
            .addAll(_virtualAddresses)
            .add(virtualAddress)
            .build();
  }

  private boolean _preempt;
  private int _priority;
  private final @Nonnull ConcreteInterfaceAddress _sourceAddress;
  private @Nonnull Set<Ip> _virtualAddresses;
}
