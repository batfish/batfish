package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L2VniHubToL2Vni;

/**
 * A central point representing a connected subgraph of {@link L2Vni}s that are generally from
 * different devices.
 */
public final class L2VniHub implements Node {

  public L2VniHub(String name) {
    _name = name;
    _attachedVNIs = new HashSet<>();
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return _attachedVNIs.stream()
        .collect(ImmutableMap.toImmutableMap(Function.identity(), l -> L2VniHubToL2Vni.instance()));
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void attachL2Vni(L2Vni l2Vni) {
    boolean added = _attachedVNIs.add(l2Vni);
    checkState(added, "Already attached to l2vni: %s", l2Vni.getNode());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2VniHub)) {
      return false;
    }
    L2VniHub that = (L2VniHub) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return L2VniHub.class.hashCode() * 31 + _name.hashCode();
  }

  private final @Nonnull Set<L2Vni> _attachedVNIs;
  private final @Nonnull String _name;
}
