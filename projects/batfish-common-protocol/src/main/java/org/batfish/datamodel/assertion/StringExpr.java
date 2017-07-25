package org.batfish.datamodel.assertion;

public interface StringExpr extends ComparableExpr {

  @Override
  String evaluate(Environment env);
}
