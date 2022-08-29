package org.batfish.specifier.parboiled.parser;

interface RoutingPolicyAstNode extends AstNode {
  <T> T accept(RoutingPolicyAstNodeVisitor<T> visitor);
}
