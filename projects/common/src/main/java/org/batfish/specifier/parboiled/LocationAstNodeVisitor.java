package org.batfish.specifier.parboiled;

interface LocationAstNodeVisitor<T> {
  T visitUnionLocationAstNode(UnionLocationAstNode unionLocationAstNode);

  T visitDifferenceLocationAstNode(DifferenceLocationAstNode differenceLocationAstNode);

  T visitIntersectionLocationAstNode(IntersectionLocationAstNode intersectionLocationAstNode);

  T visitInterfaceLocationAstNode(InterfaceLocationAstNode interfaceLocationAstNode);

  T visitInternetLocationAstNode();

  T visitEnterLocationAstNode(EnterLocationAstNode enterLocationAstNode);
}
