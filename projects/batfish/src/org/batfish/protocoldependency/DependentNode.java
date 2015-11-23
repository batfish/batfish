package org.batfish.protocoldependency;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.RoutingProtocol;

public class DependentNode {

   private final Map<RoutingProtocol, DependentProtocol> _dependentProtocols;

   private final String _name;

   public DependentNode(String name) {
      _name = name;
      _dependentProtocols = new TreeMap<RoutingProtocol, DependentProtocol>();
   }

   public String getName() {
      return _name;
   }

   public Map<RoutingProtocol, DependentProtocol> getProtocols() {
      return _dependentProtocols;
   }

}
