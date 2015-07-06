package org.batfish.question;

import java.util.HashSet;
import java.util.Set;

public class AndExpr implements BooleanExpr {

   private final Set<BooleanExpr> _conjuncts;

   public AndExpr(Set<BooleanExpr> conjuncts) {
      _conjuncts = new HashSet<BooleanExpr>();
      _conjuncts.addAll(conjuncts);
   }

   @Override
   public boolean evaluate(Environment env) {
      for (BooleanExpr conjunct : _conjuncts) {
         if (!conjunct.evaluate(env)) {
            return false;
         }
      }
      return true;
   }

}
