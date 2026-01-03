package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionNameSetAstNode implements NameSetAstNode {
  private final NameSetAstNode _left;
  private final NameSetAstNode _right;

  UnionNameSetAstNode(AstNode left, AstNode right) {
    _left = (NameSetAstNode) left;
    _right = (NameSetAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionNameSetAstNode(this);
  }

  @Override
  public <T> T accept(NameSetAstNodeVisitor<T> visitor) {
    return visitor.visitUnionNameSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionNameSetAstNode)) {
      return false;
    }
    UnionNameSetAstNode that = (UnionNameSetAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public NameSetAstNode getLeft() {
    return _left;
  }

  public NameSetAstNode getRight() {
    return _right;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("left", _left)
        .add("right", _right)
        .toString();
  }
}
