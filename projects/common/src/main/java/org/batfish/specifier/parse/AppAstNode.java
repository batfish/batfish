package org.batfish.specifier.parse;

interface AppAstNode extends AstNode {
  <T> T accept(AppAstNodeVisitor<T> visitor);
}
