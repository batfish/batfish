package org.batfish.datamodel.isis;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_1_2;
import static org.batfish.datamodel.isis.IsisLevel.LEVEL_2;
import static org.batfish.datamodel.isis.IsisLevel.intersection;
import static org.batfish.datamodel.isis.IsisLevel.union;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;

public final class IsisEdge implements Comparable<IsisEdge>, Serializable {

  private static @Nullable IsisLevel interfaceSettingsLevel(IsisInterfaceSettings settings) {
    return union(
        settings.getLevel1() != null ? LEVEL_1 : null,
        settings.getLevel2() != null ? LEVEL_2 : null);
  }

  /** Return an {@link IsisEdge} if there is an IS-IS circuit on {@code edge}. */
  public static @Nonnull Optional<IsisEdge> edgeIfCircuit(
      Edge edge, Map<String, Configuration> configurations) {
    // vertex1
    Configuration c1 = configurations.get(edge.getNode1());
    assert c1 != null;
    Interface iface1 = c1.getAllInterfaces().get(edge.getInt1());
    assert iface1 != null;
    Vrf vrf1 = iface1.getVrf();
    assert vrf1 != null;
    IsisProcess proc1 = vrf1.getIsisProcess();
    if (proc1 == null) {
      return empty();
    }
    IsisInterfaceSettings is1 = iface1.getIsis();
    if (is1 == null) {
      return empty();
    }

    // vertex2
    Configuration c2 = configurations.get(edge.getNode2());
    assert c2 != null;
    Interface iface2 = c2.getAllInterfaces().get(edge.getInt2());
    assert iface2 != null;
    Vrf vrf2 = iface2.getVrf();
    assert vrf2 != null;
    IsisProcess proc2 = vrf2.getIsisProcess();
    if (proc2 == null) {
      return empty();
    }
    IsisInterfaceSettings is2 = iface2.getIsis();
    if (is2 == null) {
      return empty();
    }

    boolean sameArea =
        Arrays.equals(proc1.getNetAddress().getAreaId(), proc2.getNetAddress().getAreaId());

    // make sure system IDs are distinct
    if (Arrays.equals(proc1.getNetAddress().getSystemId(), proc2.getNetAddress().getSystemId())) {
      return empty();
    }

    /* Compute level. If areas are distinct, only level 2 is allowed. */
    IsisLevel circuitType =
        intersection(
            processLevel(proc1),
            interfaceSettingsLevel(is1),
            processLevel(proc2),
            interfaceSettingsLevel(is2),
            sameArea ? LEVEL_1_2 : LEVEL_2);
    if (circuitType == null) {
      return empty();
    }

    return Optional.of(
        new IsisEdge(
            circuitType,
            new IsisNode(c1.getHostname(), iface1.getName()),
            new IsisNode(c2.getHostname(), iface2.getName())));
  }

  private static @Nullable IsisLevel processLevel(IsisProcess isisProcess) {
    return union(
        isisProcess.getLevel1() != null ? LEVEL_1 : null,
        isisProcess.getLevel2() != null ? LEVEL_2 : null);
  }

  private static final String PROP_CIRCUIT_TYPE = "circuitType";
  private static final String PROP_NODE1 = "node1";
  private static final String PROP_NODE2 = "node2";

  private final IsisLevel _circuitType;
  private final IsisNode _node1;
  private final IsisNode _node2;

  public IsisEdge(
      @Nonnull IsisLevel circuitType, @Nonnull IsisNode node1, @Nonnull IsisNode node2) {
    _circuitType = circuitType;
    _node1 = node1;
    _node2 = node2;
  }

  @JsonCreator
  private static @Nonnull IsisEdge create(
      @JsonProperty(PROP_CIRCUIT_TYPE) IsisLevel circuitType,
      @JsonProperty(PROP_NODE1) IsisNode node1,
      @JsonProperty(PROP_NODE2) IsisNode node2) {
    return new IsisEdge(requireNonNull(circuitType), requireNonNull(node1), requireNonNull(node2));
  }

  @Override
  public int compareTo(@Nonnull IsisEdge o) {
    return Comparator.comparing(IsisEdge::getNode1)
        .thenComparing(IsisEdge::getNode2)
        .thenComparing(IsisEdge::getCircuitType)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IsisEdge)) {
      return false;
    }
    IsisEdge rhs = (IsisEdge) o;
    return _circuitType == rhs._circuitType
        && _node1.equals(rhs._node1)
        && _node2.equals(rhs._node2);
  }

  @JsonProperty(PROP_CIRCUIT_TYPE)
  public @Nonnull IsisLevel getCircuitType() {
    return _circuitType;
  }

  @JsonProperty(PROP_NODE1)
  public @Nonnull IsisNode getNode1() {
    return _node1;
  }

  @JsonProperty(PROP_NODE2)
  public @Nonnull IsisNode getNode2() {
    return _node2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_circuitType.ordinal(), _node1, _node2);
  }

  public @Nonnull IsisEdge reverse() {
    return new IsisEdge(_circuitType, _node2, _node1);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("node1", _node1)
        .add("node2", _node2)
        .add("circuitType", _circuitType)
        .toString();
  }
}
