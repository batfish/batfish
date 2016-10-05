package org.batfish.datamodel.routing_policy.expr;

import java.io.Serializable;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CommunitySetElem implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private CommunitySetElemHalfExpr _prefix;

   private CommunitySetElemHalfExpr _suffix;

   @JsonCreator
   public CommunitySetElem() {
   }

   public CommunitySetElem(CommunitySetElemHalfExpr prefix,
         CommunitySetElemHalfExpr suffix) {
      _prefix = prefix;
      _suffix = suffix;
   }

   public CommunitySetElem(long value) {
      int prefixInt = (int) ((value & 0xFFFF0000l) >> 16);
      _prefix = new LiteralCommunitySetElemHalf(prefixInt);
      int suffixInt = (int) (value & 0xFFFFl);
      _suffix = new LiteralCommunitySetElemHalf(suffixInt);
   }

   public long community(Environment environment) {
      if (_prefix instanceof LiteralCommunitySetElemHalf
            && _suffix instanceof LiteralCommunitySetElemHalf) {
         LiteralCommunitySetElemHalf prefix = (LiteralCommunitySetElemHalf) _prefix;
         LiteralCommunitySetElemHalf suffix = (LiteralCommunitySetElemHalf) _suffix;
         int prefixInt = prefix.getValue();
         int suffixInt = suffix.getValue();
         return (((long) prefixInt) << 16) | (suffixInt);
      }
      else {
         throw new BatfishException("Does not represent a single community");
      }
   }

   public CommunitySetElemHalfExpr getPrefix() {
      return _prefix;
   }

   public CommunitySetElemHalfExpr getSuffix() {
      return _suffix;
   }

   public void setPrefix(CommunitySetElemHalfExpr prefix) {
      _prefix = prefix;
   }

   public void setSuffix(CommunitySetElemHalfExpr suffix) {
      _suffix = suffix;
   }

}
