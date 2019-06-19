package org.batfish.specifier.parboiled;

interface InterfaceAstNodeVisitor<T> {
  T visitUnionInterfaceAstNode(UnionInterfaceAstNode unionInterfaceAstNode);

  T visitDifferenceInterfaceAstNode(DifferenceInterfaceAstNode differenceInterfaceAstNode);

  T visitConnectedToInterfaceAstNode(ConnectedToInterfaceAstNode connectedToInterfaceAstNode);

  T visitTypeInterfaceNode(TypeInterfaceAstNode typeInterfaceAstNode);

  T visitNameInterfaceNode(NameInterfaceAstNode nameInterfaceAstNode);

  T visitNameRegexInterfaceAstNode(NameRegexInterfaceAstNode nameRegexInterfaceAstNode);

  T visitVrfInterfaceAstNode(VrfInterfaceAstNode vrfInterfaceAstNode);

  T visitZoneInterfaceAstNode(ZoneInterfaceAstNode zoneInterfaceAstNode);

  T visitInterfaceGroupInterfaceAstNode(
      InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode);

  T visitIntersectionInterfaceAstNode(IntersectionInterfaceAstNode intersectionInterfaceAstNode);

  T visitInterfaceWithNodeInterfaceAstNode(
      InterfaceWithNodeInterfaceAstNode interfaceWithNodeInterfaceAstNode);
}
