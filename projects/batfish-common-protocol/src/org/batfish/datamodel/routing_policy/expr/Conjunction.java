package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

public class Conjunction implements BooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private List<BooleanExpr> _conjuncts;

   public Conjunction() {
      _conjuncts = new ArrayList<BooleanExpr>();
   }

   public List<BooleanExpr> getConjuncts() {
      return _conjuncts;
   }

   public BooleanExpr simplify() {
      if (_conjuncts.isEmpty()) {
         return BooleanExprs.True.toStaticBooleanExpr();
      }
      else if (_conjuncts.size() == 1) {
         return _conjuncts.get(0);
      }
      else {
         return this;
      }
   }

}
