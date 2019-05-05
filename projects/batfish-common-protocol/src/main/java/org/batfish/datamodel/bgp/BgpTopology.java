package org.batfish.datamodel.bgp;

import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.SerializableValueGraph;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;

/** A topology representing all BGP peerings. */
@ParametersAreNonnullByDefault
public final class BgpTopology implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final BgpTopology EMPTY =
      new BgpTopology(ValueGraphBuilder.directed().allowsSelfLoops(false).build());

  private final SerializableValueGraph<BgpPeerConfigId, BgpSessionProperties> _graph;

  public BgpTopology(ValueGraph<BgpPeerConfigId, BgpSessionProperties> graph) {
    _graph = new SerializableValueGraph<>(graph);
  }

  public @Nonnull SerializableValueGraph<BgpPeerConfigId, BgpSessionProperties> getGraph() {
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

  /** Directional, reversible BGP edge pointing to two {@link BgpPeerConfigId}. */
  @ParametersAreNonnullByDefault
  public static final class EdgeId implements Comparable<EdgeId> {

    @Nonnull private final BgpPeerConfigId _tail;
    @Nonnull private final BgpPeerConfigId _head;
    private static final Comparator<EdgeId> COMPARATOR =
        Comparator.comparing(EdgeId::tail).thenComparing(EdgeId::head);

    public EdgeId(BgpPeerConfigId tail, BgpPeerConfigId head) {
      _tail = tail;
      _head = head;
    }

    public BgpPeerConfigId tail() {
      return _tail;
    }

    public BgpPeerConfigId head() {
      return _head;
    }

    public EdgeId reverse() {
      return new EdgeId(_head, _tail);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof EdgeId)) {
        return false;
      }
      EdgeId other = (EdgeId) o;
      return _tail.equals(other._tail) && _head.equals(other._head);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_tail, _head);
    }

    @Override
    public int compareTo(EdgeId o) {
      return COMPARATOR.compare(this, o);
    }
  }
}
