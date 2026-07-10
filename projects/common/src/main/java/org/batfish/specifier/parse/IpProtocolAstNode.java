package org.batfish.specifier.parse;

interface IpProtocolAstNode extends AstNode {
  <T> T accept(IpProtocolAstNodeVisitor<T> visitor);
}
