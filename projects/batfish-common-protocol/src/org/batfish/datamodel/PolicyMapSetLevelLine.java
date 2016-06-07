package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetLevelLine extends PolicyMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String LEVEL_VAR = "level";

   private final IsisLevel _level;

   @JsonCreator
   public PolicyMapSetLevelLine(@JsonProperty(LEVEL_VAR) IsisLevel level) {
      _level = level;
   }

   @JsonProperty(LEVEL_VAR)
   public IsisLevel getLevel() {
      return _level;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.LEVEL;
   }

}
