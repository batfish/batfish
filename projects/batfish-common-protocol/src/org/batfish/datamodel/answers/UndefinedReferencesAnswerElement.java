package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UndefinedReferencesAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _undefinedReferences;

   public UndefinedReferencesAnswerElement() {
      _undefinedReferences = new TreeMap<String, SortedMap<String, SortedSet<String>>>();
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUndefinedReferences() {
      return _undefinedReferences;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setUndefinedReferences(
         SortedMap<String, SortedMap<String, SortedSet<String>>> undefinedReferences) {
      _undefinedReferences = undefinedReferences;
   }

}
