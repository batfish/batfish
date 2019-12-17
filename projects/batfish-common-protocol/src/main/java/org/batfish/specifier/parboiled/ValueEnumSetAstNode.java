package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Collection;
import java.util.Objects;

final class ValueEnumSetAstNode<T> implements EnumSetAstNode {
  private final T _value;

  ValueEnumSetAstNode(String stringValue, Collection<T> allValues) {
    // find the value in the collection and map to that
    _value =
        allValues.stream()
            .filter(p -> p.toString().equalsIgnoreCase(stringValue))
            .findAny()
            .orElseThrow(
                () -> new IllegalArgumentException("Value not found in allValues " + stringValue));
  }

  public static <T> boolean isValidValue(String value, Collection<T> allValues) {
    return allValues.stream().anyMatch(p -> p.toString().equalsIgnoreCase(value));
  }

  @Override
  public <T1> T1 accept(AstNodeVisitor<T1> visitor) {
    return visitor.visitValueEnumSetAstNode(this);
  }

  @Override
  public <T1> T1 accept(EnumSetAstNodeVisitor<T1> visitor) {
    return visitor.visitValueEnumSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ValueEnumSetAstNode<?>)) {
      return false;
    }
    return Objects.equals(_value, ((ValueEnumSetAstNode) o)._value);
  }

  public T getValue() {
    return _value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("value", _value).toString();
  }
}
