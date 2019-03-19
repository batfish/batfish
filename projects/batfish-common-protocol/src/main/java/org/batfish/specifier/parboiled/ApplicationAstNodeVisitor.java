package org.batfish.specifier.parboiled;

interface ApplicationAstNodeVisitor<T> {
  T visitUnionApplicationAstNode(UnionApplicationAstNode unionApplicationAstNode);

  T visitNameApplicationAstNode(NameApplicationAstNode nameApplicationAstNode);
}
