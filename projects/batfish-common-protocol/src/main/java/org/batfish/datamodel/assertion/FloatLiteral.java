package org.batfish.datamodel.assertion;

public class FloatLiteral implements FloatExpr {

  private float _f;

  public FloatLiteral(float f) {
    _f = f;
  }

  @Override
  public Float evaluate(Environment env) {
    return _f;
  }
}
