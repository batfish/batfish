package org.batfish.question;

public interface BooleanExpr extends PrintableExpr {

   public boolean evaluate(Environment environment);

}
