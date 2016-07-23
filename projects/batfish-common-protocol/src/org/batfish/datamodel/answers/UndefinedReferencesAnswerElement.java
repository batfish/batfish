package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setUndefinedReferences(
         SortedMap<String, SortedMap<Integer, String>> undefinedReferences) {
      _undefinedReferences = undefinedReferences;
   }

}
