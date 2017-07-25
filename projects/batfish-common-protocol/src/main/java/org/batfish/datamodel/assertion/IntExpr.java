package org.batfish.datamodel.assertion;

public interface IntExpr extends ComparableExpr {

  @Override
  Integer evaluate(Environment env);
}
