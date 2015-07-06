package org.batfish.question;

public class IfExpr implements BooleanExpr {

   private BooleanExpr _antecedent;

   private BooleanExpr _consequent;

   public IfExpr(BooleanExpr antecedent, BooleanExpr consequent) {
      _antecedent = antecedent;
      _consequent = consequent;
   }

   @Override
   public boolean evaluate(Environment environment) {
      return !_antecedent.evaluate(environment)
            || _consequent.evaluate(environment);
   }

}
