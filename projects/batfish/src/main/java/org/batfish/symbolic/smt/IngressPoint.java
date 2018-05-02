package org.batfish.symbolic.smt;

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

  private final @Nonnull String _interfaceOrVrf;

  private final @Nonnull Type _type;

  private IngressPoint(@Nonnull Type type, @Nonnull String node, @Nonnull String interfaceOrVrf) {
    _node = node;
    _interfaceOrVrf = interfaceOrVrf;
    _type = type;
  }

  public static IngressPoint ingressInterface(@Nonnull String node, @Nonnull String iface) {
    return new IngressPoint(Type.INTERFACE, node, iface);
  }

  public static IngressPoint ingressVrf(@Nonnull String node, @Nonnull String vrf) {
    return new IngressPoint(Type.VRF, node, vrf);
  }

  public boolean isIngressInterface() {
    return _type == Type.INTERFACE;
  }

  public boolean isIngressVrf() {
    return _type == Type.VRF;
  }

  public @Nonnull String getInterface() {
    if (_type == Type.INTERFACE) {
      return _interfaceOrVrf;
    }
    throw new BatfishException("IngressPoint type is not interface: " + _type);
  }

  public @Nonnull String getNode() {
    return _node;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  public @Nonnull String getVrf() {
    if (_type == Type.VRF) {
      return _interfaceOrVrf;
    }
    throw new BatfishException("IngressPoint type is not vrf: " + _type);
  }

  @Override
  public int compareTo(IngressPoint other) {
    if (other == null) {
      return 1;
    }

    int comparison = _type.compareTo(other._type);
    if (comparison != 0) {
      return comparison;
    }

    comparison = _node.compareTo(other._node);
    if (comparison != 0) {
      return comparison;
    }

    return _interfaceOrVrf.compareTo(other._interfaceOrVrf);
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
    return this._type == other._type
        && Objects.equals(this._node, other._node)
        && Objects.equals(this._interfaceOrVrf, other._interfaceOrVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this._type, this._node, this._interfaceOrVrf);
  }
}
