package batfish.representation;

import java.io.Serializable;
import java.util.List;

public class Topology implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<Edge> _edges;

   public Topology(List<Edge> edges) {
      _edges = edges;
   }

   public List<Edge> getEdges() {
      return _edges;
   }

}
