package org.batfish.datamodel.collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NamedStructureEquivalenceSets<T> {

   private String _hrName;

   private Map<String, Set<NamedStructureEquivalenceSet<T>>> _sameNamedStructures;

   public NamedStructureEquivalenceSets() {
      this("");
   }

   public NamedStructureEquivalenceSets(String hrName) {
      _hrName = hrName;
      _sameNamedStructures = new HashMap<String, Set<NamedStructureEquivalenceSet<T>>>();
   }

   public void add(String node, String name, T namedStructure) {
      if (!_sameNamedStructures.containsKey(name)) {
         _sameNamedStructures.put(name,
               new HashSet<NamedStructureEquivalenceSet<T>>());
      }
      Set<NamedStructureEquivalenceSet<T>> equiClasses = _sameNamedStructures
            .get(name);

      for (NamedStructureEquivalenceSet<T> equiClass : equiClasses) {
         if (equiClass.compareStructure(namedStructure)) {
            equiClass.addNode(node);
            return;
         }
      }
      equiClasses
            .add(new NamedStructureEquivalenceSet<T>(node, namedStructure));
   }

   public String get_hrName() {
      return _hrName;
   }

   public Map<String, Set<NamedStructureEquivalenceSet<T>>> get_sameNamedStructures() {
      return _sameNamedStructures;
   }

   public void set_hrName(String _hrName) {
      this._hrName = _hrName;
   }

   public void set_sameNamedStructures(
         Map<String, Set<NamedStructureEquivalenceSet<T>>> _sameNamedStructures) {
      this._sameNamedStructures = _sameNamedStructures;
   }
}
