package org.batfish.specifier.parboiled;

import java.util.Objects;

final class UnionAppAstNode implements AppAstNode {

  private final AppAstNode _left;

  private final AppAstNode _right;

  UnionAppAstNode(AstNode left, AstNode right) {
    _left = (AppAstNode) left;
    _right = (AppAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionAppAstNode(this);
  }

  @Override
  public <T> T accept(AppAstNodeVisitor<T> visitor) {
    return visitor.visitUnionAppAstNode(this);
  }

  public AppAstNode getLeft() {
    return _left;
  }

  public AppAstNode getRight() {
    return _right;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionAppAstNode)) {
      return false;
    }
    UnionAppAstNode that = (UnionAppAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_left, _right);
  }
}
