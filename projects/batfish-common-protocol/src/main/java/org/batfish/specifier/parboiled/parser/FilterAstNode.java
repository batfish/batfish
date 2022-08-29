package org.batfish.specifier.parboiled.parser;

interface FilterAstNode extends AstNode {
  <T> T accept(FilterAstNodeVisitor<T> visitor);
}
