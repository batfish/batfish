package org.batfish.question;

public interface IntExpr extends PrintableExpr {

   int evaluate(Environment environment);

}
