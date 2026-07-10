package org.batfish.specifier.parse;

interface EnumSetAstNode extends AstNode {
  <T> T accept(EnumSetAstNodeVisitor<T> visitor);
}
