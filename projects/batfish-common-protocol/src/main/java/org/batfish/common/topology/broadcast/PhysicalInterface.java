package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.broadcast.L3Interface.Unit;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A physical Ethernet interface sends possibly-tagged frames.
 *
 * <p>Can have edges to any number of {@link L3Interface L3 interfaces} when receiving frames tagged
 * for a dot1q encapsulated subinterface/untagged frames directly, or to {@link
 * DeviceBroadcastDomain} when this interface is a switchport. These are mutually exclusive.
 *
 * <p>Can have one edge to an {@link EthernetHub}.
 */
public final class PhysicalInterface extends Node<EthernetTag> {

  public PhysicalInterface(NodeInterfacePair iface) {
    _iface = iface;
    _l3Interfaces = new HashMap<>();
  }

  public @Nonnull NodeInterfacePair getIface() {
    return _iface;
  }

  public void deliverDirectlyToInterface(L3Interface iface, Edge<EthernetTag, Unit> edge) {
    if (_switch != null) {
      throw new IllegalStateException(
          String.format(
              "Physical interface %s is already connected to the device switch: cannot also connect"
                  + " to L3 interface %s",
              _iface, iface.getIface()));
    }
    Edge<EthernetTag, Unit> previous = _l3Interfaces.putIfAbsent(iface, edge);
    checkArgument(previous == null, "Cannot connect the same interface %s twice", iface.getIface());
  }

  public void deliverToSwitch(DeviceBroadcastDomain sw, Edge<EthernetTag, Integer> edge) {
    if (!_l3Interfaces.isEmpty()) {
      throw new IllegalStateException(
          String.format(
              "Physical interface %s is already connected to L3 interfaces: cannot also connect"
                  + " device switch %s",
              _iface, sw.getHostname()));
    }
    if (_switch != null) {
      throw new IllegalStateException(
          String.format(
              "Physical interface %s is already connected to the device switch %s: cannot also"
                  + " connect to %s",
              _iface, _switch.getHostname(), sw.getHostname()));
    }
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
    assert _l3Interfaces.isEmpty() || _switch == null; // mutually exclusive.
    if (!_l3Interfaces.isEmpty()) {
      _l3Interfaces.forEach(
          (iface, edge) -> edge.traverse(tag).ifPresent(unit -> iface.reached(domain, visited)));
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

  @VisibleForTesting
  EthernetHub getAttachedHubForTest() {
    return _attachedHub;
  }

  @VisibleForTesting
  Map<L3Interface, Edge<EthernetTag, Unit>> getL3InterfacesForTest() {
    return _l3Interfaces;
  }

  @VisibleForTesting
  DeviceBroadcastDomain getSwitchForTest() {
    return _switch;
  }

  private final @Nonnull NodeInterfacePair _iface;

  private @Nullable EthernetHub _attachedHub;
  private @Nullable Edge<EthernetTag, EthernetTag> _transmitToHub;
  private final @Nonnull Map<L3Interface, Edge<EthernetTag, Unit>> _l3Interfaces;
  private @Nullable DeviceBroadcastDomain _switch;
  private @Nullable Edge<EthernetTag, Integer> _deliverToSwitch;
}
