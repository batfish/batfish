package org.batfish.specifier.parboiled.parser;

interface NameSetAstNode extends AstNode {
  <T> T accept(NameSetAstNodeVisitor<T> visitor);
}
