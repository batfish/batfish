package org.batfish.datamodel.assertion;

public interface ComparableExpr extends Expr {

  @Override
  Comparable<?> evaluate(Environment env);
}
