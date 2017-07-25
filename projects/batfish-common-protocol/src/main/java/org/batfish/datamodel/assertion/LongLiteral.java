package org.batfish.datamodel.assertion;

public class LongLiteral implements LongExpr {

  private long _l;

  public LongLiteral(long l) {
    _l = l;
  }

  @Override
  public Long evaluate(Environment env) {
    return _l;
  }
}
