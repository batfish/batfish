package org.batfish.specifier.parboiled;

interface NamedStructureAstNodeVisitor<T> {
  T visitUnionNamedStructureAstNode(UnionNamedStructureAstNode unionNamedStructureAstNode);

  T visitTypeNamedStructureAstNode(TypeNamedStructureAstNode typeNamedStructureAstNode);

  T visitTypeRegexNamedStructureAstNode(
      TypeRegexNamedStructureAstNode typeRegexNamedStructureAstNode);
}
