package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.TreeMap;

public class UndefinedReferencesAnswerElement implements AnswerElement {

   private Map<String, Map<Integer, String>> _undefinedReferences;

   public UndefinedReferencesAnswerElement() {
      _undefinedReferences = new TreeMap<String, Map<Integer, String>>();
   }

   public void add(String hostname, String text) {
      Map<Integer, String> mapByHost = _undefinedReferences.get(hostname);
      if (mapByHost == null) {
         mapByHost = new TreeMap<Integer, String>();
         _undefinedReferences.put(hostname, mapByHost);
      }
      mapByHost.put(mapByHost.size() + 1, text);
   }

   public Map<String, Map<Integer, String>> getUndefinedReferences() {
      return _undefinedReferences;
   }

   public void setUndefinedReferences(
         Map<String, Map<Integer, String>> undefinedReferences) {
      _undefinedReferences = undefinedReferences;
   }

}
