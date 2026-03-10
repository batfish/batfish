package org.batfish.specifier.parboiled;

interface InterfaceAstNode extends AstNode {
  <T> T accept(InterfaceAstNodeVisitor<T> visitor);
}
