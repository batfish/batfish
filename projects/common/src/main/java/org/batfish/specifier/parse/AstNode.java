package org.batfish.specifier.parse;

/** The base interface of all AST nodes. */
interface AstNode {
  <T> T accept(AstNodeVisitor<T> visitor);
}
