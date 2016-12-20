package org.batfish.datamodel.collections;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedStructureEquivalenceSets<T> {

   private SortedMap<String, SortedSet<NamedStructureEquivalenceSet<T>>> _sameNamedStructures;

   private String _structureClassName;

   @JsonCreator
   public NamedStructureEquivalenceSets() {
      this("");
   }

   public NamedStructureEquivalenceSets(String structureClassName) {
      _structureClassName = structureClassName;
      _sameNamedStructures = new TreeMap<>();
   }

   public void add(String node, String name, T namedStructure) {
      if (!_sameNamedStructures.containsKey(name)) {
         _sameNamedStructures.put(name,
               new TreeSet<NamedStructureEquivalenceSet<T>>());
      }
      SortedSet<NamedStructureEquivalenceSet<T>> equiClasses = _sameNamedStructures
            .get(name);

      for (NamedStructureEquivalenceSet<T> equiClass : equiClasses) {
         if (equiClass.compareStructure(namedStructure)) {
            equiClass.getNodes().add(node);
            return;
         }
      }
      equiClasses.add(new NamedStructureEquivalenceSet<>(node, namedStructure));
   }

   /**
    * Remove structures with only one equivalence class, since they indicate
    * nothing of note
    */
   public void clean() {
      Set<String> structureNames = new TreeSet<>(_sameNamedStructures.keySet());
      for (String structureName : structureNames) {
         if (_sameNamedStructures.get(structureName).size() == 1) {
            _sameNamedStructures.remove(structureName);
         }
      }
   }

   public SortedMap<String, SortedSet<NamedStructureEquivalenceSet<T>>> getSameNamedStructures() {
      return _sameNamedStructures;
   }

   public String getStructureClassName() {
      return _structureClassName;
   }

   public String prettyPrint(String indent) {
      StringBuilder sb = new StringBuilder();
      for (String name : _sameNamedStructures.keySet()) {
         sb.append(indent + name + "\n");
         for (NamedStructureEquivalenceSet<T> set : _sameNamedStructures
               .get(name)) {
            sb.append(set.prettyPrint(indent + indent));
         }
      }
      return sb.toString();
   }

   public void setSameNamedStructures(
         SortedMap<String, SortedSet<NamedStructureEquivalenceSet<T>>> sameNamedStructures) {
      _sameNamedStructures = sameNamedStructures;
   }

   public void setStructureClassName(String structureClassName) {
      _structureClassName = structureClassName;
   }

   public int size() {
      return _sameNamedStructures.size();
   }
}
