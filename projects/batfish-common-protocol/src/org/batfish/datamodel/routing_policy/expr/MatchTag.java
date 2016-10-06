package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

import com.fasterxml.jackson.annotation.JsonCreator;

public class MatchTag extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private IntComparator _cmp;

   private IntExpr _tag;

   @JsonCreator
   public MatchTag() {
   }

   public MatchTag(IntComparator cmp, IntExpr tag) {
      _cmp = cmp;
      _tag = tag;
   }

   @Override
   public Result evaluate(Environment environment) {
      int lhs = environment.getOriginalRoute().getTag();
      int rhs = _tag.evaluate(environment);
      return _cmp.apply(lhs, rhs);
   }

   public IntComparator getCmp() {
      return _cmp;
   }

   public IntExpr getTag() {
      return _tag;
   }

   public void setCmp(IntComparator cmp) {
      _cmp = cmp;
   }

   public void setTag(IntExpr tag) {
      _tag = tag;
   }

}
