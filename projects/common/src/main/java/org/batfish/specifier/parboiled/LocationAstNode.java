package org.batfish.specifier.parboiled;

interface LocationAstNode extends AstNode {
  <T> T accept(LocationAstNodeVisitor<T> visitor);
}
