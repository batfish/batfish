package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class Conjunction extends AbstractBooleanExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<BooleanExpr> _conjuncts;

   public Conjunction() {
      _conjuncts = new ArrayList<BooleanExpr>();
   }

   @Override
   public Result evaluate(Environment environment, Route route) {
      for (BooleanExpr conjunct : _conjuncts) {
         Result result = conjunct.evaluate(environment, route);
         if (result.getExit()) {
            return result;
         }
         else if (!result.getBooleanValue()) {
            result.setBooleanValue(false);
            return result;
         }
      }
      Result result = new Result();
      result.setBooleanValue(true);
      return result;
   }

   public List<BooleanExpr> getConjuncts() {
      return _conjuncts;
   }

   public void setConjuncts(List<BooleanExpr> conjuncts) {
      _conjuncts = conjuncts;
   }

   @Override
   public BooleanExpr simplify() {
      List<BooleanExpr> simpleConjuncts = new ArrayList<BooleanExpr>();
      boolean atLeastOneFalse = false;
      boolean atLeastOneComplex = false;
      for (BooleanExpr conjunct : _conjuncts) {
         BooleanExpr simpleConjunct = conjunct.simplify();
         if (simpleConjunct.equals(BooleanExprs.False.toStaticBooleanExpr())) {
            atLeastOneFalse = true;
            if (!atLeastOneComplex) {
               return BooleanExprs.False.toStaticBooleanExpr();
            }
            else if (!atLeastOneFalse) {
               simpleConjuncts.add(simpleConjunct);
            }
         }
         else if (!simpleConjunct.equals(BooleanExprs.True
               .toStaticBooleanExpr())) {
            atLeastOneComplex = true;
            simpleConjuncts.add(simpleConjunct);
         }
      }
      if (simpleConjuncts.isEmpty()) {
         return BooleanExprs.True.toStaticBooleanExpr();
      }
      else if (simpleConjuncts.size() == 1) {
         return simpleConjuncts.get(0);
      }
      else {
         Conjunction simple = new Conjunction();
         simple.setConjuncts(simpleConjuncts);
         return simple;
      }
   }

}
