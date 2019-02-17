package org.batfish.specifier.parboiled;

import java.util.Objects;

class UnionNodeAstNode extends SetOpNodeAstNode {

  UnionNodeAstNode(NodeAstNode left, NodeAstNode right) {
    _left = left;
    _right = right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionNodeAstNode(this);
  }

  @Override
  public <T> T accept(NodeAstNodeVisitor<T> visitor) {
    return visitor.visitUnionNodeAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionNodeAstNode)) {
      return false;
    }
    UnionNodeAstNode that = (UnionNodeAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }
}
