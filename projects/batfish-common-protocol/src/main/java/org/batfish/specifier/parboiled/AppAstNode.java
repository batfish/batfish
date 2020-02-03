package org.batfish.specifier.parboiled;

interface AppAstNode extends AstNode {
  <T> T accept(AppAstNodeVisitor<T> visitor);
}
