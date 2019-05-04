package org.batfish.specifier.parboiled;

interface AstNodeVisitor<T> {
  T visitAddressGroupIpSpaceAstNode(AddressGroupIpSpaceAstNode addressGroupIpSpaceAstNode);

  T visitUnionIpSpaceAstNode(UnionIpSpaceAstNode unionIpSpaceAstNode);

  T visitIpAstNode(IpAstNode ipAstNode);

  T visitIpWildcardAstNode(IpWildcardAstNode ipWildcardAstNode);

  T visitPrefixAstNode(PrefixAstNode prefixAstNode);

  T visitIpRangeAstNode(IpRangeAstNode rangeIpSpaceAstNode);

  T visitStringAstNode(StringAstNode stringAstNode);

  T visitUnionInterfaceAstNode(UnionInterfaceAstNode unionInterfaceAstNode);

  T visitDifferenceInterfaceAstNode(DifferenceInterfaceAstNode differenceInterfaceAstNode);

  T visitConnectedToInterfaceAstNode(ConnectedToInterfaceAstNode connectedToInterfaceAstNode);

  T visitTypeInterfaceAstNode(TypeInterfaceAstNode typeInterfaceAstNode);

  T visitNameInterfaceAstNode(NameInterfaceAstNode nameInterfaceAstNode);

  T visitNameRegexInterfaceAstNode(NameRegexInterfaceAstNode nameRegexInterfaceAstNode);

  T visitVrfInterfaceAstNode(VrfInterfaceAstNode vrfInterfaceAstNode);

  T visitZoneInterfaceAstNode(ZoneInterfaceAstNode zoneInterfaceAstNode);

  T visitInterfaceGroupInterfaceAstNode(
      InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode);

  T visitIntersectionInterfaceAstNode(IntersectionInterfaceAstNode intersectionInterfaceAstNode);

  T visitUnionNodeAstNode(UnionNodeAstNode unionNodeAstNode);

  T visitDifferenceNodeAstNode(DifferenceNodeAstNode differenceNodeAstNode);

  T visitIntersectionNodeAstNode(IntersectionNodeAstNode intersectionNodeAstNode);

  T visitRoleNodeAstNode(RoleNodeAstNode roleNodeAstNode);

  T visitNameNodeAstNode(NameNodeAstNode nameNodeAstNode);

  T visitNameRegexNodeAstNode(NameRegexNodeAstNode nameRegexNodeAstNode);

  T visitTypeNodeAstNode(TypeNodeAstNode typeNodeAstNode);

  T visitUnionFilterAstNode(UnionFilterAstNode unionFilterAstNode);

  T visitDifferenceFilterAstNode(DifferenceFilterAstNode differenceFilterAstNode);

  T visitIntersectionFilterAstNode(IntersectionFilterAstNode intersectionFilterAstNode);

  T visitNameFilterAstNode(NameFilterAstNode nameFilterAstNode);

  T visitNameRegexFilterAstNode(NameRegexFilterAstNode nameRegexFilterAstNode);

  T visitInFilterAstNode(InFilterAstNode inFilterAstNode);

  T visitOutFilterAstNode(OutFilterAstNode outFilterAstNode);

  T visitUnionLocationAstNode(UnionLocationAstNode unionLocationAstNode);

  T visitDifferenceLocationAstNode(DifferenceLocationAstNode differenceLocationAstNode);

  T visitIntersectionLocationAstNode(IntersectionLocationAstNode intersectionLocationAstNode);

  T visitInterfaceLocationAstNode(InterfaceLocationAstNode interfaceLocationAstNode);

  T visitEnterLocationAstNode(EnterLocationAstNode enterLocationAstNode);

  T visitLocationIpSpaceAstNode(LocationIpSpaceAstNode locationIpSpaceAstNode);

  T visitInterfaceWithNodeInterfaceAstNode(
      InterfaceWithNodeInterfaceAstNode interfaceWithNodeInterfaceAstNode);

  T visitUnionApplicationAstNode(UnionApplicationAstNode unionApplicationAstNode);

  T visitNameApplicationAstNode(NameApplicationAstNode nameApplicationAstNode);

  T visitIpProtocolIpProtocolAstNode(IpProtocolIpProtocolAstNode ipProtocolIpProtocolAstNode);

  T visitUnionIpProtocolAstNode(UnionIpProtocolAstNode unionIpProtocolAstNode);

  T visitNotIpProtocolAstNode(NotIpProtocolAstNode notIpProtocolAstNode);

  T visitNameRoutingPolicyAstNode(NameRoutingPolicyAstNode nameRoutingPolicyAstNode);

  T visitNameRegexRoutingPolicyAstNode(NameRegexRoutingPolicyAstNode nameRegexRoutingPolicyAstNode);

  T visitUnionRoutingPolicyAstNode(UnionRoutingPolicyAstNode unionRoutingPolicyAstNode);

  T visitDifferenceRoutingPolicyAstNode(
      DifferenceRoutingPolicyAstNode differenceRoutingPolicyAstNode);

  T visitIntersectionRoutingPolicyAstNode(
      IntersectionRoutingPolicyAstNode intersectionRoutingPolicyAstNode);

  T visitDifferenceIpSpaceAstNode(DifferenceIpSpaceAstNode differenceIpSpaceAstNode);

  T visitIntersectionIpSpaceAstNode(IntersectionIpSpaceAstNode intersectionIpSpaceAstNode);

  T visitRegexAstNode(RegexAstNode regexAstNode);

  T visitOperatorAstNode(OperatorAstNode operatorAstNode);
}
