package org.batfish.specifier.parboiled.parser;

interface AppAstNode extends AstNode {
  <T> T accept(AppAstNodeVisitor<T> visitor);
}
