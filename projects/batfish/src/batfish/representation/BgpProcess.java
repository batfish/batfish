package batfish.representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class BgpProcess implements Serializable {

   private static final long serialVersionUID = 1L;

   private Map<String, BgpNeighbor> _bgpNeighbors;
   private Set<GeneratedRoute> _generatedRoutes;

   public BgpProcess() {
      _bgpNeighbors = new HashMap<String, BgpNeighbor>();
      _generatedRoutes = new HashSet<GeneratedRoute>();
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public Map<String, BgpNeighbor> getNeighbors() {
      return _bgpNeighbors;
   }

}
