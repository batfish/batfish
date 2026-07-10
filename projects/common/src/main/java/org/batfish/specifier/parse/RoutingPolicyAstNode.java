package org.batfish.specifier.parse;

interface RoutingPolicyAstNode extends AstNode {
  <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor);
}
