package org.batfish.common.topology;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * Represents information about possibly-point-to-point connected interfaces, both
 * physical/aggregated and their subinterfaces.
 */
@ParametersAreNonnullByDefault
public final class PointToPointInterfaces implements Serializable {

  /**
   * For any interface or subinterface {@code iface}, returns the interfaces and subinterfaces of
   * the neighboring point-to-point interface.
   *
   * <p>For example, if Ethernet1 has subinterfaces Ethernet1.2 and Ethernet1.3, Ethernet2 has
   * subinterfaces Ethernet2.3 and Ethernet2.4, and there is a layer-1 edge between them, then
   * calling this function on any of the three Ethernet1 interface or subinterfaces will return all
   * three of those for Ethernet2.
   *
   * <p>NOTE: this function does <strong>not</strong> take Layer-2 adjacency, Layer-3 configuration,
   * or any other such state into account.
   */
  public Collection<NodeInterfacePair> pointToPointInterfaces(NodeInterfacePair iface) {
    NodeInterfacePair physical = _interfacesToPhysical.get(iface);
    if (physical == null) {
      return ImmutableList.of();
    }
    NodeInterfacePair neighbor = _pointToPointPhysical.get(physical);
    if (neighbor == null) {
      return ImmutableList.of();
    }
    return _physicalToInterfaces.get(neighbor);
  }

  /**
   * @see PointToPointComputer#compute
   */
  @VisibleForTesting
  public static PointToPointInterfaces createForTesting(
      Map<NodeInterfacePair, NodeInterfacePair> pointToPointPhysical,
      Map<NodeInterfacePair, NodeInterfacePair> interfacesToPhysical) {
    return new PointToPointInterfaces(pointToPointPhysical, interfacesToPhysical);
  }

  /// Internal below here ///

  PointToPointInterfaces(
      Map<NodeInterfacePair, NodeInterfacePair> pointToPointPhysical,
      Map<NodeInterfacePair, NodeInterfacePair> interfacesToPhysical) {
    _pointToPointPhysical = ImmutableMap.copyOf(pointToPointPhysical);
    _interfacesToPhysical = ImmutableMap.copyOf(interfacesToPhysical);
    _physicalToInterfaces = computePhysicalToInterfaces(interfacesToPhysical);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PointToPointInterfaces)) {
      return false;
    }
    PointToPointInterfaces that = (PointToPointInterfaces) o;
    return _pointToPointPhysical.equals(that._pointToPointPhysical)
        && _physicalToInterfaces.equals(that._physicalToInterfaces);
    // _interfacesToPhysical is derived from others
  }

  @Override
  public int hashCode() {
    // _interfacesToPhysical is derived from others
    return Objects.hash(_pointToPointPhysical, _physicalToInterfaces);
  }

  /**
   * Computes the reverse mapping to {@link #_interfacesToPhysical}. Note that a physical interface
   * can have logical configuration itself or on its several (sub)-interfaces, so this returns a
   * multiple mapping.
   */
  private static @Nonnull Multimap<NodeInterfacePair, NodeInterfacePair>
      computePhysicalToInterfaces(Map<NodeInterfacePair, NodeInterfacePair> interfacesToPhysical) {
    ImmutableMultimap.Builder<NodeInterfacePair, NodeInterfacePair> physicalToInterfaces =
        ImmutableMultimap.builder();
    interfacesToPhysical.forEach((iface, phys) -> physicalToInterfaces.put(phys, iface));
    return physicalToInterfaces.build();
  }

  private final @Nonnull Map<NodeInterfacePair, NodeInterfacePair> _pointToPointPhysical;
  private final @Nonnull Multimap<NodeInterfacePair, NodeInterfacePair> _physicalToInterfaces;
  private final @Nonnull Map<NodeInterfacePair, NodeInterfacePair> _interfacesToPhysical;
}
