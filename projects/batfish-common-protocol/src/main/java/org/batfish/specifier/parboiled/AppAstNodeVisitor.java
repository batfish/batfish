package org.batfish.specifier.parboiled;

interface AppAstNodeVisitor<T> {
  T visitUnionAppAstNode(UnionAppAstNode unionAppAstNode);

  T visitIcmpAllAppAstNode(IcmpAllAppAstNode icmpAllAppAstNode);

  T visitNameAppAstNode(NameAppAstNode nameAppAstNode);

  T visitTcpAppAstNode(TcpAppAstNode tcpAppAstNode);

  T visitUdpAppAstNode(UdpAppAstNode udpAppAstNode);

  T visitIcmpTypeAppAstNode(IcmpTypeAppAstNode icmpTypeAppAstNode);

  T visitIcmpTypeCodeAppAstNode(IcmpTypeCodeAppAstNode icmpTypeCodeAppAstNode);

  T visitRegexAppAstNode(RegexAppAstNode regexAppAstNode);
}
