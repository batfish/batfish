package org.batfish.specifier.parboiled;

interface EnumSetAstNodeVisitor<T> {
  T visitUnionEnumSetAstNode(UnionEnumSetAstNode unionNamedStructureAstNode);

  T visitValueEnumSetAstNode(ValueEnumSetAstNode typeNamedStructureAstNode);

  T visitRegexEnumSetAstNode(RegexEnumSetAstNode typeRegexNamedStructureAstNode);
}
