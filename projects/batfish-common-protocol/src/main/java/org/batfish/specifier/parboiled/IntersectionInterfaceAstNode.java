package org.batfish.specifier.parboiled;

import java.util.Objects;

class IntersectionInterfaceAstNode extends SetOpInterfaceAstNode {

  IntersectionInterfaceAstNode(AstNode left, AstNode right) {
    _left = (InterfaceAstNode) left;
    _right = (InterfaceAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionInterfaceAstNode(this);
  }

  @Override
  public <T> T accept(InterfaceAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionInterfaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntersectionInterfaceAstNode)) {
      return false;
    }
    IntersectionInterfaceAstNode that = (IntersectionInterfaceAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }
}
