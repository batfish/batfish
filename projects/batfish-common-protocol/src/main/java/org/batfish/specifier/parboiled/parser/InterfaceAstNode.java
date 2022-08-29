package org.batfish.specifier.parboiled.parser;

interface InterfaceAstNode extends AstNode {
  <T> T accept(InterfaceAstNodeVisitor<T> visitor);
}
