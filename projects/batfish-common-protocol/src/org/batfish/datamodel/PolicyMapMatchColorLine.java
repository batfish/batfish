package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapMatchColorLine extends PolicyMapMatchLine {

   private static final String COLOR_VAR = "color";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _color;

   @JsonCreator
   public PolicyMapMatchColorLine(@JsonProperty(COLOR_VAR) int color) {
      _color = color;
   }

   @JsonProperty(COLOR_VAR)
   public int getColor() {
      return _color;
   }

   @Override
   public PolicyMapMatchType getType() {
      return PolicyMapMatchType.COLOR;
   }

}
