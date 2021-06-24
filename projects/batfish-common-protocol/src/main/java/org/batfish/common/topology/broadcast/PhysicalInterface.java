package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkState;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A physical Ethernet interface sends possibly-tagged frames.
 *
 * <p>Can have one edge to {@link L3Interface} when receiving frames tagged for a dot1q encapsulated
 * subinterface/untagged frames directly, or to {@link DeviceBroadcastDomain} when this interface is
 * a switchport. These are mutually exclusive.
 *
 * <p>Can have one edge to an {@link EthernetHub}.
 */
public final class PhysicalInterface extends Node<EthernetTag> {

  public PhysicalInterface(NodeInterfacePair iface) {
    _iface = iface;
  }

  public @Nonnull NodeInterfacePair getIface() {
    return _iface;
  }

  public void deliverDirectlyToInterface(L3Interface iface, Edge<EthernetTag, Unit> edge) {
    checkState(
        _interface == null && _switch == null,
        "Cannot connect a physical interface to multiple of L3 interface or device broadcast"
            + " domain");
    _interface = iface;
    _deliverToInterface = edge;
  }

  public void deliverToSwitch(DeviceBroadcastDomain sw, Edge<EthernetTag, Integer> edge) {
    checkState(
        _interface == null && _switch == null,
        "Cannot connect a physical interface to multiple of L3 interface or device broadcast"
            + " domain");
    _switch = sw;
    _deliverToSwitch = edge;
  }

  public void attachToHub(EthernetHub hub, Edge<EthernetTag, EthernetTag> edge) {
    checkState(
        _attachedHub == null, "Cannot connect a physical interface to multiple Ethernet hubs");
    _attachedHub = hub;
    _transmitToHub = edge;
  }

  public void transmit(EthernetTag tag, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (_attachedHub != null) {
      assert _transmitToHub != null;
      _transmitToHub
          .traverse(tag)
          .ifPresent(wireTag -> _attachedHub.broadcast(wireTag, domain, visited));
    }
  }

  public void receive(EthernetTag tag, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    assert _interface == null || _switch == null; // mutually exclusive.
    if (_interface != null) {
      assert _deliverToInterface != null; // contract
      _deliverToInterface.traverse(tag).ifPresent(unit -> _interface.reached(domain, visited));
    } else if (_switch != null) {
      assert _deliverToSwitch != null; // contract
      _deliverToSwitch.traverse(tag).ifPresent(vlan -> _switch.broadcast(vlan, domain, visited));
    }
  }

  // Internal implementation details.

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PhysicalInterface)) {
      return false;
    }
    PhysicalInterface that = (PhysicalInterface) o;
    return _iface.equals(that._iface);
  }

  @Override
  public int hashCode() {
    return 31 * PhysicalInterface.class.hashCode() + _iface.hashCode();
  }

  private final @Nonnull NodeInterfacePair _iface;

  private @Nullable EthernetHub _attachedHub;
  private @Nullable Edge<EthernetTag, EthernetTag> _transmitToHub;
  private @Nullable L3Interface _interface;
  private @Nullable Edge<EthernetTag, Unit> _deliverToInterface;
  private @Nullable DeviceBroadcastDomain _switch;
  private @Nullable Edge<EthernetTag, Integer> _deliverToSwitch;
}
