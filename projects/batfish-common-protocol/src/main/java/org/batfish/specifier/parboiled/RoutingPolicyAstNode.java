package org.batfish.specifier.parboiled;

interface RoutingPolicyAstNode extends AstNode {
  <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor);
}
