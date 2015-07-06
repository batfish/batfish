package org.batfish.question;

public class LiteralIntExpr extends BaseIntExpr {

   private final int _value;

   public LiteralIntExpr(int value) {
      _value = value;
   }

   @Override
   public int evaluate(Environment environment) {
      return _value;
   }

}
