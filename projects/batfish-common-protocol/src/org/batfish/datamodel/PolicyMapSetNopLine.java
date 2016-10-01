package org.batfish.datamodel;

public class PolicyMapSetNopLine extends PolicyMapSetLine {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   @Override
   public PolicyMapSetType getType() {
      return PolicyMapSetType.NOP;
   }

}
