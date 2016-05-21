package org.batfish.datamodel.answers;

import java.util.HashSet;
import java.util.Set;

public class NamedStructureEquivalenceClass<T> {
   
   private final Set<String> _nodes;
   private final T _namedStructure;
   
   public NamedStructureEquivalenceClass(String node, T namedStructure) {
      _nodes = new HashSet<String>();
      _nodes.add(node);
      _namedStructure = namedStructure;
   }
  
   public Set<String> getNodes() {
      return _nodes;
   }
   
   public void addNode(String node) {
      _nodes.add(node);
   }
   
   public T getNamedStrcuture() {
      return _namedStructure;
   }

}
