package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.InterfaceType;

final class InterfaceTypeAstNode implements AstNode {
  private final InterfaceType _type;

  InterfaceTypeAstNode(InterfaceType type) {
    _type = type;
  }

  InterfaceTypeAstNode(String type) {
    _type = Enum.valueOf(InterfaceType.class, type.toUpperCase());
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitInterfaceTypeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterfaceTypeAstNode that = (InterfaceTypeAstNode) o;
    return Objects.equals(_type, that._type);
  }

  public InterfaceType getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type);
  }
}
