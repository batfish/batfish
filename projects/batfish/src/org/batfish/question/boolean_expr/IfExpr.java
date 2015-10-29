package org.batfish.question.boolean_expr;

import org.batfish.question.Environment;

public class IfExpr extends BaseBooleanExpr {

   private BooleanExpr _antecedent;

   private BooleanExpr _consequent;

   public IfExpr(BooleanExpr antecedent, BooleanExpr consequent) {
      _antecedent = antecedent;
      _consequent = consequent;
   }

   @Override
   public Boolean evaluate(Environment environment) {
      return !_antecedent.evaluate(environment)
            || _consequent.evaluate(environment);
   }

}
