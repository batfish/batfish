package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapClauseMatchInterfaceLine extends PolicyMapMatchLine {

   private static final String NAME_VAR = "name";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final String _name;

   @JsonCreator
   public PolicyMapClauseMatchInterfaceLine(
         @JsonProperty(NAME_VAR) String name) {
      _name = name;
   }

   @JsonProperty(NAME_VAR)
   public String getName() {
      return _name;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.INTERFACE;
   }

}
