package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralCommunitySetElemHalf implements CommunitySetElemHalfExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _value;

   @JsonCreator
   public LiteralCommunitySetElemHalf() {
   }

   public LiteralCommunitySetElemHalf(int value) {
      _value = value;
   }

   public int getValue() {
      return _value;
   }

   public void setValue(int value) {
      _value = value;
   }

}
