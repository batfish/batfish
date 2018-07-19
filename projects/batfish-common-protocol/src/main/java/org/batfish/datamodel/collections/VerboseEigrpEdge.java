package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import org.batfish.datamodel.eigrp.EigrpNeighbor;

/** Verbose description of EIGRP adjacency */
public final class VerboseEigrpEdge implements Serializable, Comparable<VerboseEigrpEdge> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IpEdge _edgeSummary;
  @Nonnull private final EigrpNeighbor _session1;
  @Nonnull private final EigrpNeighbor _session2;

  @JsonCreator
  public VerboseEigrpEdge(
      @Nonnull @JsonProperty(PROP_NODE1_SESSION) EigrpNeighbor s1,
      @Nonnull @JsonProperty(PROP_NODE2_SESSION) EigrpNeighbor s2,
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
  public EigrpNeighbor getSession1() {
    return _session1;
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public EigrpNeighbor getSession2() {
    return _session2;
  }

  @Override
  public int compareTo(@Nonnull VerboseEigrpEdge o) {
    return Comparator.comparing(VerboseEigrpEdge::getEdgeSummary)
        .thenComparing(edge -> edge.getSession1().getIpLink())
        .thenComparing(edge -> edge.getSession2().getIpLink())
        .compare(this, o);
  }
}
