package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionEnumSetAstNode implements EnumSetAstNode {
  private final EnumSetAstNode _left;
  private final EnumSetAstNode _right;

  UnionEnumSetAstNode(AstNode left, AstNode right) {
    _left = (EnumSetAstNode) left;
    _right = (EnumSetAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionEnumSetAstNode(this);
  }

  @Override
  public <T> T accept(EnumSetAstNodeVisitor<T> visitor) {
    return visitor.visitUnionEnumSetAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionEnumSetAstNode)) {
      return false;
    }
    UnionEnumSetAstNode that = (UnionEnumSetAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public EnumSetAstNode getLeft() {
    return _left;
  }

  public EnumSetAstNode getRight() {
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
