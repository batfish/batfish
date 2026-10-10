package org.batfish.common.topology;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * The user-provided layer-1 edges that wire a device to itself, with each subinterface resolved to
 * the interface it is bound to. Backs {@link L3Adjacencies#isWiredOnSameDevice}.
 */
@ParametersAreNonnullByDefault
public final class SameDeviceWires implements Serializable {
  public static final SameDeviceWires EMPTY =
      new SameDeviceWires(ImmutableSetMultimap.of(), ImmutableMap.of());

  public static SameDeviceWires create(
      Layer1Topology userL1, Map<String, Configuration> configurations) {
    ImmutableSetMultimap.Builder<NodeInterfacePair, NodeInterfacePair> wires =
        ImmutableSetMultimap.builder();
    userL1
        .edgeStream()
        .filter(e -> e.getNode1().getHostname().equals(e.getNode2().getHostname()))
        .forEach(
            e -> {
              NodeInterfacePair a = e.getNode1().asNodeInterfacePair();
              NodeInterfacePair b = e.getNode2().asNodeInterfacePair();
              wires.put(a, b);
              wires.put(b, a);
            });
    ImmutableSetMultimap<NodeInterfacePair, NodeInterfacePair> builtWires = wires.build();
    if (builtWires.isEmpty()) {
      return EMPTY;
    }
    ImmutableMap.Builder<NodeInterfacePair, NodeInterfacePair> boundParents =
        ImmutableMap.builder();
    for (Configuration c : configurations.values()) {
      for (Interface i : c.getAllInterfaces().values()) {
        i.getDependencies().stream()
            .filter(d -> d.getType() == DependencyType.BIND)
            .findFirst()
            .ifPresent(
                d ->
                    boundParents.put(
                        NodeInterfacePair.of(i),
                        NodeInterfacePair.of(c.getHostname(), d.getInterfaceName())));
      }
    }
    return new SameDeviceWires(builtWires, boundParents.buildOrThrow());
  }

  /**
   * Whether the user wired {@code i1} to {@code i2}: an edge between them, or between either one
   * and the other's parent, or between their parents.
   */
  public boolean isWired(NodeInterfacePair i1, NodeInterfacePair i2) {
    if (!i1.getHostname().equals(i2.getHostname())) {
      return false;
    }
    for (NodeInterfacePair a : selfAndParent(i1)) {
      Set<NodeInterfacePair> wired = _wires.get(a);
      for (NodeInterfacePair b : selfAndParent(i2)) {
        if (wired.contains(b)) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<NodeInterfacePair> selfAndParent(NodeInterfacePair i) {
    NodeInterfacePair parent = _boundParents.get(i);
    return parent == null ? ImmutableSet.of(i) : ImmutableSet.of(i, parent);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof SameDeviceWires)) {
      return false;
    }
    SameDeviceWires that = (SameDeviceWires) o;
    return _wires.equals(that._wires) && _boundParents.equals(that._boundParents);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_wires, _boundParents);
  }

  private SameDeviceWires(
      ImmutableSetMultimap<NodeInterfacePair, NodeInterfacePair> wires,
      ImmutableMap<NodeInterfacePair, NodeInterfacePair> boundParents) {
    _wires = wires;
    _boundParents = boundParents;
  }

  private final ImmutableSetMultimap<NodeInterfacePair, NodeInterfacePair> _wires;
  private final ImmutableMap<NodeInterfacePair, NodeInterfacePair> _boundParents;
}
