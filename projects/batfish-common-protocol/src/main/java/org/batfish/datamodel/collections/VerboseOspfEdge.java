package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfNeighbor;

public final class VerboseOspfEdge implements Serializable, Comparable<VerboseOspfEdge> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final OspfNeighbor _session1;
  @Nonnull private final OspfNeighbor _session2;

  @JsonCreator
  public VerboseOspfEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) OspfNeighbor s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) OspfNeighbor s2,
      @Nonnull @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    _session1 = s1;
    _session2 = s2;
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_SESSION)
  public OspfNeighbor getSession1() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public OspfNeighbor getSession2() {
    return _session2;
  }

  @Override
  public int compareTo(@Nonnull VerboseOspfEdge o) {
    return Comparator.comparing(VerboseOspfEdge::getEdgeSummary)
        .thenComparing(edge -> edge.getSession1().getIpLink())
        .thenComparing(edge -> edge.getSession2().getIpLink())
        .compare(this, o);
  }
}
