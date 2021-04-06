package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

@ParametersAreNonnullByDefault
public final class LiteralUint16Range implements Uint16RangeExpr {

  public LiteralUint16Range(SubRange range) {
    _range = range;
  }

  @Override
  public <T, U> T accept(Uint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralUint16Range(this, arg);
  }

  public @Nonnull SubRange getRange() {
    return _range;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LiteralUint16Range)) {
      return false;
    }
    LiteralUint16Range that = (LiteralUint16Range) o;
    return _range.equals(that._range);
  }

  @Override
  public int hashCode() {
    return _range.hashCode();
  }

  private final @Nonnull SubRange _range;
}
