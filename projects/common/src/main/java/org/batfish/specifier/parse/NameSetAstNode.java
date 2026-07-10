package org.batfish.specifier.parse;

interface NameSetAstNode extends AstNode {
  <T> T accept(NameSetAstNodeVisitor<T> visitor);
}
