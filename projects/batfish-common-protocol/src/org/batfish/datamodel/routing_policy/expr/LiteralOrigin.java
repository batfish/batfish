package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class LiteralOrigin extends OriginExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Integer _asNum;

   private OriginType _originType;

   @JsonCreator
   private LiteralOrigin() {
   }

   public LiteralOrigin(OriginType originType, Integer asNum) {
      _asNum = asNum;
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
      LiteralOrigin other = (LiteralOrigin) obj;
      if (_asNum == null) {
         if (other._asNum != null) {
            return false;
         }
      }
      else if (!_asNum.equals(other._asNum)) {
         return false;
      }
      if (_originType != other._originType) {
         return false;
      }
      return true;
   }

   @Override
   public OriginType evaluate(Environment environment) {
      return _originType;
   }

   public Integer getAsNum() {
      return _asNum;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_asNum == null) ? 0 : _asNum.hashCode());
      result = prime * result
            + ((_originType == null) ? 0 : _originType.hashCode());
      return result;
   }

   public void setAsNum(Integer asNum) {
      _asNum = asNum;
   }

   public void setOriginType(OriginType originType) {
      _originType = originType;
   }

}
