package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.SubRange;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RangeCommunitySetElemHalf implements CommunitySetElemHalfExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private SubRange _range;

   @JsonCreator
   private RangeCommunitySetElemHalf() {
   }

   public RangeCommunitySetElemHalf(SubRange range) {
      _range = range;
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
      RangeCommunitySetElemHalf other = (RangeCommunitySetElemHalf) obj;
      if (_range == null) {
         if (other._range != null) {
            return false;
         }
      }
      else if (!_range.equals(other._range)) {
         return false;
      }
      return true;
   }

   public SubRange getRange() {
      return _range;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_range == null) ? 0 : _range.hashCode());
      return result;
   }

   public void setRange(SubRange range) {
      _range = range;
   }

}
