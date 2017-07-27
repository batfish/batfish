package org.batfish.datamodel.assertion;

public class Not implements BooleanExpr {

  private BooleanExpr _expr;

  public Not(BooleanExpr expr) {
    _expr = expr;
  }

  @Override
  public Boolean evaluate(Environment env) {
    boolean val = _expr.evaluate(env);
    return !val;
  }
}
