package org.batfish.specifier.parse;

interface FilterAstNode extends AstNode {
  <T> T accept(FilterAstNodeVisitor<T> visitor);
}
