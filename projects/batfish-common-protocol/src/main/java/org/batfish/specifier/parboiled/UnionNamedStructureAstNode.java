package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionNamedStructureAstNode implements NamedStructureAstNode {
  private final NamedStructureAstNode _left;
  private final NamedStructureAstNode _right;

  UnionNamedStructureAstNode(AstNode left, AstNode right) {
    _left = (NamedStructureAstNode) left;
    _right = (NamedStructureAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionNamedStructureAstNode(this);
  }

  @Override
  public <T> T accept(NamedStructureAstNodeVisitor<T> visitor) {
    return visitor.visitUnionNamedStructureAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionNamedStructureAstNode)) {
      return false;
    }
    UnionNamedStructureAstNode that = (UnionNamedStructureAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public NamedStructureAstNode getLeft() {
    return _left;
  }

  public NamedStructureAstNode getRight() {
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
