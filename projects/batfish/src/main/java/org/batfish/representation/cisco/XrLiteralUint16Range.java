package org.batfish.representation.cisco;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;

@ParametersAreNonnullByDefault
public class XrLiteralUint16Range implements XrUint16RangeExpr {

  public XrLiteralUint16Range(SubRange range) {
    _range = range;
  }

  @Override
  public <T, U> T accept(XrUint16RangeExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralUint16Range(this, arg);
  }

  public @Nonnull SubRange getRange() {
    return _range;
  }

  private final @Nonnull SubRange _range;
}
