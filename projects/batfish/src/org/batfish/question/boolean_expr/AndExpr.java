package org.batfish.question.boolean_expr;

import java.util.HashSet;
import java.util.Set;

import org.batfish.question.Environment;

public class AndExpr extends BaseBooleanExpr {

   private final Set<BooleanExpr> _conjuncts;

   public AndExpr(Set<BooleanExpr> conjuncts) {
      _conjuncts = new HashSet<BooleanExpr>();
      _conjuncts.addAll(conjuncts);
   }

   @Override
   public Boolean evaluate(Environment env) {
      for (BooleanExpr conjunct : _conjuncts) {
         if (!conjunct.evaluate(env)) {
            return false;
         }
      }
      return true;
   }

}
