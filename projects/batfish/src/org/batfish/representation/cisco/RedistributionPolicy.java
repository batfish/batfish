package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.RoutingProtocol;

public abstract class RedistributionPolicy implements Serializable {

   private static final long serialVersionUID = 1L;

   protected final RoutingProtocol _destinationProtocol;
   protected final RoutingProtocol _sourceProtocol;
   protected final Map<String, Object> _specialAttributes;

   public RedistributionPolicy(RoutingProtocol sourceProtocol,
         RoutingProtocol destinationProtocol) {
      _sourceProtocol = sourceProtocol;
      _destinationProtocol = destinationProtocol;
      _specialAttributes = new TreeMap<String, Object>();
   }

   public RoutingProtocol getDestinationProtocol() {
      return _destinationProtocol;
   }

   public RoutingProtocol getSourceProtocol() {
      return _sourceProtocol;
   }

   public Map<String, Object> getSpecialAttributes() {
      return _specialAttributes;
   }

}
