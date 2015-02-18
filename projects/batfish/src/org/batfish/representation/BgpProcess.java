package org.batfish.representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents a bgp process on a router
 */
public class BgpProcess implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * A map of all the bgp neighbors with which the router owning this process
    * is configured to peer, keyed by ip address
    */
   private Map<Ip, BgpNeighbor> _bgpNeighbors;

   /**
    * The set of <i>neighbor-independent</i> generated routes that may be
    * advertised by this process if permitted by their respective generation
    * policies
    */
   private Set<GeneratedRoute> _generatedRoutes;

   /**
    * Constructs a BgpProcess
    */
   public BgpProcess() {
      _bgpNeighbors = new HashMap<Ip, BgpNeighbor>();
      _generatedRoutes = new HashSet<GeneratedRoute>();
   }

   /**
    * @return {@link #_generatedRoutes}
    */
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   /**
    * @return {@link #_bgpNeighbors}
    */
   public Map<Ip, BgpNeighbor> getNeighbors() {
      return _bgpNeighbors;
   }

}
