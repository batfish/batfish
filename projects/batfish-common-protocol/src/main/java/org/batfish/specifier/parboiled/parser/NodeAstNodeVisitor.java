package org.batfish.specifier.parboiled.parser;

interface NodeAstNodeVisitor<T> {
  T visitUnionNodeAstNode(UnionNodeAstNode unionNodeAstNode);

  T visitDifferenceNodeAstNode(DifferenceNodeAstNode differenceNodeAstNode);

  T visitIntersectionNodeAstNode(IntersectionNodeAstNode intersectionNodeAstNode);

  T visitRoleNodeAstNode(RoleNodeAstNode roleNodeAstNode);

  T visitNameNodeAstNode(NameNodeAstNode nameNodeAstNode);

  T visitNameRegexNodeAstNode(NameRegexNodeAstNode nameRegexNodeAstNode);

  T visitTypeNodeAstNode(TypeNodeAstNode typeNodeAstNode);
}
