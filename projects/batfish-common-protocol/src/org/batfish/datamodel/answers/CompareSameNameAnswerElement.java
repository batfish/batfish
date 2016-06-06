package org.batfish.datamodel.answers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CompareSameNameAnswerElement<T> implements AnswerElement {
   
   private String _hrName;
   private Map<String, Set<NamedStructureEquivalenceClass<T>>> _sameNamedStructures;

   public CompareSameNameAnswerElement() {
      _hrName = "";
      _sameNamedStructures = new HashMap<String, Set<NamedStructureEquivalenceClass<T>>>();
   }
   
   public CompareSameNameAnswerElement(String hrName) {
      _hrName = hrName;
      _sameNamedStructures = new HashMap<String, Set<NamedStructureEquivalenceClass<T>>>();
   }

   public void add(String node, String name, T namedStructure) {
      if (!_sameNamedStructures.containsKey(name)) {
         _sameNamedStructures.put(name,
               new HashSet<NamedStructureEquivalenceClass<T>>());
      }
      Set<NamedStructureEquivalenceClass<T>> equiClasses = _sameNamedStructures
            .get(name);

      for (NamedStructureEquivalenceClass<T> equiClass : equiClasses) {
         if (equiClass.compareStructure(namedStructure)) {
            equiClass.addNode(node);
            return;
         }
      }
      equiClasses.add(new NamedStructureEquivalenceClass<T>(node,
            namedStructure));
   }
   
   public void set_hrName(String _hrName) {
      this._hrName = _hrName;
   }

   public String get_hrName() {
      return _hrName;
   }
   
   public void set_sameNamedStructures(
         Map<String, Set<NamedStructureEquivalenceClass<T>>> _sameNamedStructures) {
      this._sameNamedStructures = _sameNamedStructures;
   }

   public Map<String, Set<NamedStructureEquivalenceClass<T>>> get_sameNamedStructures() {
      return _sameNamedStructures;
   }

   

}
