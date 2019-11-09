package org.batfish.representation.cisco_xr;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LiteralUint32 implements Uint32RangeExpr {

  public LiteralUint32(long value) {
    _value = value;
  }

  @Override
  public <T, U> T accept(Uint32RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralUint32(this, arg);
  }

  public long getValue() {
    return _value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralUint32)) {
      return false;
    }
    return _value == ((LiteralUint32) obj)._value;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_value);
  }

  private final long _value;
}
