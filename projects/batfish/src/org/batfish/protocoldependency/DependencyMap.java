package org.batfish.protocoldependency;

import java.util.Map;
import java.util.TreeMap;

import org.batfish.representation.Configuration;

public class DependencyMap {

   private final Map<String, DependentNode> _dependentNodes;

   public DependencyMap(Map<String, Configuration> configurations) {
      _dependentNodes = new TreeMap<String, DependentNode>();
      for (Configuration c : configurations.values()) {
         String name = c.getHostname();
         DependentNode dependentNode = new DependentNode(name);
         _dependentNodes.put(name, dependentNode);
      }
   }

   public Map<String, DependentNode> getDependentNodes() {
      return _dependentNodes;
   }

}
