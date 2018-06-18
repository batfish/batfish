package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.RipNeighbor;

public class NodeRipSessionPair {

  private static final String PROP_NODE = "node";

  private static final long serialVersionUID = 1L;

  private static final String PROP_SESSION = "session";

  private final Configuration _first;

  private final RipNeighbor _second;

  @JsonCreator
  public NodeRipSessionPair(
      @JsonProperty(PROP_NODE) Configuration t1, @JsonProperty(PROP_SESSION) RipNeighbor t2) {
    _first = t1;
    _second = t2;
  }

  @JsonProperty(PROP_NODE)
  public Configuration getHost() {
    return _first;
  }

  @JsonProperty(PROP_SESSION)
  public RipNeighbor getSession() {
    return _second;
  }
}
