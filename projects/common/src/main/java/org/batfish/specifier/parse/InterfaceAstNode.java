package org.batfish.specifier.parse;

interface InterfaceAstNode extends AstNode {
  <T> T accept(InterfaceAstNodeVisitor<T> visitor);
}
