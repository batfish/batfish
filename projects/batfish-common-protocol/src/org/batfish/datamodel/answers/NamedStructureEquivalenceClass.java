package org.batfish.datamodel.answers;

import java.util.HashSet;
import java.util.Set;
import org.batfish.common.Util;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NamedStructureEquivalenceClass<T> {
   
   // Jackson cannot serialize generics correctly.
   @JsonIgnore
   private final T _namedStructure; 
   
   private final Set<String> _nodes;
   
   public NamedStructureEquivalenceClass(String node, T namedStructure) {
      _namedStructure = namedStructure;
      _nodes = new HashSet<String>();
      _nodes.add(node);
      addNode(node);
   }

   public void addNode(String node) {
      _nodes.add(node);
   }
   
   public boolean CompareStructure(T s)
   {
      return Util.checkEqual(_namedStructure, s);
     // return _namedStructure.equals(s);
   }
  
   public Set<String> getNodes() {
      return _nodes;
   }
}
