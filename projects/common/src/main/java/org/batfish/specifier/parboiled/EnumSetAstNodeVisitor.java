package org.batfish.specifier.parboiled;

interface EnumSetAstNodeVisitor<T> {
  T visitUnionEnumSetAstNode(UnionEnumSetAstNode unionEnumSetAstNode);

  <T1> T visitValueEnumSetAstNode(ValueEnumSetAstNode<T1> valueEnumSetAstNode);

  T visitRegexEnumSetAstNode(RegexEnumSetAstNode regexEnumSetAstNode);

  T visitNotEnumSetAstNode(NotEnumSetAstNode notEnumSetAstNode);
}
