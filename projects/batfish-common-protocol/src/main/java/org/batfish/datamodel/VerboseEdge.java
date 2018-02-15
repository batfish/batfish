package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public final class VerboseEdge extends Pair<Interface, Interface> {

  private static final String PROP_EDGE_SUMMARY = "edgeSummary";

  private static final String PROP_NODE1_INTERFACE = "node1Interface";

  private static final String PROP_NODE2_INTERFACE = "node2Interface";

  private static final long serialVersionUID = 1L;

  private final Edge _edgeSummary;

  @JsonCreator
  public VerboseEdge(
      @JsonProperty(PROP_NODE1_INTERFACE) Interface int1,
      @JsonProperty(PROP_NODE2_INTERFACE) Interface int2,
      @JsonProperty(PROP_EDGE_SUMMARY) Edge e) {
    super(int1, int2);
    this._edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public Edge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_INTERFACE)
  public Interface getInt1() {
    return _first;
  }

  @JsonProperty(PROP_NODE2_INTERFACE)
  public Interface getInt2() {
    return _second;
  }

  @Override
  public String toString() {
    return _edgeSummary.toString();
  }
}
