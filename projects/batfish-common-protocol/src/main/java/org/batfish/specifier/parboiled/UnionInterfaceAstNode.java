package org.batfish.specifier.parboiled;

import java.util.Objects;

class UnionInterfaceAstNode extends SetOpInterfaceAstNode {

  UnionInterfaceAstNode(InterfaceAstNode left, InterfaceAstNode right) {
    _left = left;
    _right = right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionInterfaceSpecAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitUnionInterfaceSpecAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionInterfaceAstNode)) {
      return false;
    }
    UnionInterfaceAstNode that = (UnionInterfaceAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }
}
