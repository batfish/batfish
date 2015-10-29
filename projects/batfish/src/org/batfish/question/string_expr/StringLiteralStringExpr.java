package org.batfish.question.string_expr;

import org.batfish.question.Environment;

public final class StringLiteralStringExpr extends BaseStringExpr {

   private String _value;

   public StringLiteralStringExpr(String value) {
      _value = value;
   }

   @Override
   public String evaluate(Environment environment) {
      return _value;
   }

}
