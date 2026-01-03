package org.batfish.specifier.parboiled;

interface IpSpaceAstNodeVisitor<T> {
  T visitAddressGroupAstNode(AddressGroupIpSpaceAstNode addressGroupIpSpaceAstNode);

  T visitUnionIpSpaceAstNode(UnionIpSpaceAstNode unionIpSpaceAstNode);

  T visitIpAstNode(IpAstNode ipAstNode);

  T visitIpRangeAstNode(IpRangeAstNode rangeIpSpaceAstNode);

  T visitIpWildcardAstNode(IpWildcardAstNode ipWildcardAstNode);

  T visitPrefixAstNode(PrefixAstNode prefixAstNode);

  T visitLocationIpSpaceAstNode(LocationIpSpaceAstNode locationIpSpaceAstNode);

  T visitDifferenceIpSpaceAstNode(DifferenceIpSpaceAstNode differenceIpSpaceAstNode);

  T visitIntersectionIpSpaceAstNode(IntersectionIpSpaceAstNode intersectionIpSpaceAstNode);
}
