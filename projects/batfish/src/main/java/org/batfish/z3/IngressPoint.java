package org.batfish.z3;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

/** Location where reachability analysis begins. */
public class IngressPoint implements Comparable<IngressPoint> {
  public enum Type {
    INTERFACE,
    VRF
  }

  private final @Nonnull String _node;

  private final @Nonnull String _pointWithinNode;

  private final @Nonnull Type _pointType;

  private IngressPoint(
      @Nonnull String node, @Nonnull String pointWithinNode, @Nonnull Type pointType) {
    _node = node;
    _pointWithinNode = pointWithinNode;
    _pointType = pointType;
  }

  public static IngressPoint ingressInterface(@Nonnull String node, @Nonnull String iface) {
    return new IngressPoint(node, iface, Type.INTERFACE);
  }

  public static IngressPoint ingressVrf(@Nonnull String node, @Nonnull String vrf) {
    return new IngressPoint(node, vrf, Type.VRF);
  }

  public boolean isIngressInterface() {
    return _pointType == Type.INTERFACE;
  }

  public boolean isIngressVrf() {
    return _pointType == Type.VRF;
  }

  public @Nonnull String getInterface() {
    if (_pointType == Type.INTERFACE) {
      return _pointWithinNode;
    }
    throw new BatfishException("IngressPoint type is not interface: " + _pointType);
  }

  public @Nonnull String getNode() {
    return _node;
  }

  private @Nonnull String getPointWithinNode() {
    return _pointWithinNode;
  }

  public @Nonnull Type getType() {
    return _pointType;
  }

  public @Nonnull String getVrf() {
    if (_pointType == Type.VRF) {
      return _pointWithinNode;
    }
    throw new BatfishException("IngressPoint type is not vrf: " + _pointType);
  }

  @Override
  public int compareTo(IngressPoint other) {
    return Comparator.comparing(IngressPoint::getType)
        .thenComparing(IngressPoint::getNode)
        .thenComparing(IngressPoint::getPointWithinNode)
        .compare(this, other);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IngressPoint)) {
      return false;
    }
    IngressPoint other = (IngressPoint) o;
    return this._pointType == other._pointType
        && Objects.equals(this._node, other._node)
        && Objects.equals(this._pointWithinNode, other._pointWithinNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this._pointType, this._node, this._pointWithinNode);
  }
}
