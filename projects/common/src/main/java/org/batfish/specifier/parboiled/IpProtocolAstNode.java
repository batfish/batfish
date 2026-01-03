package org.batfish.specifier.parboiled;

interface IpProtocolAstNode extends AstNode {
  <T> T accept(IpProtocolAstNodeVisitor<T> visitor);
}
