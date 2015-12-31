package org.batfish.question.string_expr;

import org.batfish.question.Environment;

public final class StringConcatenateExpr extends BaseStringExpr {

   private final StringExpr _s1;

   private final StringExpr _s2;

   public StringConcatenateExpr(StringExpr s1, StringExpr s2) {
      _s1 = s1;
      _s2 = s2;
   }

   @Override
   public String evaluate(Environment environment) {
      String s1 = _s1.evaluate(environment);
      String s2 = _s2.evaluate(environment);
      return s1 + s2;
   }

}
