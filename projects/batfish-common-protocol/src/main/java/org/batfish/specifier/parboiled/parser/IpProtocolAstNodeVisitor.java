package org.batfish.specifier.parboiled.parser;

interface IpProtocolAstNodeVisitor<T> {
  T visitUnionIpProtocolAstNode(UnionIpProtocolAstNode unionIpProtocolAstNode);

  T visitNotIpProtocolAstNode(NotIpProtocolAstNode notIpProtocolAstNode);

  T visitIpProtocolIpProtocolAstNode(IpProtocolIpProtocolAstNode ipProtocolIpProtocolAstNode);
}
