package org.batfish.question;

public final class StringLiteralStringExpr extends BaseStringExpr implements
      PrintableExpr {

   private String _value;

   public StringLiteralStringExpr(String value) {
      _value = value;
   }

   @Override
   public String evaluate(Environment environment) {
      return _value;
   }

}
