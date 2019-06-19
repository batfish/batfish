package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpFilterAstNode implements FilterAstNode {
  private final FilterAstNode _left;
  private final FilterAstNode _right;

  static SetOpFilterAstNode create(Character c, AstNode left, AstNode right) {
    FilterAstNode leftSpec = (FilterAstNode) left;
    FilterAstNode rightSpec = (FilterAstNode) right;
    switch (c) {
      case ',':
        return new UnionFilterAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceFilterAstNode(leftSpec, rightSpec);
      default:
        throw new IllegalStateException("Unknown set operation for node spec " + c);
    }
  }

  SetOpFilterAstNode(FilterAstNode left, FilterAstNode right) {
    _left = left;
    _right = right;
  }

  public final FilterAstNode getLeft() {
    return _left;
  }

  public final FilterAstNode getRight() {
    return _right;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetOpFilterAstNode that = (SetOpFilterAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), _left, _right);
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("left", _left)
        .add("right", _right)
        .toString();
  }
}
