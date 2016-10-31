package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.OriginType;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralOrigin implements OriginExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer _asNum;

   private OriginType _originType;

   @JsonCreator
   public LiteralOrigin() {
   }

   public LiteralOrigin(OriginType originType, Integer asNum) {
      _asNum = asNum;
   }

   public Integer getAsNum() {
      return _asNum;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   public void setAsNum(Integer asNum) {
      _asNum = asNum;
   }

   public void setOriginType(OriginType originType) {
      _originType = originType;
   }

}
