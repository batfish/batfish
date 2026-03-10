package org.batfish.specifier.parboiled;

final class DifferenceIpSpaceAstNode extends SetOpIpSpaceAstNode {

  DifferenceIpSpaceAstNode(IpSpaceAstNode left, IpSpaceAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceIpSpaceAstNode(this);
  }
}
