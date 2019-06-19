package org.batfish.specifier.parboiled;

final class UnionRoutingPolicyAstNode extends SetOpRoutingPolicyAstNode {

  UnionRoutingPolicyAstNode(RoutingPolicyAstNode left, RoutingPolicyAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitUnionRoutingPolicyAstNode(this);
  }

  @Override
  public <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor) {
    return visitor.visitUnionRoutingPolicyAstNode(this);
  }
}
