package org.batfish.specifier.parboiled.parser;

interface RoutingPolicyAstNodeVisitor<T> {
  T visitNameRoutingPolicyAstNode(NameRoutingPolicyAstNode nameRoutingPolicyAstNode);

  T visitNameRegexRoutingPolicyAstNode(NameRegexRoutingPolicyAstNode nameRegexRoutingPolicyAstNode);

  T visitUnionRoutingPolicyAstNode(UnionRoutingPolicyAstNode unionRoutingPolicyAstNode);

  T visitDifferenceRoutingPolicyAstNode(
      DifferenceRoutingPolicyAstNode differenceRoutingPolicyAstNode);

  T visitIntersectionRoutingPolicyAstNode(
      IntersectionRoutingPolicyAstNode intersectionRoutingPolicyAstNode);
}
