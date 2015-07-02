package org.batfish.question;

public class IfExpr implements BooleanExpr {

   private BooleanExpr _antecedent;

   private BooleanExpr _consequent;

   public IfExpr(BooleanExpr antecedent, BooleanExpr consequent) {
      _antecedent = antecedent;
      _consequent = consequent;
   }

   @Override
   public boolean evaluate(AssertionCtx context) {
      return !_antecedent.evaluate(context) || _consequent.evaluate(context);
   }

}
