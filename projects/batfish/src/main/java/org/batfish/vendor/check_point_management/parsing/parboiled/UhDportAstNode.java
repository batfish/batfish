package org.batfish.vendor.check_point_management.parsing.parboiled;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class UhDportAstNode extends BooleanExprAstNode {

  public UhDportAstNode(ComparatorAstNode comparator, Uint16AstNode value) {
    _comparator = comparator;
    _value = value;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof UhDportAstNode)) {
      return false;
    }
    UhDportAstNode that = (UhDportAstNode) o;
    return _comparator.equals(that._comparator) && _value.equals(that._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator, _value);
  }

  private final @Nonnull ComparatorAstNode _comparator;
  private final @Nonnull Uint16AstNode _value;
}
