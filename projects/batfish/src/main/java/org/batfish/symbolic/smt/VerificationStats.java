package org.batfish.symbolic.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A simple class to record a few statistics about the network encoding and how long it spends in Z3
 * to solve the instance.
 *
 * @author Ryan Beckett
 */
public class VerificationStats {

  private static final String NUM_NODES_VAR = "numNodes";

  private static final String NUM_EDGES_VAR = "numEdges";

  private static final String NUM_VARIABLES_VAR = "numVariables";

  private static final String NUM_CONSTRAINTS_VAR = "numConstraints";

  private static final String TIME_VAR = "time";

  private int _numNodes;

  private int _numEdges;

  private int _numVariables;

  private int _numConstraints;

  private long _time;

  @JsonCreator
  public VerificationStats(
      @JsonProperty(NUM_NODES_VAR) int n,
      @JsonProperty(NUM_EDGES_VAR) int e,
      @JsonProperty(NUM_VARIABLES_VAR) int v,
      @JsonProperty(NUM_CONSTRAINTS_VAR) int c,
      @JsonProperty(TIME_VAR) long t) {
    _numNodes = n;
    _numEdges = e;
    _numVariables = v;
    _numConstraints = c;
    _time = t;
  }

  @JsonProperty(NUM_NODES_VAR)
  public int getNumNodes() {
    return _numNodes;
  }

  @JsonProperty(NUM_EDGES_VAR)
  public int getNumEdges() {
    return _numEdges;
  }

  @JsonProperty(NUM_VARIABLES_VAR)
  public int getNumVariables() {
    return _numVariables;
  }

  @JsonProperty(NUM_CONSTRAINTS_VAR)
  public int getNumConstraints() {
    return _numConstraints;
  }

  @JsonProperty(TIME_VAR)
  public long getTime() {
    return _time;
  }

  @Override
  public String toString() {
    return "VerificationStats{"
        + "_numNodes="
        + _numNodes
        + ", _numEdges="
        + _numEdges
        + ", _numVariables="
        + _numVariables
        + ", _numConstraints="
        + _numConstraints
        + ", _time="
        + _time
        + '}';
  }
}
