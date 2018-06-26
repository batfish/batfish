package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.RipNeighbor;

public final class VerboseRipEdge implements Serializable, Comparable<VerboseRipEdge> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final RipNeighbor _session1;
  @Nonnull private final RipNeighbor _session2;

  @JsonCreator
  public VerboseRipEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) RipNeighbor s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) RipNeighbor s2,
      @Nonnull @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    this._session1 = s1;
    this._session2 = s2;
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_SESSION)
  public RipNeighbor getSession1() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public RipNeighbor getSession2() {
    return _session2;
  }

  @Override
  public int compareTo(VerboseRipEdge o) {
    int cmp = _edgeSummary.compareTo(o._edgeSummary);
    if (cmp != 0) {
      return cmp;
    }
    cmp = _session1.getIpEdge().compareTo(o._session1.getIpEdge());
    if (cmp != 0) {
      return cmp;
    }
    cmp = _session2.getIpEdge().compareTo(o._session2.getIpEdge());
    return cmp;
  }
}
