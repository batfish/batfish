package org.batfish.specifier.parboiled;

interface ApplicationAstNode extends AstNode {
  <T> T accept(ApplicationAstNodeVisitor<T> visitor);
}
