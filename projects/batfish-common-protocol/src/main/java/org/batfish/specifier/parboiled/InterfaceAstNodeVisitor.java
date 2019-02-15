package org.batfish.specifier.parboiled;

interface InterfaceAstNodeVisitor<T> {
  T visitUnionInterfaceSpecAstNode(UnionInterfaceAstNode unionInterfaceSpecAstNode);

  T visitDifferenceInterfaceSpecAstNode(DifferenceInterfaceAstNode differenceInterfaceAstNode);

  T visitConnectedToInterfaceSpecAstNode(ConnectedToInterfaceAstNode connectedToInterfaceAstNode);

  T visitTypeInterfaceSpecAstNode(TypeInterfaceAstNode typeInterfaceSpecAstNode);

  T visitNameInterfaceSpecAstNode(NameInterfaceAstNode nameInterfaceSpecAstNode);

  T visitNameRegexInterfaceSpecAstNode(NameRegexInterfaceAstNode nameRegexInterfaceSpecAstNode);

  T visitVrfInterfaceSpecAstNode(VrfInterfaceAstNode vrfInterfaceSpecAstNode);

  T visitZoneInterfaceSpecAstNode(ZoneInterfaceAstNode zoneInterfaceSpecAstNode);

  T visitInterfaceGroupInterfaceSpecAstNode(
      InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode);

  T visitIntersectionInterfaceSpecAstNode(
      IntersectionInterfaceAstNode intersectionInterfaceSpecAstNode);
}
