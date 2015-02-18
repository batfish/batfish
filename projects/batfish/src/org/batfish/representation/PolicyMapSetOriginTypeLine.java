package org.batfish.representation;

public class PolicyMapSetOriginTypeLine extends PolicyMapSetLine {

   private static final long serialVersionUID = 1L;
   private OriginType _originType;

   public PolicyMapSetOriginTypeLine(OriginType originType) {
      _originType = originType;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.ORIGIN_TYPE;
   }

}
