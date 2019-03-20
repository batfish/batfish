package org.batfish.specifier.parboiled;

import com.google.common.base.MoreObjects;
import java.util.Objects;

final class UnionIpProtocolAstNode implements IpProtocolAstNode {
  private final IpProtocolAstNode _left;
  private final IpProtocolAstNode _right;

  UnionIpProtocolAstNode(AstNode left, AstNode right) {
    _left = (IpProtocolAstNode) left;
    _right = (IpProtocolAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpProtocolAstNode(this);
  }

  @Override
  public <T> T accept(IpProtocolAstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpProtocolAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnionIpProtocolAstNode)) {
      return false;
    }
    UnionIpProtocolAstNode that = (UnionIpProtocolAstNode) o;
    return Objects.equals(_left, that._left) && Objects.equals(_right, that._right);
  }

  public IpProtocolAstNode getLeft() {
    return _left;
  }

  public IpProtocolAstNode getRight() {
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
