package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Represents any interface with an IP address.
 *
 * <p>SVI/IRB/Vlan interfaces connect to the {@link DeviceBroadcastDomain} they are associated with.
 *
 * <p>Ethernet (sub)interfaces connect to the {@link PhysicalInterface} they are bound to. Even if
 * the configuration has an IP address directly assigned to Ethernet1, say, there will be both an
 * {@link L3Interface} and {@link PhysicalInterface} for Ethernet1.
 */
public final class L3Interface extends Node<L3Interface.Unit> {
  /** There is no data needed for {@link L3Interface}, but we can't have a non-null {@link Void} */
  public enum Unit {
    /** The only legal value. */
    VALUE
  }

  public L3Interface(NodeInterfacePair iface) {
    _iface = iface;
  }

  public @Nonnull NodeInterfacePair getIface() {
    return _iface;
  }

  public void originate(Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    // No need to check or add this to visited, we'll never come back here.

    domain.add(this);

    assert _sendToInterface == null || _sendToSwitch == null;
    if (_sendToInterface != null) {
      assert _sendToInterfaceEdge != null; // contract
      _sendToInterfaceEdge
          .traverse(Unit.VALUE)
          .ifPresent(tag -> _sendToInterface.transmit(tag, domain, visited));
    }
    if (_sendToSwitch != null) {
      assert _sendToSwitchEdge != null; // contract
      _sendToSwitchEdge
          .traverse(Unit.VALUE)
          .ifPresent(vlan -> _sendToSwitch.broadcast(vlan, domain, visited));
    }
  }

  public void reached(Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    domain.add(this);
  }

  public void sendDirectlyOutIface(PhysicalInterface iface, Edge<Unit, EthernetTag> edge) {
    checkState(
        _sendToInterface == null && _sendToSwitch == null,
        "Cannot connect an L3Interface to two places");
    _sendToInterface = iface;
    _sendToInterfaceEdge = edge;
  }

  public void sendThroughSwitch(DeviceBroadcastDomain sw, Edge<Unit, Integer> edge) {
    checkState(
        _sendToInterface == null && _sendToSwitch == null,
        "Cannot connect an L3Interface to two places");
    _sendToSwitch = sw;
    _sendToSwitchEdge = edge;
  }

  // Internal details
  private @Nullable PhysicalInterface _sendToInterface;
  private @Nullable Edge<Unit, EthernetTag> _sendToInterfaceEdge;
  private @Nullable DeviceBroadcastDomain _sendToSwitch;
  private @Nullable Edge<Unit, Integer> _sendToSwitchEdge;

  @VisibleForTesting
  @Nullable
  PhysicalInterface getSendToInterfaceForTesting() {
    return _sendToInterface;
  }

  @VisibleForTesting
  @Nullable
  DeviceBroadcastDomain getSendToSwitchForTesting() {
    return _sendToSwitch;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L3Interface)) {
      return false;
    }
    L3Interface that = (L3Interface) o;
    return _iface.equals(that._iface);
  }

  @Override
  public int hashCode() {
    return 31 * L3Interface.class.hashCode() + _iface.hashCode();
  }

  private final @Nonnull NodeInterfacePair _iface;
}
