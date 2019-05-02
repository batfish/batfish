package org.batfish.specifier.parboiled;

interface NodeAstNode extends AstNode {
  <T> T accept(NodeAstNodeVisitor<T> visitor);
}
