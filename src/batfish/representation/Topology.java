package batfish.representation;

import java.util.List;

public class Topology {
   private List<Edge> _edges;

   public Topology(List<Edge> edges) {
      _edges = edges;
   }

   public List<Edge> getEdges() {
      return _edges;
   }

}
