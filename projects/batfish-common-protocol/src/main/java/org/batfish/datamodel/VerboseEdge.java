package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.collections.VerboseNodeInterfacePair;

public final class VerboseEdge extends Pair<VerboseNodeInterfacePair, VerboseNodeInterfacePair> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_INTERFACE = "node1Interface";

  private static final String PROP_NODE1 = "node1";

  private static final String PROP_NODE2_INTERFACE = "node2Interface";

  private static final String PROP_NODE2 = "node2";

  private static final long serialVersionUID = 1L;

  private final Edge _edgeSummary;

  @JsonCreator
  public VerboseEdge(
      @JsonProperty(PROP_NODE1) Configuration node1,
      @JsonProperty(PROP_NODE1_INTERFACE) Interface int1,
      @JsonProperty(PROP_NODE2) Configuration node2,
      @JsonProperty(PROP_NODE2_INTERFACE) Interface int2,
      @JsonProperty(PROP_EDGE_SUMMARY) Edge e) {
    this(new VerboseNodeInterfacePair(node1, int1), new VerboseNodeInterfacePair(node2, int2), e);
  }

  public VerboseEdge(VerboseNodeInterfacePair p1, VerboseNodeInterfacePair p2, Edge e) {
    super(p1, p2);
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public Edge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_INTERFACE)
  public Interface getInt1() {
    return _first.getInterface();
  }

  @JsonProperty(PROP_NODE2_INTERFACE)
  public Interface getInt2() {
    return _second.getInterface();
  }

  @JsonIgnore
  public VerboseNodeInterfacePair getInterface1() {
    return _first;
  }

  @JsonIgnore
  public VerboseNodeInterfacePair getInterface2() {
    return _second;
  }

  @JsonProperty(PROP_NODE1)
  public Configuration getNode1() {
    return _first.getHost();
  }

  @JsonProperty(PROP_NODE2)
  public Configuration getNode2() {
    return _second.getHost();
  }

  @Override
  public String toString() {
    return "<" + getNode1() + ":" + getInt1() + ", " + getNode2() + ":" + getInt2() + ">";
  }
}
