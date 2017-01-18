package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralCommunitySetElemHalf implements CommunitySetElemHalfExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private int _value;

   @JsonCreator
   private LiteralCommunitySetElemHalf() {
   }

   public LiteralCommunitySetElemHalf(int value) {
      _value = value;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      LiteralCommunitySetElemHalf other = (LiteralCommunitySetElemHalf) obj;
      if (_value != other._value) {
         return false;
      }
      return true;
   }

   public int getValue() {
      return _value;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _value;
      return result;
   }

   public void setValue(int value) {
      _value = value;
   }

}
