package org.batfish.symbolic;

import static org.batfish.datamodel.Names.escapeNameIfNeeded;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;

/** Location where reachability analysis begins. */
public final class IngressLocation implements Comparable<IngressLocation> {
  public enum Type {
    INTERFACE_LINK,
    VRF
  }

  private final @Nonnull String _node;

  private final @Nonnull String _pointWithinNode;

  private final @Nonnull Type _pointType;

  private IngressLocation(
      @Nonnull String node, @Nonnull String pointWithinNode, @Nonnull Type pointType) {
    _node = node;
    _pointWithinNode = pointWithinNode;
    _pointType = pointType;
  }

  public static IngressLocation interfaceLink(@Nonnull String node, @Nonnull String iface) {
    return new IngressLocation(node, iface, Type.INTERFACE_LINK);
  }

  public static IngressLocation vrf(@Nonnull String node, @Nonnull String vrf) {
    return new IngressLocation(node, vrf, Type.VRF);
  }

  public boolean isIngressVrf() {
    return _pointType == Type.VRF;
  }

  public @Nonnull String getInterface() {
    if (_pointType == Type.INTERFACE_LINK) {
      return _pointWithinNode;
    }
    throw new BatfishException("IngressLocation type is not interface: " + _pointType);
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
    throw new BatfishException("IngressLocation type is not vrf: " + _pointType);
  }

  @Override
  public int compareTo(IngressLocation other) {
    return Comparator.comparing(IngressLocation::getType)
        .thenComparing(IngressLocation::getNode)
        .thenComparing(IngressLocation::getPointWithinNode)
        .compare(this, other);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IngressLocation)) {
      return false;
    }
    IngressLocation other = (IngressLocation) o;
    return _pointType == other._pointType
        && _node.equals(other._node)
        && _pointWithinNode.equals(other._pointWithinNode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_pointType, _node, _pointWithinNode);
  }

  @Override
  public String toString() {
    String node = escapeNameIfNeeded(_node);
    String pointWithinNode = escapeNameIfNeeded(_pointWithinNode);

    if (_pointType == Type.VRF) {
      return String.format("%s[@vrf(%s)]", node, pointWithinNode);
    } else {
      return String.format("%s[%s]", node, pointWithinNode);
    }
  }
}
