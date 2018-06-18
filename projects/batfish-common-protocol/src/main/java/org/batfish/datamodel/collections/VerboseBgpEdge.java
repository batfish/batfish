package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.BgpNeighbor;

public final class VerboseBgpEdge implements Serializable, Comparable<VerboseBgpEdge> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final BgpNeighbor _session1;
  @Nonnull private final BgpNeighbor _session2;

  @JsonCreator
  public VerboseBgpEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) BgpNeighbor s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) BgpNeighbor s2,
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
  public BgpNeighbor getNode1Session() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public BgpNeighbor getNode2Session() {
    return _session2;
  }

  @Override
  public int compareTo(VerboseBgpEdge o) {
    int cmp = _edgeSummary.compareTo(o._edgeSummary);
    if (cmp != 0) {
      return cmp;
    }
    cmp = _session1.getName().compareTo(o._session1.getName());
    if (cmp != 0) {
      return cmp;
    }
    cmp = _session2.getName().compareTo(o._session2.getName());
    return cmp;
  }
}
