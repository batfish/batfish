package org.batfish.specifier.parboiled;

interface EnumSetAstNode extends AstNode {
  <T> T accept(EnumSetAstNodeVisitor<T> visitor);
}
