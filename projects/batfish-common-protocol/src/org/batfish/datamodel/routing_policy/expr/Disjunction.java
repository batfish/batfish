package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

public class Disjunction extends AbstractBooleanExpr {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private List<BooleanExpr> _disjuncts;

   public Disjunction() {
      _disjuncts = new ArrayList<BooleanExpr>();
   }

   public List<BooleanExpr> getDisjuncts() {
      return _disjuncts;
   }

   public void setDisjuncts(List<BooleanExpr> disjuncts) {
      _disjuncts = disjuncts;
   }

   public BooleanExpr simplify() {
      if (_disjuncts.isEmpty()) {
         return BooleanExprs.False.toStaticBooleanExpr();
      }
      else if (_disjuncts.size() == 1) {
         return _disjuncts.get(0);
      }
      else {
         return this;
      }
   }

}
