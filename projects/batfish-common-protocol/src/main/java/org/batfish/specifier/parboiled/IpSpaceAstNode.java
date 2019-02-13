package org.batfish.specifier.parboiled;

interface IpSpaceAstNode extends AstNode {
  <T> T accept(IpSpaceAstNodeVisitor<T> visitor);
}
