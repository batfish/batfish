package org.batfish.vendor.check_point_management.parsing.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An {@link AstNode} representing a 16-bit unsigned integer. */
@ParametersAreNonnullByDefault
public final class Uint16AstNode implements AstNode {

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Uint16AstNode)) {
      return false;
    }
    Uint16AstNode that = (Uint16AstNode) o;
    return _value == that._value;
  }

  @Override
  public int hashCode() {
    return _value;
  }

  public static Uint16AstNode of(String valueStr) {
    return of(Integer.parseInt(valueStr));
  }

  static Uint16AstNode of(int value) {
    checkArgument(0 <= value && value <= 0xFFFF, "Invalid unsigned 16-bit integer: %s", value);
    return new Uint16AstNode(value);
  }

  private Uint16AstNode(int value) {
    _value = value;
  }

  private final int _value;
}
