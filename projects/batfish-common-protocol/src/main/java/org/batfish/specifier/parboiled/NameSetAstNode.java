package org.batfish.specifier.parboiled;

interface NameSetAstNode extends AstNode {
  <T> T accept(NameSetAstNodeVisitor<T> visitor);
}
