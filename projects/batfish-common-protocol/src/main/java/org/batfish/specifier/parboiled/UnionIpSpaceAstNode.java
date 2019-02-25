package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionIpSpaceAstNode implements IpSpaceAstNode {
  private final IpSpaceAstNode _left;
  private final IpSpaceAstNode _right;

  UnionIpSpaceAstNode(AstNode left, AstNode right) {
    _left = (IpSpaceAstNode) left;
    _right = (IpSpaceAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpSpaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionIpSpaceAstNode)) {
      return false;
    }
    UnionIpSpaceAstNode that = (UnionIpSpaceAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public IpSpaceAstNode getLeft() {
    return _left;
  }

  public IpSpaceAstNode getRight() {
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
