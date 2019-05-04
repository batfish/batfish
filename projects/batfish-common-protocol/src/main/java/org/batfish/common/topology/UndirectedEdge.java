package org.batfish.common.topology;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class UndirectedEdge<N> {

  private static final String PROP_NODE_U = "nodeU";
  private static final String PROP_NODE_V = "nodeV";

  @JsonCreator
  private static @Nonnull <N> UndirectedEdge<N> create(
      @JsonProperty(PROP_NODE_U) @Nullable N nodeU, @JsonProperty(PROP_NODE_V) @Nullable N nodeV) {
    checkArgument(nodeU != null, "Missing %s", PROP_NODE_U);
    checkArgument(nodeV != null, "Missing %s", PROP_NODE_V);
    return new UndirectedEdge<>(nodeU, nodeV);
  }

  private final @Nonnull N _nodeU;
  private final @Nonnull N _nodeV;

  public UndirectedEdge(N nodeU, N nodeV) {
    _nodeU = nodeU;
    _nodeV = nodeV;
  }

  @JsonProperty(PROP_NODE_U)
  public @Nonnull N getNodeU() {
    return _nodeU;
  }

  @JsonProperty(PROP_NODE_V)
  public @Nonnull N getNodeV() {
    return _nodeV;
  }
}
