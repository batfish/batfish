package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

@ParametersAreNonnullByDefault
public class LiteralUint16Range implements Uint16RangeExpr {

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

  private final @Nonnull SubRange _range;
}
