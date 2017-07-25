package org.batfish.datamodel.assertion;

public interface DoubleExpr extends ComparableExpr {

  @Override
  Double evaluate(Environment env);
}
