package org.batfish.datamodel.answers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class CompareSameNameAnswerElement<T> implements AnswerElement { 
   private final String _hrName;
   private final Map<String, Set<NamedStructureEquivalenceClass<T>>> _sameNamedStructures;
   
   public CompareSameNameAnswerElement(String hrName) {
      _hrName = hrName;
     _sameNamedStructures = new HashMap<String, Set<NamedStructureEquivalenceClass<T>>>();
   }

   public void add(String node, String name, T namedStructure) {
      if (!_sameNamedStructures.containsKey(name)) {
         _sameNamedStructures.put(name, new HashSet<NamedStructureEquivalenceClass<T>>());
      }
      Set<NamedStructureEquivalenceClass<T>> equiClasses = _sameNamedStructures.get(name);
      
      for(NamedStructureEquivalenceClass<T> equiClass: equiClasses) {
         if (equiClass.CompareStructure(namedStructure)) {
            equiClass.addNode(node);
            return;
         }
      }
      equiClasses.add(new NamedStructureEquivalenceClass<T>(node, namedStructure));
   }
   
   public String gethrName()
   {
      return _hrName;
   }
   
   public Map<String, Set<NamedStructureEquivalenceClass<T>>> getSameNamedStructures()
   {
      return _sameNamedStructures;
   }
}
