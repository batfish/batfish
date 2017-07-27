package org.batfish.datamodel.assertion;

public class IntLiteral implements IntExpr {

  private int _i;

  public IntLiteral(int i) {
    _i = i;
  }

  @Override
  public Integer evaluate(Environment env) {
    return _i;
  }
}
