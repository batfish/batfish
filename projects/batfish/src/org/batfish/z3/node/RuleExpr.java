package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class RuleExpr extends Statement implements ComplexExpr {

   private BooleanExpr _subExpression;
   private List<Expr> _subExpressions;

   public RuleExpr() {
      _subExpression = new IfExpr();
      init();
   }

   public RuleExpr(BooleanExpr subExpression) {
      _subExpression = subExpression;
      init();
   }

   public RuleExpr(BooleanExpr antecedent, BooleanExpr consequent) {
      _subExpression = new IfExpr(antecedent, consequent);
      init();
   }

   @Override
   public Set<String> getRelations() {
      return _subExpression.getRelations();
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   private void init() {
      _subExpressions = new ArrayList<Expr>();
      _subExpressions.add(new IdExpr("rule"));
      _subExpressions.add(_subExpression);
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public Statement simplify() {
      BooleanExpr newSubExpression = _subExpression.simplify();
      if (newSubExpression != _subExpression) {
         if (newSubExpression == TrueExpr.INSTANCE) {
            return new Comment("(vacuous rule)");
         }
         else if (newSubExpression == FalseExpr.INSTANCE) {
            throw new Error("Unsatisfiable!");
         }
         else {
            return new RuleExpr(newSubExpression);
         }
      }
      else {
         return this;
      }
   }

   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      return _subExpression.toBoolExpr(nodProgram);
   }

}
