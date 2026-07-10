package org.batfish.specifier.parse;

interface NodeAstNode extends AstNode {
  <T> T accept(NodeAstNodeVisitor<T> visitor);
}
