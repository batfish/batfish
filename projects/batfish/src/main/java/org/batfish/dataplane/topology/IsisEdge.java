package org.batfish.dataplane.topology;

import static org.batfish.datamodel.IsisLevel.LEVEL_1;
import static org.batfish.datamodel.IsisLevel.LEVEL_1_2;
import static org.batfish.datamodel.IsisLevel.LEVEL_2;

import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IsisInterfaceSettings;
import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.IsisProcess;
import org.batfish.datamodel.Vrf;
import org.batfish.dataplane.ibdp.Node;
import org.batfish.dataplane.ibdp.VirtualRouter;

public class IsisEdge {

  private static IsisLevel interfaceSettingsLevel(IsisInterfaceSettings settings) {
    return union(
        settings.getLevel1() != null ? IsisLevel.LEVEL_1 : null,
        settings.getLevel2() != null ? IsisLevel.LEVEL_2 : null);
  }

  private static @Nullable IsisLevel intersection(IsisLevel... levels) {
    return levels.length == 0 ? null : Arrays.stream(levels).reduce(IsisEdge::intersection).get();
  }

  private static @Nullable IsisLevel intersection(
      @Nullable IsisLevel first, @Nullable IsisLevel second) {
    if (first == second) {
      return first;
    }
    if (first == LEVEL_1_2) {
      return second;
    }
    if (second == LEVEL_1_2) {
      return first;
    }
    return null;
  }

  static @Nullable public IsisEdge newEdge(Edge edge, Map<String, Node> nodes) {
    // vertex1
    Node n1 = nodes.get(edge.getNode1());
    Configuration c1 = n1.getConfiguration();
    Interface iface1 = c1.getInterfaces().get(edge.getInt1());
    Vrf vrf1 = iface1.getVrf();
    IsisProcess proc1 = vrf1.getIsisProcess();
    if (proc1 == null) {
      return null;
    }
    IsisLevel proc1Level = proc1.getLevel();
    IsisInterfaceSettings is1 = iface1.getIsis();
    if (is1 == null) {
      return null;
    }

    // vertex2
    Node n2 = nodes.get(edge.getNode2());
    Configuration c2 = n2.getConfiguration();
    Interface iface2 = c2.getInterfaces().get(edge.getInt2());
    Vrf vrf2 = iface2.getVrf();
    IsisProcess proc2 = vrf2.getIsisProcess();
    if (proc2 == null) {
      return null;
    }
    IsisLevel proc2Level = proc2.getLevel();
    IsisInterfaceSettings is2 = iface2.getIsis();
    if (is2 == null) {
      return null;
    }

    boolean sameArea =
        Arrays.equals(proc1.getNetAddress().getAreaId(), proc2.getNetAddress().getAreaId());
    IsisLevel circuitTypeModuloArea =
        intersection(
            proc1Level, proc2Level, interfaceSettingsLevel(is1), interfaceSettingsLevel(is2));
    IsisLevel level1Component = intersection(sameArea ? LEVEL_1 : null, circuitTypeModuloArea);
    IsisLevel level2Component = intersection(LEVEL_2, circuitTypeModuloArea);
    IsisLevel circuitType = union(level1Component, level2Component);
    if (circuitType == null) {
      return null;
    }
    return new IsisEdge(
        circuitType,
        iface1,
        iface2,
        n1.getVirtualRouters().get(vrf1.getName()),
        n2.getVirtualRouters().get(vrf2.getName()));
  }

  private static @Nullable IsisLevel union(@Nullable IsisLevel level1, @Nullable IsisLevel level2) {
    if (level1 == level2) {
      return level1;
    }
    if (level1 == LEVEL_1_2 || level2 == LEVEL_1_2) {
      return LEVEL_1_2;
    }
    if (level1 == LEVEL_1 || level2 == LEVEL_1) {
      // other is null or LEVEL_2
      if (level1 == LEVEL_2 || level2 == LEVEL_2) {
        return LEVEL_1_2;
      }
      // other is null
      return LEVEL_1;
    }
    // one is null and one is LEVEL_2
    return LEVEL_2;
  }

  private final IsisLevel _circuitType;

  private final Interface _interface1;

  private final Interface _interface2;

  private final VirtualRouter _VirtualRouter1;

  private final VirtualRouter _VirtualRouter2;

  private IsisEdge(
      IsisLevel circuitType,
      Interface interface1,
      Interface interface2,
      VirtualRouter VirtualRouter1,
      VirtualRouter VirtualRouter2) {
    _circuitType = circuitType;
    _interface1 = interface1;
    _interface2 = interface2;
    _VirtualRouter1 = VirtualRouter1;
    _VirtualRouter2 = VirtualRouter2;
  }

  public @Nonnull IsisLevel getCircuitType() {
    return _circuitType;
  }

  public @Nonnull Interface getInterface1() {
    return _interface1;
  }

  public @Nonnull Interface getInterface2() {
    return _interface2;
  }

  public @Nonnull VirtualRouter getProc1() {
    return _VirtualRouter1;
  }

  public @Nonnull VirtualRouter getProc2() {
    return _VirtualRouter2;
  }
}
