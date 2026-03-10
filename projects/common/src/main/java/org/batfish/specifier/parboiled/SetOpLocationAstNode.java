package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpLocationAstNode implements LocationAstNode {
  private final LocationAstNode _left;
  private final LocationAstNode _right;

  static SetOpLocationAstNode create(Character c, AstNode left, AstNode right) {
    LocationAstNode leftSpec = (LocationAstNode) left;
    LocationAstNode rightSpec = (LocationAstNode) right;
    switch (c) {
      case ',':
        return new UnionLocationAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceLocationAstNode(leftSpec, rightSpec);
      // intersection takes a different code path
      default:
        throw new IllegalStateException("Unknown set operation for Location spec " + c);
    }
  }

  SetOpLocationAstNode(LocationAstNode left, LocationAstNode right) {
    _left = left;
    _right = right;
  }

  public final LocationAstNode getLeft() {
    return _left;
  }

  public final LocationAstNode getRight() {
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
    SetOpLocationAstNode that = (SetOpLocationAstNode) o;
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
