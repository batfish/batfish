package org.batfish.specifier.parse;

interface NameSetAstNodeVisitor<T> {
  T visitUnionNameSetAstNode(UnionNameSetAstNode unionNameSetAstNode);

  T visitRegexNameSetAstNode(RegexNameSetAstNode regexNameSetAstNode);

  T visitNameNameSetAstNode(SingletonNameSetAstNode singletonNameSetAstNode);
}
