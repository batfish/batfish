package org.batfish.datamodel.assertion;

public class ComparableIf implements ComparableExpr {

  private ComparableExpr _falseExpr;

  private BooleanExpr _guard;

  private ComparableExpr _trueExpr;

  public ComparableIf(BooleanExpr guard, ComparableExpr trueExpr, ComparableExpr falseExpr) {
    _guard = guard;
    _trueExpr = trueExpr;
    _falseExpr = falseExpr;
  }

  @Override
  public Comparable<?> evaluate(Environment env) {
    if (_guard.evaluate(env)) {
      return _trueExpr.evaluate(env);
    } else {
      return _falseExpr.evaluate(env);
    }
  }
}
