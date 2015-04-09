package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class OrExpr extends BooleanExpr implements ComplexExpr {

   private List<BooleanExpr> _disjuncts;
   private List<Expr> _subExpressions;

   public OrExpr() {
      init();
   }

   public OrExpr(List<BooleanExpr> disjuncts) {
      init();
      addDisjuncts(disjuncts);
   }

   public void addDisjunct(BooleanExpr disjunct) {
      _disjuncts.add(disjunct);
      _subExpressions.add(disjunct);
   }

   public void addDisjuncts(List<BooleanExpr> disjuncts) {
      _disjuncts.addAll(disjuncts);
      _subExpressions.addAll(disjuncts);
   }

   public List<BooleanExpr> getDisjuncts() {
      return _disjuncts;
   }

   @Override
   public Set<String> getRelations() {
      Set<String> relations = new HashSet<String>();
      for (BooleanExpr disjunct : _disjuncts) {
         relations.addAll(disjunct.getRelations());
      }
      return relations;
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      Set<String> variables = new HashSet<String>();
      for (BooleanExpr disjunct : _disjuncts) {
         variables.addAll(disjunct.getVariables());
      }
      return variables;

   }

   public void init() {
      _printer = new ExpandedComplexExprPrinter(this);
      _disjuncts = new ArrayList<BooleanExpr>();
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("or"));
   }

   @Override
   public BooleanExpr simplify() {
      boolean changed = false;
      List<BooleanExpr> newDisjuncts = new ArrayList<BooleanExpr>();

      // first check for nested ORs
      boolean containsOrExpr = false;
      for (BooleanExpr disjunct : _disjuncts) {
         if (disjunct instanceof OrExpr) {
            containsOrExpr = true;
         }
      }
      if (containsOrExpr) {
         for (BooleanExpr disjunct : _disjuncts) {
            if (disjunct instanceof OrExpr) {
               OrExpr nestedOrExpr = (OrExpr) disjunct;
               newDisjuncts.addAll(nestedOrExpr._disjuncts);
            }
            else {
               newDisjuncts.add(disjunct);
            }
         }
         return new OrExpr(newDisjuncts).simplify();
      }

      // no nested ORs, so just simplify all disjuncts
      for (BooleanExpr disjunct : _disjuncts) {
         BooleanExpr simplifiedDisjunct = disjunct.simplify();
         if (disjunct != simplifiedDisjunct) {
            changed = true;
         }
         if (simplifiedDisjunct == TrueExpr.INSTANCE) {
            return TrueExpr.INSTANCE;
         }
         else if (simplifiedDisjunct != FalseExpr.INSTANCE) {
            newDisjuncts.add(simplifiedDisjunct);
         }
         else {
            changed = true;
         }
      }
      if (newDisjuncts.size() == 0) {
         return FalseExpr.INSTANCE;
      }
      else if (newDisjuncts.size() == 1) {
         return newDisjuncts.get(0);
      }
      else if (!changed) {
         return this;
      }
      else {
         return new OrExpr(newDisjuncts);
      }
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      Context ctx = nodProgram.getContext();
      List<BoolExpr> args = new ArrayList<BoolExpr>();
      for (BooleanExpr disjunct : _disjuncts) {
         BoolExpr be = disjunct.toBoolExpr(nodProgram);
         args.add(be);
      }
      BoolExpr result = ctx.mkOr(args.toArray(new BoolExpr[] {}));
      return result;
   }

}
