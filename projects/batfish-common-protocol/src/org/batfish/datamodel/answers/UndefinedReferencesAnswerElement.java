package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

public class UndefinedReferencesAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<Integer, String>> _undefinedReferences;

   public UndefinedReferencesAnswerElement() {
      _undefinedReferences = new TreeMap<String, SortedMap<Integer, String>>();
   }

   public void add(String hostname, String text) {
      SortedMap<Integer, String> mapByHost = _undefinedReferences.get(hostname);
      if (mapByHost == null) {
         mapByHost = new TreeMap<Integer, String>();
         _undefinedReferences.put(hostname, mapByHost);
      }
      mapByHost.put(mapByHost.size() + 1, text);
   }

   public SortedMap<String, SortedMap<Integer, String>> getUndefinedReferences() {
      return _undefinedReferences;
   }

   public void setUndefinedReferences(
         SortedMap<String, SortedMap<Integer, String>> undefinedReferences) {
      _undefinedReferences = undefinedReferences;
   }

}
