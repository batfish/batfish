package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;

public final class VerboseBgpEdge extends Pair<NodeBgpSessionPair, NodeBgpSessionPair> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_SESSION = "node1Session";

  private static final String PROP_NODE1 = "node1";

  private static final String PROP_NODE2_SESSION = "node2Session";

  private static final String PROP_NODE2 = "node2";

  private static final long serialVersionUID = 1L;

  private final IpEdge _edgeSummary;

  @JsonCreator
  public VerboseBgpEdge(
      @JsonProperty(PROP_NODE1) Configuration node1,
      @JsonProperty(PROP_NODE1_SESSION) BgpNeighbor s1,
      @JsonProperty(PROP_NODE2) Configuration node2,
      @JsonProperty(PROP_NODE2_SESSION) BgpNeighbor s2,
      @JsonProperty(PROP_EDGE_SUMMARY) IpEdge e) {
    super(new NodeBgpSessionPair(node1, s1), new NodeBgpSessionPair(node2, s2));
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public IpEdge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1)
  public Configuration getNode1() {
    return _first.getNode();
  }

  @JsonProperty(PROP_NODE1_SESSION)
  public BgpNeighbor getNode1Session() {
    return _first.getSession();
  }

  @JsonProperty(PROP_NODE2)
  public Configuration getNode2() {
    return _second.getNode();
  }

  @JsonProperty(PROP_NODE2_SESSION)
  public BgpNeighbor getNode2Session() {
    return _second.getSession();
  }
}
