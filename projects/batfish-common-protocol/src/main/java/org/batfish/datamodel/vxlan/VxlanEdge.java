package org.batfish.datamodel.vxlan;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

@ParametersAreNonnullByDefault
public final class VxlanEdge implements Comparable<VxlanEdge> {

  public static final class Builder {

    private Ip _multicastGroup;
    private VxlanNode _node1;
    private VxlanNode _node2;
    private Integer _udpPort;
    private Integer _vni;

    private Builder() {}

    public @Nonnull VxlanEdge build() {
      return create(_multicastGroup, _node1, _node2, _udpPort, _vni);
    }

    public @Nonnull Builder setMulticastGroup(@Nullable Ip multicastGroup) {
      _multicastGroup = multicastGroup;
      return this;
    }

    public @Nonnull Builder setNode1(VxlanNode node1) {
      _node1 = node1;
      return this;
    }

    public @Nonnull Builder setNode2(VxlanNode node2) {
      _node2 = node2;
      return this;
    }

    public @Nonnull Builder setUdpPort(int udpPort) {
      _udpPort = udpPort;
      return this;
    }

    public @Nonnull Builder setVni(int vni) {
      _vni = vni;
      return this;
    }
  }

  private static final String PROP_MULTICAST_GROUP = "multicastGroup";
  private static final String PROP_NODE1 = "node1";
  private static final String PROP_NODE2 = "node2";
  private static final String PROP_UDP_PORT = "udpPort";
  private static final String PROP_VNI = "vni";

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonCreator
  private static @Nonnull VxlanEdge create(
      @JsonProperty(PROP_MULTICAST_GROUP) @Nullable Ip multicastGroup,
      @JsonProperty(PROP_NODE1) @Nullable VxlanNode node1,
      @JsonProperty(PROP_NODE2) @Nullable VxlanNode node2,
      @JsonProperty(PROP_UDP_PORT) @Nullable Integer udpPort,
      @JsonProperty(PROP_VNI) @Nullable Integer vni) {
    checkArgument(node1 != null, "Missing %s", PROP_NODE1);
    checkArgument(node2 != null, "Missing %s", PROP_NODE2);
    checkArgument(udpPort != null, "Missing %s", PROP_UDP_PORT);
    checkArgument(vni != null, "Missing %s", PROP_VNI);
    return new VxlanEdge(multicastGroup, node1, node2, udpPort, vni);
  }

  private final Ip _multicastGroup;
  private final VxlanNode _node1;
  private final VxlanNode _node2;
  private final int _udpPort;
  private final int _vni;

  public VxlanEdge(
      @Nullable Ip multicastGroup, VxlanNode node1, VxlanNode node2, int udpPort, int vni) {
    _multicastGroup = multicastGroup;
    _node1 = node1;
    _node2 = node2;
    _udpPort = udpPort;
    _vni = vni;
  }

  private static final Comparator<VxlanEdge> COMPARATOR =
      comparing(VxlanEdge::getNode1)
          .thenComparing(VxlanEdge::getNode2)
          .thenComparing(VxlanEdge::getVni)
          .thenComparing(VxlanEdge::getUdpPort)
          .thenComparing(VxlanEdge::getMulticastGroup, nullsFirst(Ip::compareTo));

  @Override
  public int compareTo(VxlanEdge o) {
    return COMPARATOR.compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VxlanEdge)) {
      return false;
    }
    VxlanEdge rhs = (VxlanEdge) obj;
    return Objects.equals(_multicastGroup, rhs._multicastGroup)
        && _node1.equals(rhs._node1)
        && _node2.equals(rhs._node2)
        && _udpPort == rhs._udpPort;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_multicastGroup, _node1, _node2, _udpPort);
  }

  @JsonProperty(PROP_MULTICAST_GROUP)
  public @Nullable Ip getMulticastGroup() {
    return _multicastGroup;
  }

  @JsonProperty(PROP_NODE1)
  public @Nonnull VxlanNode getNode1() {
    return _node1;
  }

  @JsonProperty(PROP_NODE2)
  public @Nonnull VxlanNode getNode2() {
    return _node2;
  }

  @JsonProperty(PROP_UDP_PORT)
  public int getUdpPort() {
    return _udpPort;
  }

  @JsonProperty(PROP_VNI)
  public int getVni() {
    return _vni;
  }
}
