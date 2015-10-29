package org.batfish.question;

public interface Expr {

   Object evaluate(Environment environment);

   String print(Environment environment);

}
