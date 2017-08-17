package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OspfNeighbor;

public final class VerboseOspfEdge extends Pair<NodeOspfSessionPair, NodeOspfSessionPair> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE1 = "node1";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final String PROP_NODE2 = "node2";

  private static final long serialVersionUID = 1L;

  private final IpEdge _edgeSummary;

  @JsonCreator
  public VerboseOspfEdge(
      @JsonProperty(PROP_NODE1) Configuration node1,
      @JsonProperty(PROP_NODE1_SESSION) OspfNeighbor s1,
      @JsonProperty(PROP_NODE2) Configuration node2,
      @JsonProperty(PROP_NODE2_SESSION) OspfNeighbor s2,
      @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    super(new NodeOspfSessionPair(node1, s1), new NodeOspfSessionPair(node2, s2));
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1)
  public Configuration getNode1() {
    return _first.getHost();
  }

  @JsonProperty(PROP_NODE2)
  public Configuration getNode2() {
    return _second.getHost();
  }

  @JsonProperty(PROP_NODE1_SESSION)
  public OspfNeighbor getSession1() {
    return _first.getSession();
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public OspfNeighbor getSession2() {
    return _second.getSession();
  }
}
