package org.batfish.datamodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetAsPathPrependLine extends PolicyMapSetLine {

   private static final String LIST_VAR = "list";

   private static final long serialVersionUID = 1L;

   private final List<Integer> _asList;

   @JsonCreator
   public PolicyMapSetAsPathPrependLine(
         @JsonProperty(LIST_VAR) List<Integer> asList) {
      _asList = asList;
   }

   @JsonProperty(LIST_VAR)
   public List<Integer> getAsList() {
      return _asList;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.AS_PATH_PREPEND;
   }

}
