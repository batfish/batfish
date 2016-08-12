package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.TreeMap;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CompareSameNameAnswerElement implements AnswerElement {

   /**
    * Equivalence sets are keyed by classname
    */
   private SortedMap<String, NamedStructureEquivalenceSets<?>> _equivalenceSets;

   private final String EQUIVALENCE_SETS_MAP_VAR = "equivalenceSetsMap";

   public CompareSameNameAnswerElement() {
      _equivalenceSets = new TreeMap<String, NamedStructureEquivalenceSets<?>>();
   }

   public void add(String className, NamedStructureEquivalenceSets<?> sets) {
      _equivalenceSets.put(className, sets);
   }

   @JsonProperty(EQUIVALENCE_SETS_MAP_VAR)
   public SortedMap<String, NamedStructureEquivalenceSets<?>> getEquivalenceSets() {
      return _equivalenceSets;
   }

   @Override
   public String prettyPrint() throws JsonProcessingException {
      // TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }

   @JsonProperty(EQUIVALENCE_SETS_MAP_VAR)
   public void setEquivalenceSets(
         SortedMap<String, NamedStructureEquivalenceSets<?>> equivalenceSets) {
      _equivalenceSets = equivalenceSets;
   }
}
