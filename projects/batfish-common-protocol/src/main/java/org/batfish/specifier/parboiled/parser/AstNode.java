package org.batfish.specifier.parboiled.parser;

/** The base interface of all AST nodes. */
interface AstNode {
  <T> T accept(AstNodeVisitor<T> visitor);
}
