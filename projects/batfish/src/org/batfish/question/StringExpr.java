package org.batfish.question;

public interface StringExpr extends PrintableExpr {

   String evaluate(Environment environment);

}
