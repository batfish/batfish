package org.batfish.datamodel.assertion;

public class BooleanIf implements BooleanExpr {

  private BooleanExpr _falseExpr;

  private BooleanExpr _guard;

  private BooleanExpr _trueExpr;

  public BooleanIf(BooleanExpr guard, BooleanExpr trueExpr, BooleanExpr falseExpr) {
    _guard = guard;
    _trueExpr = trueExpr;
    _falseExpr = falseExpr;
  }

  @Override
  public Boolean evaluate(Environment env) {
    if (_guard.evaluate(env)) {
      return _trueExpr.evaluate(env);
    } else {
      return _falseExpr.evaluate(env);
    }
  }
}
