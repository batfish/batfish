package org.batfish.datamodel.ipsec;

import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpsecPeerConfigId;
import org.batfish.datamodel.IpsecSession;

/** A topology representing all IPsec peerings. */
@ParametersAreNonnullByDefault
public final class IpsecTopology {

  public static final IpsecTopology EMPTY =
      new IpsecTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build());

  private final ValueGraph<IpsecPeerConfigId, IpsecSession> _graph;

  public IpsecTopology(ValueGraph<IpsecPeerConfigId, IpsecSession> graph) {
    _graph = ImmutableValueGraph.copyOf(graph);
  }

  public @Nonnull ValueGraph<IpsecPeerConfigId, IpsecSession> getGraph() {
    return _graph;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IpsecTopology)) {
      return false;
    }
    return _graph.equals(((IpsecTopology) obj)._graph);
  }

  @Override
  public int hashCode() {
    return _graph.hashCode();
  }
}
