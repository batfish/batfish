package org.batfish.representation;

import java.io.Serializable;

import org.batfish.collections.EdgeSet;

public class Topology implements Serializable {

   private static final long serialVersionUID = 1L;

   private EdgeSet _edges;

   public Topology(EdgeSet edges) {
      _edges = edges;
   }

   public EdgeSet getEdges() {
      return _edges;
   }

}
