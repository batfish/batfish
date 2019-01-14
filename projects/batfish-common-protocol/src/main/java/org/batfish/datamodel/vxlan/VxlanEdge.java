package org.batfish.datamodel.vxlan;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.Ip;

/**
 * An edge between a pair of {@link VxlanNode}s. The edge represents a connection initiated by the
 * {@code _tail} node to the {@code _head} node.
 */
@ParametersAreNonnullByDefault
public final class VxlanEdge {

  public static final class Builder {

    private VxlanNode _head;
    private Ip _multicastGroup;
    private VxlanNode _tail;
    private Integer _udpPort;
    private Integer _vni;

    private Builder() {}

    public @Nonnull VxlanEdge build() {
      checkArgument(_tail != null, "Missing %s", "tail");
      checkArgument(_head != null, "Missing %s", "head");
      checkArgument(_udpPort != null, "Missing %s", "udpPort");
      checkArgument(_vni != null, "Missing %s", "vni");
      return new VxlanEdge(_multicastGroup, _tail, _head, _udpPort, _vni);
    }

    public @Nonnull Builder setHead(VxlanNode head) {
      _head = head;
      return this;
    }

    public @Nonnull Builder setMulticastGroup(@Nullable Ip multicastGroup) {
      _multicastGroup = multicastGroup;
      return this;
    }

    public @Nonnull Builder setTail(VxlanNode tail) {
      _tail = tail;
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

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final VxlanNode _head;
  private final Ip _multicastGroup;
  private final VxlanNode _tail;
  private final int _udpPort;
  private final int _vni;

  public VxlanEdge(
      @Nullable Ip multicastGroup, VxlanNode node1, VxlanNode node2, int udpPort, int vni) {
    _multicastGroup = multicastGroup;
    _tail = node1;
    _head = node2;
    _udpPort = udpPort;
    _vni = vni;
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
        && _tail.equals(rhs._tail)
        && _head.equals(rhs._head)
        && _udpPort == rhs._udpPort;
  }

  /** The listening node for the VXLAN connection represented by this edge. */
  public @Nonnull VxlanNode getHead() {
    return _head;
  }

  /**
   * Multicast group that is destination address of messages in the case multicast is the {@link
   * BumTransportMethod} of the connection representd by this edge.
   */
  public @Nullable Ip getMulticastGroup() {
    return _multicastGroup;
  }

  /** The initiating node for the VXLAN connection represented by this edge. */
  public @Nonnull VxlanNode getTail() {
    return _tail;
  }

  /** The UDP port on which VXLAN messages are sent. */
  public int getUdpPort() {
    return _udpPort;
  }

  /** The VNI of the VXLAN connection represented by this edge. */
  public int getVni() {
    return _vni;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_multicastGroup, _tail, _head, _udpPort);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add("head", _head)
        .add("multicastGroup", _multicastGroup)
        .add("tail", _tail)
        .add("udpPort", _udpPort)
        .add("vni", _vni)
        .toString();
  }
}
