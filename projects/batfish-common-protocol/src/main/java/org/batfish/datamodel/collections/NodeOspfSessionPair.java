package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OspfNeighbor;

public class NodeOspfSessionPair extends Pair<Configuration, OspfNeighbor> {

  private static final String PROP_NODE = "node";

  private static final long serialVersionUID = 1L;

  private static final String PROP_SESSION = "session";

  @JsonCreator
  public NodeOspfSessionPair(
      @JsonProperty(PROP_NODE) Configuration t1, @JsonProperty(PROP_SESSION) OspfNeighbor t2) {
    super(t1, t2);
  }

  @JsonProperty(PROP_NODE)
  public Configuration getHost() {
    return _first;
  }

  @JsonProperty(PROP_SESSION)
  public OspfNeighbor getSession() {
    return _second;
  }
}
