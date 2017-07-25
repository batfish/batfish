package org.batfish.datamodel.assertion;

public class StringLiteral implements StringExpr {

  private String _s;

  public StringLiteral(String s) {
    _s = s;
  }

  @Override
  public String evaluate(Environment env) {
    return _s;
  }
}
