package org.batfish.specifier.parboiled;

final class IntersectionRoutingPolicyAstNode extends SetOpRoutingPolicyAstNode {

  IntersectionRoutingPolicyAstNode(AstNode left, AstNode right) {
    super((RoutingPolicyAstNode) left, (RoutingPolicyAstNode) right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionRoutingPolicyAstNode(this);
  }

  @Override
  public <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor) {
    return visitor.visitIntersectionRoutingPolicyAstNode(this);
  }
}
