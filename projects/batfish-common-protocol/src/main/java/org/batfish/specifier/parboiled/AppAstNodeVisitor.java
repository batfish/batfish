package org.batfish.specifier.parboiled;

interface AppAstNodeVisitor<T> {
  T visitUnionAppAstNode(UnionAppAstNode unionAppAstNode);

  T visitIcmpAppAstNode(IcmpAppAstNode icmpAppAstNode);

  T visitNameAppAstNode(NameAppAstNode nameAppAstNode);

  T visitTcpAppAstNode(PortAppAstNode tcpAppAstNode);

  T visitUdpAppAstNode(UdpAppAstNode udpAppAstNode);
}
