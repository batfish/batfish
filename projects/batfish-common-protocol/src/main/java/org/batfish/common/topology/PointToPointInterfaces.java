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
   * subinterfaces Ethernet2.3 and Ethernet.4, and there is a layer-1 edge between them, then
   * calling this function on any of the three Ethernet1 interface or subinterfaces will return all
   * three of those for Ethernet2.
   *
   * <p>NOTE: this function does <strong>not</strong> take Layer-2 adjacency, Layer-3 configuration,
   * or any other such state into account.
   */
  public Collection<NodeInterfacePair> pointToPointInterfaces(NodeInterfacePair iface) {
    NodeInterfacePair physical = _subinterfacesToPhysical.get(iface);
    if (physical == null) {
      return ImmutableList.of();
    }
    NodeInterfacePair neighbor = _pointToPointPhysical.get(physical);
    if (neighbor == null) {
      return ImmutableList.of();
    }
    return _physicalToSubinterfaces.get(neighbor);
  }

  /** @see PointToPointComputer#compute */
  @VisibleForTesting
  public static PointToPointInterfaces createForTesting(
      Map<NodeInterfacePair, NodeInterfacePair> pointToPointPhysical,
      Map<NodeInterfacePair, NodeInterfacePair> subInterfacesToPhysical) {
    return new PointToPointInterfaces(pointToPointPhysical, subInterfacesToPhysical);
  }

  /// Internal below here ///

  PointToPointInterfaces(
      Map<NodeInterfacePair, NodeInterfacePair> pointToPointPhysical,
      Map<NodeInterfacePair, NodeInterfacePair> subInterfacesToPhysical) {
    _pointToPointPhysical = ImmutableMap.copyOf(pointToPointPhysical);
    _subinterfacesToPhysical = ImmutableMap.copyOf(subInterfacesToPhysical);
    _physicalToSubinterfaces = computePhysicalToSubinterfaces(subInterfacesToPhysical);
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
        && _physicalToSubinterfaces.equals(that._physicalToSubinterfaces);
    // _subinterfacesToPhysical is derived from others
  }

  @Override
  public int hashCode() {
    // _subinterfacesToPhysical is derived from others
    return Objects.hash(_pointToPointPhysical, _physicalToSubinterfaces);
  }

  /**
   * Computes the reverse mapping to {@link #_subinterfacesToPhysical}. Note that a physical
   * interface can have many different subinterfaces, so this returns a multiple mapping.
   */
  private static @Nonnull Multimap<NodeInterfacePair, NodeInterfacePair>
      computePhysicalToSubinterfaces(
          Map<NodeInterfacePair, NodeInterfacePair> subinterfacesToPhysical) {
    ImmutableMultimap.Builder<NodeInterfacePair, NodeInterfacePair> physicalToSubinterfaces =
        ImmutableMultimap.builder();
    subinterfacesToPhysical.forEach((subif, phys) -> physicalToSubinterfaces.put(phys, subif));
    return physicalToSubinterfaces.build();
  }

  private final @Nonnull Map<NodeInterfacePair, NodeInterfacePair> _pointToPointPhysical;
  private final @Nonnull Multimap<NodeInterfacePair, NodeInterfacePair> _physicalToSubinterfaces;
  private final @Nonnull Map<NodeInterfacePair, NodeInterfacePair> _subinterfacesToPhysical;
}
