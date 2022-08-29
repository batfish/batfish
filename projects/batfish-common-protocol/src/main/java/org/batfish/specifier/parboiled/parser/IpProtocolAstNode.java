package org.batfish.specifier.parboiled.parser;

interface IpProtocolAstNode extends AstNode {
  <T> T accept(IpProtocolAstNodeVisitor<T> visitor);
}
