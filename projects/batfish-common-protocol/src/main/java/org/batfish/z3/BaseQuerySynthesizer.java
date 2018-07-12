package org.batfish.z3;

public abstract class BaseQuerySynthesizer implements QuerySynthesizer {

  private boolean _negate;

  @Override
  public boolean getNegate() {
    return _negate;
  }

  public void setNegate(boolean negate) {
    _negate = negate;
  }
}
