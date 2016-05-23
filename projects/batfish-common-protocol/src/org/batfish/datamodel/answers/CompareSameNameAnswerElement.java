package org.batfish.datamodel.answers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompareSameNameAnswerElement<T> implements AnswerElement {   
   Map<String, Set<NamedStructureEquivalenceClass<T>>> _sameNameInfo;
   
   public CompareSameNameAnswerElement() {
     _sameNameInfo = new HashMap<String, Set<NamedStructureEquivalenceClass<T>>>();
   }
  
   public Map<String, Set<NamedStructureEquivalenceClass<T>>> getSameNameInfo() {
      return _sameNameInfo;
   }
  
   public void add(String node, String name, T namedStructure) {
      Set<NamedStructureEquivalenceClass<T>> equiClasses;
      if (_sameNameInfo.containsKey(name)) {
         equiClasses = _sameNameInfo.get(name);
      }
      else {
         equiClasses = new HashSet<NamedStructureEquivalenceClass<T>>();
         _sameNameInfo.put(name, equiClasses);
      }
      
      boolean done = false;
      for(NamedStructureEquivalenceClass<T> equiClass: equiClasses) {
         if (namedStructure.equals(equiClass.getNamedStrcuture())) {
            equiClass.addNode(node);
            done = true;
            break;
         }
      }
      if (!done) {
         NamedStructureEquivalenceClass<T> equiClass = new NamedStructureEquivalenceClass<T>(node, name, namedStructure);
         equiClasses.add(equiClass);
      }
   }
}
