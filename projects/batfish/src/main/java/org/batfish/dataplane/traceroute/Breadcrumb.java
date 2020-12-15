package org.batfish.dataplane.traceroute;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;

/** Used for loop detection */
class Breadcrumb {
  private final @Nonnull String _node;
  private final @Nonnull String _vrf;
  private final @Nullable String _ingressInterface;
  private final @Nonnull Flow _flow;

  Breadcrumb(
      @Nonnull String node,
      @Nonnull String vrf,
      @Nullable String ingressInterface,
      @Nonnull Flow flow) {
    _node = node;
    _vrf = vrf;
    _ingressInterface = ingressInterface;
    _flow = flow;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Breadcrumb)) {
      return false;
    }
    Breadcrumb other = (Breadcrumb) obj;
    return _node.equals(other._node)
        && _vrf.equals(other._vrf)
        && _flow.equals(other._flow)
        && Objects.equals(_ingressInterface, other._ingressInterface);
  }

  @Override
  public int hashCode() {
    int hash = _hashCode;
    if (hash == 0) {
      hash = Objects.hash(_node, _vrf, _flow, _ingressInterface);
      _hashCode = hash;
    }
    return hash;
  }

  private int _hashCode;
}
