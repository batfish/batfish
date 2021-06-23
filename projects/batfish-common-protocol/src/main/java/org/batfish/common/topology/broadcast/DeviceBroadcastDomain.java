package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.topology.broadcast.L3Interface.Unit;

/**
 * Represents the switch running inside a device and modeling its broadcast domain.
 *
 * <p>Has edges to {@link L3Interface L3 interfaces} with vlan configuration, like {@code irb.1} or
 * {@code Vlan7}.
 *
 * <p>Has edges to {@link PhysicalInterface physical interfaces} with switchport configuration,
 * specifically in trunk or access modes.
 */
public final class DeviceBroadcastDomain extends Node<Integer> {
  public DeviceBroadcastDomain(String hostname) {
    _hostname = hostname;
    _l3Interfaces = new HashMap<>();
    _physicalInterfaces = new HashMap<>();
  }

  public void deliverToL3(L3Interface iface, Edge<Integer, Unit> edge) {
    Edge<Integer, Unit> previous = _l3Interfaces.putIfAbsent(iface, edge);
    checkArgument(previous == null, "Cannot connect the same interface %s twice", iface.getIface());
  }

  public void transmitOutPhysical(PhysicalInterface iface, Edge<Integer, EthernetTag> edge) {
    Edge<Integer, EthernetTag> previous = _physicalInterfaces.putIfAbsent(iface, edge);
    checkArgument(previous == null, "Cannot connect the same interface %s twice", iface.getIface());
  }

  public @Nonnull String getHostname() {
    return _hostname;
  }

  // Internal implementation details.
  public void broadcast(int vlan, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (!visited.add(new NodeAndData<>(this, vlan))) {
      return;
    }

    _physicalInterfaces.forEach(
        (iface, edge) ->
            edge.traverse(vlan).ifPresent(tag -> iface.transmit(tag, domain, visited)));
    _l3Interfaces.forEach(
        (iface, edge) -> edge.traverse(vlan).ifPresent(v -> iface.reached(domain, visited)));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof DeviceBroadcastDomain)) {
      return false;
    }
    DeviceBroadcastDomain that = (DeviceBroadcastDomain) o;
    return _hostname.equals(that._hostname);
  }

  @Override
  public int hashCode() {
    return 31 * DeviceBroadcastDomain.class.hashCode() + _hostname.hashCode();
  }

  private final @Nonnull String _hostname;
  private final @Nonnull Map<L3Interface, Edge<Integer, Unit>> _l3Interfaces;
  private final @Nonnull Map<PhysicalInterface, Edge<Integer, EthernetTag>> _physicalInterfaces;
}
