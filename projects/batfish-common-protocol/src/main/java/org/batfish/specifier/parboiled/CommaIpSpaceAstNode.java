package org.batfish.specifier.parboiled;

import java.util.Objects;

final class CommaIpSpaceAstNode implements IpSpaceAstNode {
  private final IpSpaceAstNode _left;
  private final IpSpaceAstNode _right;

  CommaIpSpaceAstNode(AstNode left, AstNode right) {
    _left = (IpSpaceAstNode) left;
    _right = (IpSpaceAstNode) right;
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitCommaIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitCommaIpSpaceAstNode(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommaIpSpaceAstNode)) {
      return false;
    }
    CommaIpSpaceAstNode that = (CommaIpSpaceAstNode) o;
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
}
