package org.batfish.datamodel.assertion;

public interface FloatExpr extends ComparableExpr {

  @Override
  Float evaluate(Environment env);
}
