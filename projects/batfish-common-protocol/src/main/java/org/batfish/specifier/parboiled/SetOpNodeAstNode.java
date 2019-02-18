package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpNodeAstNode implements NodeAstNode {
  protected NodeAstNode _left;
  protected NodeAstNode _right;

  static SetOpNodeAstNode create(Character c, AstNode left, AstNode right) {
    NodeAstNode leftSpec = (NodeAstNode) left;
    NodeAstNode rightSpec = (NodeAstNode) right;
    switch (c) {
      case '+':
        return new UnionNodeAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceNodeAstNode(leftSpec, rightSpec);
      default:
        throw new IllegalStateException("Unknown set operation for node spec " + c);
    }
  }

  public NodeAstNode getLeft() {
    return _left;
  }

  public NodeAstNode getRight() {
    return _right;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetOpNodeAstNode that = (SetOpNodeAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), _left, _right);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("left", _left)
        .add("right", _right)
        .toString();
  }
}
