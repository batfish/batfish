package org.batfish.representation;

public class PolicyMapSetLevelLine extends PolicyMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IsisLevel _level;

   public PolicyMapSetLevelLine(IsisLevel level) {
      _level = level;
   }

   public IsisLevel getLevel() {
      return _level;
   }

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.LEVEL;
   }

}
