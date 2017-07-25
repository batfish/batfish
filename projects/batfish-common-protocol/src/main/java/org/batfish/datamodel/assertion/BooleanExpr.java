package org.batfish.datamodel.assertion;

public interface BooleanExpr extends Expr {

  @Override
  Boolean evaluate(Environment env);
}
