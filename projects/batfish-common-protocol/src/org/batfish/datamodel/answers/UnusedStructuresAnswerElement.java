package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UnusedStructuresAnswerElement implements AnswerElement {

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _unusedStructures;

   public UnusedStructuresAnswerElement() {
      _unusedStructures = new TreeMap<>();
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUnusedStructures() {
      return _unusedStructures;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   public void setUnusedStructures(
         SortedMap<String, SortedMap<String, SortedSet<String>>> undefinedReferences) {
      _unusedStructures = undefinedReferences;
   }
}
