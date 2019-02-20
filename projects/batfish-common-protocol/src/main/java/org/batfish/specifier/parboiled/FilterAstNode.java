package org.batfish.specifier.parboiled;

interface FilterAstNode extends AstNode {
  <T> T accept(FilterAstNodeVisitor<T> visitor);
}
