package org.batfish.specifier.parse;

interface LocationAstNode extends AstNode {
  <T> T accept(LocationAstNodeVisitor<T> visitor);
}
