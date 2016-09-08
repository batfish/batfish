package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertConfigurationAnswerElement
      implements AnswerElement, Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _undefinedReferences;

   private SortedMap<String, SortedMap<String, SortedSet<String>>> _unusedStructures;

   private SortedMap<String, Warnings> _warnings;

   public ConvertConfigurationAnswerElement() {
      _warnings = new TreeMap<>();
      _undefinedReferences = new TreeMap<>();
      _unusedStructures = new TreeMap<>();
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUndefinedReferences() {
      return _undefinedReferences;
   }

   public SortedMap<String, SortedMap<String, SortedSet<String>>> getUnusedStructures() {
      return _unusedStructures;
   }

   public SortedMap<String, Warnings> getWarnings() {
      return _warnings;
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

   public void setUnusedStructures(
         SortedMap<String, SortedMap<String, SortedSet<String>>> unusedStructures) {
      _unusedStructures = unusedStructures;
   }

   public void setWarnings(SortedMap<String, Warnings> warnings) {
      _warnings = warnings;
   }
}
