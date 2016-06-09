package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

public class UnusedStructuresAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<Integer, String>> _unusedStructures;

   public UnusedStructuresAnswerElement() {
      _unusedStructures = new TreeMap<String, SortedMap<Integer, String>>();
   }

   public void add(String hostname, String text) {
      SortedMap<Integer, String> mapByHost = _unusedStructures.get(hostname);
      if (mapByHost == null) {
         mapByHost = new TreeMap<Integer, String>();
         _unusedStructures.put(hostname, mapByHost);
      }
      mapByHost.put(mapByHost.size() + 1, text);
   }

   public SortedMap<String, SortedMap<Integer, String>> getUnusedStructures() {
      return _unusedStructures;
   }

   public void setUnusedStructures(
         SortedMap<String, SortedMap<Integer, String>> undefinedReferences) {
      _unusedStructures = undefinedReferences;
   }

}
