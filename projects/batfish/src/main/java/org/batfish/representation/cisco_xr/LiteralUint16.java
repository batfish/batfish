package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LiteralUint16 implements Uint16RangeExpr {

  public LiteralUint16(int value) {
    _value = value;
  }

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralUint16(this, arg);
  }

  public int getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralUint16)) {
      return false;
    }
    return _value == ((LiteralUint16) obj)._value;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(_value);
  }

  private final int _value;
}
