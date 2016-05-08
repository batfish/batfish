package org.batfish.representation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.batfish.common.datamodel.Ip;
import org.batfish.common.datamodel.Prefix;

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
    * is configured to peer, keyed by prefix
    */
   private Map<Prefix, BgpNeighbor> _bgpNeighbors;

   /**
    * The set of <i>neighbor-independent</i> generated routes that may be
    * advertised by this process if permitted by their respective generation
    * policies
    */
   private Set<GeneratedRoute> _generatedRoutes;

   private transient PrefixSpace _originationSpace;

   private Ip _routerId;

   /**
    * Constructs a BgpProcess
    */
   public BgpProcess() {
      _bgpNeighbors = new HashMap<Prefix, BgpNeighbor>();
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
   public Map<Prefix, BgpNeighbor> getNeighbors() {
      return _bgpNeighbors;
   }

   public PrefixSpace getOriginationSpace() {
      return _originationSpace;
   }

   public Ip getRouterId() {
      return _routerId;
   }

   public void setOriginationSpace(PrefixSpace originationSpace) {
      _originationSpace = originationSpace;
   }

   public void setRouterId(Ip routerId) {
      _routerId = routerId;
   }

}
