package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IfExpr extends BooleanExpr implements ComplexExpr {

   private BooleanExpr _antecedent;
   private BooleanExpr _consequent;
   private List<Expr> _subExpressions;

   public IfExpr() {
      init();
   }

   public IfExpr(BooleanExpr antecedent, BooleanExpr consequent) {
      init();
      _antecedent = antecedent;
      _consequent = consequent;
      refreshSubExpressions();
   }

   @Override
   public Set<String> getRelations() {
      Set<String> relations = new HashSet<String>();
      relations.addAll(_antecedent.getRelations());
      relations.addAll(_consequent.getRelations());
      return relations;
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      Set<String> variables = new HashSet<String>();
      variables.addAll(_antecedent.getVariables());
      variables.addAll(_consequent.getVariables());
      return variables;
   }

   private void init() {
      _subExpressions = new ArrayList<Expr>();
      _printer = new ExpandedComplexExprPrinter(this);
   }

   private void refreshSubExpressions() {
      _subExpressions.clear();
      _subExpressions.add(new IdExpr("=>"));
      _subExpressions.add(_antecedent);
      _subExpressions.add(_consequent);
   }

   public void setAntecedent(BooleanExpr antecedent) {
      _antecedent = antecedent;
      refreshSubExpressions();
   }

   public void setConsequent(BooleanExpr consequent) {
      _consequent = consequent;
      refreshSubExpressions();
   }

   @Override
   public BooleanExpr simplify() {
      BooleanExpr newAntecedent = _antecedent.simplify();
      BooleanExpr newConsequent = _consequent.simplify();
      if (newAntecedent == FalseExpr.INSTANCE
            || newConsequent == TrueExpr.INSTANCE) {
         return TrueExpr.INSTANCE;
      }
      else if (newAntecedent == TrueExpr.INSTANCE) {
         if (newConsequent == FalseExpr.INSTANCE) {
            return FalseExpr.INSTANCE;
         }
         else {
            return newConsequent;
         }
      }
      else if (newAntecedent != _antecedent || newConsequent != _consequent) {
         return new IfExpr(newAntecedent, newConsequent);
      }
      else {
         return this;
      }
   }

}
