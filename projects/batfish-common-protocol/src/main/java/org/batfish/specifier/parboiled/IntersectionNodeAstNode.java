package org.batfish.specifier.parboiled;

import java.util.Objects;

class IntersectionNodeAstNode extends SetOpNodeAstNode {

  IntersectionNodeAstNode(AstNode left, AstNode right) {
    _left = (NodeAstNode) left;
    _right = (NodeAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionNodeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntersectionNodeAstNode)) {
      return false;
    }
    IntersectionNodeAstNode that = (IntersectionNodeAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }
}
