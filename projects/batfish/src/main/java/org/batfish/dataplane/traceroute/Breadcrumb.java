package org.batfish.dataplane.traceroute;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Flow;

/** Used for loop detection */
class Breadcrumb {
  private final @Nonnull String _node;
  private final @Nonnull String _vrf;
  private final @Nonnull Flow _flow;

  Breadcrumb(@Nonnull String node, @Nonnull String vrf, @Nonnull Flow flow) {
    _node = node;
    _vrf = vrf;
    _flow = flow;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Breadcrumb)) {
      return false;
    }

    Breadcrumb other = (Breadcrumb) obj;

    return _node.equals(other._node) && _vrf.equals(other._vrf) && _flow.equals(other._flow);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _vrf, _flow);
  }
}
