package org.batfish.representation.cisco;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class XrLiteralUint16 implements XrUint16RangeExpr {

  public XrLiteralUint16(int value) {
    _value = value;
  }

  @Override
  public <T, U> T accept(XrUint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralUint16(this, arg);
  }

  public int getValue() {
    return _value;
  }

  private final int _value;
}
