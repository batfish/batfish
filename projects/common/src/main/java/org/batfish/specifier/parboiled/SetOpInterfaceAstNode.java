package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpInterfaceAstNode implements InterfaceAstNode {
  private final InterfaceAstNode _left;
  private final InterfaceAstNode _right;

  static SetOpInterfaceAstNode create(Character c, AstNode left, AstNode right) {
    InterfaceAstNode leftSpec = (InterfaceAstNode) left;
    InterfaceAstNode rightSpec = (InterfaceAstNode) right;
    switch (c) {
      case ',':
        return new UnionInterfaceAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceInterfaceAstNode(leftSpec, rightSpec);
      // intersection takes a different code path
      default:
        throw new IllegalStateException("Unknown set operation for interface spec " + c);
    }
  }

  SetOpInterfaceAstNode(InterfaceAstNode left, InterfaceAstNode right) {
    _left = left;
    _right = right;
  }

  public final InterfaceAstNode getLeft() {
    return _left;
  }

  public final InterfaceAstNode getRight() {
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
    SetOpInterfaceAstNode that = (SetOpInterfaceAstNode) o;
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
