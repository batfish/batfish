package org.batfish.datamodel.assertion;

public class DoubleLiteral implements DoubleExpr {

  private double _d;

  public DoubleLiteral(double d) {
    _d = d;
  }

  @Override
  public Double evaluate(Environment env) {
    return _d;
  }
}
