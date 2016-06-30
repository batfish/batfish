package org.batfish.datamodel.answers;

import java.util.HashMap;
import java.util.Map;

import org.batfish.datamodel.collections.NamedStructureEquivalenceSets;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompareSameNameAnswerElement implements AnswerElement {

   private final String EQUIVALENCE_SETS_MAP_VAR = "equivalenceSetsMap";
   
   private Map<String, NamedStructureEquivalenceSets<?>> _equivalenceSets;

   public CompareSameNameAnswerElement() {
      _equivalenceSets = new HashMap<String, NamedStructureEquivalenceSets<?>>();
   }
   
   public void add(String key, NamedStructureEquivalenceSets<?> sets) {
      _equivalenceSets.put(key, sets);
   }
   
   @JsonProperty(EQUIVALENCE_SETS_MAP_VAR)
   public Map<String, NamedStructureEquivalenceSets<?>> getEquivalenceSets() {
      return _equivalenceSets;
   }
}
