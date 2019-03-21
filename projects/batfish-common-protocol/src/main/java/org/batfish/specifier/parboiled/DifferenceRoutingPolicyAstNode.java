package org.batfish.specifier.parboiled;

final class DifferenceRoutingPolicyAstNode extends SetOpRoutingPolicyAstNode {

  DifferenceRoutingPolicyAstNode(RoutingPolicyAstNode left, RoutingPolicyAstNode right) {
    super(left, right);
  }

  @Override
  public <T> T accept(AstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceRoutingPolicyAstNode(this);
  }

  @Override
  public <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor) {
    return visitor.visitDifferenceRoutingPolicyAstNode(this);
  }
}
