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
   public RangeCommunitySetElemHalf() {
   }

   public RangeCommunitySetElemHalf(SubRange range) {
      _range = range;
   }

   public SubRange getRange() {
      return _range;
   }

   public void setRange(SubRange range) {
      _range = range;
   }

}
