package org.batfish.datamodel.assertion;

public class StringIf implements StringExpr {

  private StringExpr _falseExpr;

  private BooleanExpr _guard;

  private StringExpr _trueExpr;

  public StringIf(BooleanExpr guard, StringExpr trueExpr, StringExpr falseExpr) {
    _guard = guard;
    _trueExpr = trueExpr;
    _falseExpr = falseExpr;
  }

  @Override
  public String evaluate(Environment env) {
    if (_guard.evaluate(env)) {
      return _trueExpr.evaluate(env);
    } else {
      return _falseExpr.evaluate(env);
    }
  }
}
