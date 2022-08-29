package org.batfish.specifier.parboiled.parser;

interface EnumSetAstNode extends AstNode {
  <T> T accept(EnumSetAstNodeVisitor<T> visitor);
}
