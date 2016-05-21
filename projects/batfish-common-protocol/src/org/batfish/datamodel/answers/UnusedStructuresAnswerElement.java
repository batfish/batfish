package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.TreeMap;

public class UnusedStructuresAnswerElement implements AnswerElement {

   private Map<String, Map<Integer, String>> _unusedStructures;

   public UnusedStructuresAnswerElement() {
      _unusedStructures = new TreeMap<String, Map<Integer, String>>();
   }

   public void add(String hostname, String text) {
      Map<Integer, String> mapByHost = _unusedStructures.get(hostname);
      if (mapByHost == null) {
         mapByHost = new TreeMap<Integer, String>();
         _unusedStructures.put(hostname, mapByHost);
      }
      mapByHost.put(mapByHost.size() + 1, text);
   }

   public Map<String, Map<Integer, String>> getUnusedStructures() {
      return _unusedStructures;
   }

   public void setUnusedStructures(
         Map<String, Map<Integer, String>> undefinedReferences) {
      _unusedStructures = undefinedReferences;
   }

}
