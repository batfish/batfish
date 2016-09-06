package org.batfish.datamodel.routing_policy.expr;

import java.util.ArrayList;
import java.util.List;

import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class Disjunction extends AbstractBooleanExpr {

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private List<BooleanExpr> _disjuncts;

   public Disjunction() {
      _disjuncts = new ArrayList<>();
   }

   @Override
   public Result evaluate(Environment environment,
         AbstractRouteBuilder<?> outputRoute) {
      for (BooleanExpr disjunct : _disjuncts) {
         Result result = disjunct.evaluate(environment, outputRoute);
         if (result.getExit()) {
            return result;
         }
         else if (result.getBooleanValue()) {
            result.setBooleanValue(true);
            return result;
         }
      }
      Result result = new Result();
      result.setBooleanValue(false);
      return result;
   }

   public List<BooleanExpr> getDisjuncts() {
      return _disjuncts;
   }

   public void setDisjuncts(List<BooleanExpr> disjuncts) {
      _disjuncts = disjuncts;
   }

   @Override
   public BooleanExpr simplify() {
      List<BooleanExpr> simpleDisjuncts = new ArrayList<>();
      boolean atLeastOneTrue = false;
      boolean atLeastOneComplex = false;
      for (BooleanExpr disjunct : _disjuncts) {
         BooleanExpr simpleDisjunct = disjunct.simplify();
         if (simpleDisjunct.equals(BooleanExprs.True.toStaticBooleanExpr())) {
            atLeastOneTrue = true;
            if (!atLeastOneComplex) {
               return BooleanExprs.True.toStaticBooleanExpr();
            }
            else if (!atLeastOneTrue) {
               simpleDisjuncts.add(simpleDisjunct);
            }
         }
         else if (!simpleDisjunct
               .equals(BooleanExprs.False.toStaticBooleanExpr())) {
            atLeastOneComplex = true;
            simpleDisjuncts.add(simpleDisjunct);
         }
      }

      if (simpleDisjuncts.isEmpty()) {
         return BooleanExprs.False.toStaticBooleanExpr();
      }
      else if (simpleDisjuncts.size() == 1) {
         return simpleDisjuncts.get(0);
      }
      else {
         Disjunction simple = new Disjunction();
         simple.setDisjuncts(simpleDisjuncts);
         return simple;
      }
   }

}
