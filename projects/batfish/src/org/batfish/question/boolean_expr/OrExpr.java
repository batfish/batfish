package org.batfish.question.boolean_expr;

import java.util.HashSet;
import java.util.Set;

import org.batfish.question.Environment;

public class OrExpr extends BaseBooleanExpr {

   private final Set<BooleanExpr> _disjuncts;

   public OrExpr(Set<BooleanExpr> disjuncts) {
      _disjuncts = new HashSet<BooleanExpr>();
      _disjuncts.addAll(disjuncts);
   }

   @Override
   public Boolean evaluate(Environment environment) {
      for (BooleanExpr disjunct : _disjuncts) {
         if (disjunct.evaluate(environment)) {
            return true;
         }
      }
      return false;
   }

}
