package org.batfish.specifier.parboiled.parser;

final class UnionIpSpaceAstNode extends SetOpIpSpaceAstNode {

  UnionIpSpaceAstNode(IpSpaceAstNode left, IpSpaceAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitUnionIpSpaceAstNode(this);
  }
}
