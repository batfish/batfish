package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionApplicationAstNode implements ApplicationAstNode {
  private final ApplicationAstNode _left;
  private final ApplicationAstNode _right;

  UnionApplicationAstNode(AstNode left, AstNode right) {
    _left = (ApplicationAstNode) left;
    _right = (ApplicationAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionApplicationAstNode(this);
  }

  @Override
  public <T> T accept(ApplicationAstNodeVisitor<T> visitor) {
    return visitor.visitUnionApplicationAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionApplicationAstNode)) {
      return false;
    }
    UnionApplicationAstNode that = (UnionApplicationAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public ApplicationAstNode getLeft() {
    return _left;
  }

  public ApplicationAstNode getRight() {
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
