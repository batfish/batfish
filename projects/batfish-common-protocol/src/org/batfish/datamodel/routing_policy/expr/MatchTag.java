package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchTag extends BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntComparator _cmp;

   private IntExpr _tag;

   @JsonCreator
   private MatchTag() {
   }

   public MatchTag(IntComparator cmp, IntExpr tag) {
      _cmp = cmp;
      _tag = tag;
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
      MatchTag other = (MatchTag) obj;
      if (_cmp != other._cmp) {
         return false;
      }
      if (_tag == null) {
         if (other._tag != null) {
            return false;
         }
      }
      else if (!_tag.equals(other._tag)) {
         return false;
      }
      return true;
   }

   @Override
   public Result evaluate(Environment environment) {
      int lhs;
      if (environment.getReadFromIntermediateBgpAttributes()) {
         lhs = environment.getIntermediateBgpAttributes().getTag();
      }
      else if (environment.getUseOutputAttributes()) {
         lhs = environment.getOutputRoute().getTag();
      }
      else {
         lhs = environment.getOriginalRoute().getTag();
      }
      int rhs = _tag.evaluate(environment);
      return _cmp.apply(lhs, rhs);
   }

   public IntComparator getCmp() {
      return _cmp;
   }

   public IntExpr getTag() {
      return _tag;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_cmp == null) ? 0 : _cmp.hashCode());
      result = prime * result + ((_tag == null) ? 0 : _tag.hashCode());
      return result;
   }

   public void setCmp(IntComparator cmp) {
      _cmp = cmp;
   }

   public void setTag(IntExpr tag) {
      _tag = tag;
   }

}
