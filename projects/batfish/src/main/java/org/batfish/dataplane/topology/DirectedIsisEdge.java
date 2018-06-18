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

public final class DirectedIsisEdge extends IsisEdge {

  private static IsisLevel interfaceSettingsLevel(IsisInterfaceSettings settings) {
    return union(
        settings.getLevel1() != null ? IsisLevel.LEVEL_1 : null,
        settings.getLevel2() != null ? IsisLevel.LEVEL_2 : null);
  }

  private static IsisLevel processLevel(IsisProcess isisProcess) {
    return union(
        isisProcess.getLevel1() != null ? IsisLevel.LEVEL_1 : null,
        isisProcess.getLevel2() != null ? IsisLevel.LEVEL_2 : null);
  }

  private static @Nullable IsisLevel intersection(IsisLevel... levels) {
    return levels.length == 0
        ? null
        : Arrays.stream(levels).reduce(DirectedIsisEdge::intersection).get();
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

  static @Nullable public DirectedIsisEdge newEdge(
      Edge edge, Map<String, Configuration> configurations) {
    // vertex1
    Configuration c1 = configurations.get(edge.getNode1());
    Interface iface1 = c1.getInterfaces().get(edge.getInt1());
    Vrf vrf1 = iface1.getVrf();
    IsisProcess proc1 = vrf1.getIsisProcess();
    if (proc1 == null) {
      return null;
    }
    IsisInterfaceSettings is1 = iface1.getIsis();
    if (is1 == null) {
      return null;
    }

    // vertex2
    Configuration c2 = configurations.get(edge.getNode2());
    Interface iface2 = c2.getInterfaces().get(edge.getInt2());
    Vrf vrf2 = iface2.getVrf();
    IsisProcess proc2 = vrf2.getIsisProcess();
    if (proc2 == null) {
      return null;
    }
    IsisInterfaceSettings is2 = iface2.getIsis();
    if (is2 == null) {
      return null;
    }

    boolean sameArea =
        Arrays.equals(proc1.getNetAddress().getAreaId(), proc2.getNetAddress().getAreaId());

    // make sure system IDs are distinct
    if (Arrays.equals(proc1.getNetAddress().getSystemId(), proc2.getNetAddress().getSystemId())) {
      return null;
    }

    /* Compute level. If areas are distinct, only level 2 is allowed. */
    IsisLevel circuitType =
        intersection(
            processLevel(proc1),
            interfaceSettingsLevel(is1),
            processLevel(proc2),
            interfaceSettingsLevel(is2),
            sameArea ? LEVEL_1_2 : LEVEL_1);
    if (circuitType == null) {
      return null;
    }

    return new DirectedIsisEdge(
        circuitType,
        new IsisNode(c1.getHostname(), iface1.getName()),
        new IsisNode(c2.getHostname(), iface2.getName()));
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

  protected DirectedIsisEdge(
      @Nonnull IsisLevel circuitType, @Nonnull IsisNode node1, @Nonnull IsisNode node2) {
    super(circuitType, node1, node2);
  }

  public @Nonnull UndirectedIsisEdge toUndirectedEdge() {
    return new UndirectedIsisEdge(this);
  }
}
