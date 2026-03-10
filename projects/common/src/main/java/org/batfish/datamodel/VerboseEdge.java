package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 * Represents an edge connecting the interfaces of two nodes directly. Contains the full
 * specification of both interfaces as well as the names of both nodes.
 */
public final class VerboseEdge implements Serializable, Comparable<VerboseEdge> {
  private static final String PROP_EDGE_SUMMARY = "edgeSummary";
  private static final String PROP_NODE1_INTERFACE = "node1Interface";
  private static final String PROP_NODE2_INTERFACE = "node2Interface";

  private final Edge _edgeSummary;

  private final Interface _node1Interface;

  private final Interface _node2Interface;

  @JsonCreator
  public VerboseEdge(
      @JsonProperty(PROP_NODE1_INTERFACE) Interface int1,
      @JsonProperty(PROP_NODE2_INTERFACE) Interface int2,
      @JsonProperty(PROP_EDGE_SUMMARY) Edge e) {
    _node1Interface = int1;
    _node2Interface = int2;
    _edgeSummary = e;
  }

  @JsonProperty(PROP_EDGE_SUMMARY)
  public Edge getEdgeSummary() {
    return _edgeSummary;
  }

  @JsonProperty(PROP_NODE1_INTERFACE)
  public Interface getInt1() {
    return _node1Interface;
  }

  @JsonProperty(PROP_NODE2_INTERFACE)
  public Interface getInt2() {
    return _node2Interface;
  }

  @Override
  public int compareTo(VerboseEdge other) {
    return _edgeSummary.compareTo(other._edgeSummary);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof VerboseEdge)) {
      return false;
    }
    VerboseEdge other = (VerboseEdge) o;
    return _edgeSummary.equals(other._edgeSummary);
  }

  @Override
  public int hashCode() {
    return _edgeSummary.hashCode();
  }

  @Override
  public String toString() {
    return _edgeSummary.toString();
  }
}
