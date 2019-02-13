package org.batfish.specifier.parboiled;

/** The base interface of all AST nodes. */
interface AstNode {
  <T> T accept(AstNodeVisitor<T> visitor);
}
