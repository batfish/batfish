package org.batfish.specifier.parboiled;

import java.util.Objects;

final class TypeInterfaceAstNode implements InterfaceAstNode {
  private final InterfaceTypeAstNode _interfaceTypeAstNode;

  TypeInterfaceAstNode(AstNode interfaceType) {
    _interfaceTypeAstNode = (InterfaceTypeAstNode) interfaceType;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitTypeInterfaceSpecAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitTypeInterfaceSpecAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TypeInterfaceAstNode that = (TypeInterfaceAstNode) o;
    return Objects.equals(_interfaceTypeAstNode, that._interfaceTypeAstNode);
  }

  public InterfaceTypeAstNode getInterfaceTypeAstNode() {
    return _interfaceTypeAstNode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interfaceTypeAstNode);
  }
}
