package org.batfish.datamodel.bgp;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;

/** A topology representing all BGP peerings. */
@ParametersAreNonnullByDefault
public final class BgpTopology {

  public static final BgpTopology EMPTY =
      new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build());

  private final ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> _graph;

  public BgpTopology(ValueGraph<BgpPeerConfigId, BgpSessionProperties> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  public @Nonnull ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> getGraph() {
    return _graph;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BgpTopology)) {
      return false;
    }
    return _graph.equals(((BgpTopology) obj)._graph);
  }

  @Override
  public int hashCode() {
    return _graph.hashCode();
  }
}
