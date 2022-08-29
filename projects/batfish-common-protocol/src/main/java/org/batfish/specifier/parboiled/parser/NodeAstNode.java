package org.batfish.specifier.parboiled.parser;

interface NodeAstNode extends AstNode {
  <T> T accept(NodeAstNodeVisitor<T> visitor);
}
