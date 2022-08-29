package org.batfish.specifier.parboiled.parser;

interface LocationAstNode extends AstNode {
  <T> T accept(LocationAstNodeVisitor<T> visitor);
}
