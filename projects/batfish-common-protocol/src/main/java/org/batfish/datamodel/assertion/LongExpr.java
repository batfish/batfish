package org.batfish.datamodel.assertion;

public interface LongExpr extends ComparableExpr {

  @Override
  Long evaluate(Environment env);
}
