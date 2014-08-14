package batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

public class AndExpr extends BooleanExpr implements ComplexExpr {

   private List<BooleanExpr> _conjuncts;
   private List<Expr> _subExpressions;

   public AndExpr() {
      init();
   }

   public AndExpr(List<BooleanExpr> conjuncts) {
      init();
      addConjuncts(conjuncts);
   }

   public void addConjunct(BooleanExpr conjunct) {
      _conjuncts.add(conjunct);
      _subExpressions.add(conjunct);
   }

   public void addConjuncts(List<BooleanExpr> conjuncts) {
      _conjuncts.addAll(conjuncts);
      _subExpressions.addAll(conjuncts);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   private void init() {
      _conjuncts = new ArrayList<BooleanExpr>();
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("and"));
      _printer = new ExpandedComplexExprPrinter(this);
   }

   @Override
   public BooleanExpr simplify() {
      boolean changed = false;
      List<BooleanExpr> newConjuncts = new ArrayList<BooleanExpr>();

      // first check for nested ANDs
      boolean containsAndExpr = false;
      for (BooleanExpr conjunct : _conjuncts) {
         if (conjunct instanceof AndExpr) {
            containsAndExpr = true;
         }
      }
      if (containsAndExpr) {
         for (BooleanExpr conjunct : _conjuncts) {
            if (conjunct instanceof AndExpr) {
               AndExpr nestedAndExpr = (AndExpr) conjunct;
               newConjuncts.addAll(nestedAndExpr._conjuncts);
            }
            else {
               newConjuncts.add(conjunct);
            }
         }
         return new AndExpr(newConjuncts).simplify();
      }

      // no nested ANDs, so just simplify all conjuncts
      for (BooleanExpr conjunct : _conjuncts) {
         BooleanExpr simplifiedConjunct = conjunct.simplify();
         if (conjunct != simplifiedConjunct) {
            changed = true;
         }
         if (simplifiedConjunct == FalseExpr.INSTANCE) {
            return FalseExpr.INSTANCE;
         }
         else if (simplifiedConjunct != TrueExpr.INSTANCE) {
            newConjuncts.add(simplifiedConjunct);
         }
         else {
            changed = true;
         }
      }
      if (newConjuncts.size() == 0) {
         return TrueExpr.INSTANCE;
      }
      else if (newConjuncts.size() == 1) {
         return newConjuncts.get(0);
      }
      else if (!changed) {
         return this;
      }
      else {
         return new AndExpr(newConjuncts);
      }
   }

}
