package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.OspfNeighbor;

public class NodeOspfSessionPair extends Pair<Configuration, OspfNeighbor> {

   private static final String NODE_VAR = "node";

   private static final long serialVersionUID = 1L;

   private static final String SESSION_VAR = "session";

   @JsonCreator
   public NodeOspfSessionPair(
         @JsonProperty(NODE_VAR) Configuration t1,
         @JsonProperty(SESSION_VAR) OspfNeighbor t2) {
      super(t1, t2);
   }

   @JsonProperty(NODE_VAR)
   public Configuration getHost() {
      return _first;
   }

   @JsonProperty(SESSION_VAR)
   public OspfNeighbor getSession() {
      return _second;
   }

}
