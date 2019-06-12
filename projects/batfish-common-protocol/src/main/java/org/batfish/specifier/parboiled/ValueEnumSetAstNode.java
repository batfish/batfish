package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Collection;
import java.util.Objects;

final class ValueEnumSetAstNode implements EnumSetAstNode {
  private final String _value;

  ValueEnumSetAstNode(String value, Collection<String> allValues) {
    // canonicalize to the proper case
    _value =
        allValues.stream()
            .filter(p -> p.equalsIgnoreCase(value))
            .findAny()
            .orElseThrow(
                () -> new IllegalArgumentException("Value not found in allValues " + value));
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeNamedStructureAstNode(this);
  }

  @Override
  public <T> T accept(EnumSetAstNodeVisitor<T> visitor) {
    return visitor.visitValueEnumSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ValueEnumSetAstNode)) {
      return false;
    }
    ValueEnumSetAstNode that = (ValueEnumSetAstNode) o;
    return Objects.equals(_value, that._value);
  }

  public String getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("type", _value).toString();
  }
}
