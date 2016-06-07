package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyMapSetOriginTypeLine extends PolicyMapSetLine {

   private static final String ORIGIN_TYPE_VAR = "originType";

   private static final long serialVersionUID = 1L;

   private final OriginType _originType;

   @JsonCreator
   public PolicyMapSetOriginTypeLine(
         @JsonProperty(ORIGIN_TYPE_VAR) OriginType originType) {
      _originType = originType;
   }

   @JsonProperty(ORIGIN_TYPE_VAR)
   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.ORIGIN_TYPE;
   }

}
