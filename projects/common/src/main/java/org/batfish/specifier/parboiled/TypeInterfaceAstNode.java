package org.batfish.specifier.parboiled;

import java.util.Objects;
import org.batfish.datamodel.InterfaceType;

final class TypeInterfaceAstNode implements InterfaceAstNode {
  private final InterfaceType _interfaceType;

  TypeInterfaceAstNode(AstNode interfaceType) {
    this(((StringAstNode) interfaceType).getStr());
  }

  TypeInterfaceAstNode(String interfaceTypeStr) {
    _interfaceType = Enum.valueOf(InterfaceType.class, interfaceTypeStr.toUpperCase());
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitTypeInterfaceNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TypeInterfaceAstNode)) {
      return false;
    }
    TypeInterfaceAstNode that = (TypeInterfaceAstNode) o;
    return Objects.equals(_interfaceType, that._interfaceType);
  }

  public InterfaceType getInterfaceType() {
    return _interfaceType;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_interfaceType);
  }
}
