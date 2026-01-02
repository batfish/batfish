package org.batfish.specifier.parboiled;

interface IpProtocolAstNodeVisitor<T> {
  T visitUnionIpProtocolAstNode(UnionIpProtocolAstNode unionIpProtocolAstNode);

  T visitNotIpProtocolAstNode(NotIpProtocolAstNode notIpProtocolAstNode);

  T visitIpProtocolIpProtocolAstNode(IpProtocolIpProtocolAstNode ipProtocolIpProtocolAstNode);
}
