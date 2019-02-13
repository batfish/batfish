package org.batfish.specifier.parboiled;

interface AstNodeVisitor<T> {
  T visitAddressGroupAstNode(AddressGroupAstNode addressGroupAstNode);

  T visitCommaIpSpaceAstNode(CommaIpSpaceAstNode commaIpSpaceAstNode);

  T visitIpAstNode(IpAstNode ipAstNode);

  T visitIpWildcardAstNode(IpWildcardAstNode ipWildcardAstNode);

  T visitPrefixAstNode(PrefixAstNode prefixAstNode);

  T visitIpRangeAstNode(IpRangeAstNode rangeIpSpaceAstNode);

  T visitStringAstNode(StringAstNode stringAstNode);
}
