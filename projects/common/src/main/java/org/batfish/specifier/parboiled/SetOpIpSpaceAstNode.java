package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

abstract class SetOpIpSpaceAstNode implements IpSpaceAstNode {
  private final IpSpaceAstNode _left;
  private final IpSpaceAstNode _right;

  static SetOpIpSpaceAstNode create(Character c, AstNode left, AstNode right) {
    IpSpaceAstNode leftSpec = (IpSpaceAstNode) left;
    IpSpaceAstNode rightSpec = (IpSpaceAstNode) right;
    switch (c) {
      case ',':
        return new UnionIpSpaceAstNode(leftSpec, rightSpec);
      case '\\':
        return new DifferenceIpSpaceAstNode(leftSpec, rightSpec);
      // intersection takes a different code path
      default:
        throw new IllegalStateException("Unknown set operation for IpSpace spec " + c);
    }
  }

  SetOpIpSpaceAstNode(IpSpaceAstNode left, IpSpaceAstNode right) {
    _left = left;
    _right = right;
  }

  public final IpSpaceAstNode getLeft() {
    return _left;
  }

  public final IpSpaceAstNode getRight() {
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
    SetOpIpSpaceAstNode that = (SetOpIpSpaceAstNode) o;
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
