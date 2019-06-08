package org.batfish.specifier.parboiled;

interface NamedStructureAstNode extends AstNode {
  <T> T accept(NamedStructureAstNodeVisitor<T> visitor);
}
