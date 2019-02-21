package org.batfish.specifier.parboiled;

interface IpSpaceAstNodeVisitor<T> {
  T visitAddressGroupAstNode(AddressGroupIpSpaceAstNode addressGroupIpSpaceAstNode);

  T visitCommaIpSpaceAstNode(CommaIpSpaceAstNode commaIpSpaceAstNode);

  T visitIpAstNode(IpAstNode ipAstNode);

  T visitIpRangeAstNode(IpRangeAstNode rangeIpSpaceAstNode);

  T visitIpWildcardAstNode(IpWildcardAstNode ipWildcardAstNode);

  T visitPrefixAstNode(PrefixAstNode prefixAstNode);

  T visitLocationIpSpaceAstNode(LocationIpSpaceAstNode locationIpSpaceAstNode);
}
