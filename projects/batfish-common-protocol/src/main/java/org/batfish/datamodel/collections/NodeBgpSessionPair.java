package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;

public class NodeBgpSessionPair extends Pair<Configuration, BgpNeighbor> {

   private static final String NODE_VAR = "node";

   private static final long serialVersionUID = 1L;

   private static final String SESSION_VAR = "session";

   @JsonCreator
   public NodeBgpSessionPair(@JsonProperty(NODE_VAR) Configuration t1,
         @JsonProperty(SESSION_VAR) BgpNeighbor t2) {
      super(t1, t2);
   }

   @JsonProperty(NODE_VAR)
   public Configuration getNode() {
      return _first;
   }

   @JsonProperty(SESSION_VAR)
   public BgpNeighbor getSession() {
      return _second;
   }

}
