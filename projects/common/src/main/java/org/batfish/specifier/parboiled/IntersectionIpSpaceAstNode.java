package org.batfish.specifier.parboiled;

final class IntersectionIpSpaceAstNode extends SetOpIpSpaceAstNode {

  IntersectionIpSpaceAstNode(AstNode left, AstNode right) {
    super((IpSpaceAstNode) left, (IpSpaceAstNode) right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionIpSpaceAstNode(this);
  }

  @Override
  public <T> T accept(IpSpaceAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionIpSpaceAstNode(this);
  }
}
