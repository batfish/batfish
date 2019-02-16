package org.batfish.specifier.parboiled;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.specifier.InterfaceWithConnectedIpsSpecifier;
import org.batfish.specifier.NameInterfaceSpecifier;
import org.batfish.specifier.NameRegexInterfaceSpecifier;
import org.batfish.specifier.ReferenceInterfaceGroupInterfaceSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.TypesInterfaceSpecifier;
import org.batfish.specifier.VrfNameInterfaceSpecifier;
import org.batfish.specifier.ZoneNameInterfaceSpecifier;

@ParametersAreNonnullByDefault
final class InterfaceAstNodeToInterfaces implements InterfaceAstNodeVisitor<Set<Interface>> {
  private final SpecifierContext _ctxt;
  private final Set<String> _nodes;

  InterfaceAstNodeToInterfaces(Set<String> nodes, SpecifierContext ctxt) {
    _nodes = nodes;
    _ctxt = ctxt;
  }

  @Nonnull
  @Override
  public Set<Interface> visitConnectedToInterfaceAstNode(
      ConnectedToInterfaceAstNode connectedToInterfaceAstNode) {
    return new InterfaceWithConnectedIpsSpecifier(
            connectedToInterfaceAstNode
                .getIpSpaceAstNode()
                .accept(new IpSpaceAstNodeToIpSpace(_ctxt)))
        .resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitDifferenceInterfaceAstNode(
      DifferenceInterfaceAstNode differenceInterfaceAstNode) {
    return Sets.difference(
        differenceInterfaceAstNode.getLeft().accept(this),
        differenceInterfaceAstNode.getRight().accept(this));
  }

  @Nonnull
  @Override
  public Set<Interface> visitInterfaceGroupInterfaceAstNode(
      InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode) {
    return new ReferenceInterfaceGroupInterfaceSpecifier(
            interfaceGroupInterfaceAstNode.getInterfaceGroup(),
            interfaceGroupInterfaceAstNode.getReferenceBook())
        .resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitIntersectionInterfaceAstNode(
      IntersectionInterfaceAstNode intersectionInterfaceAstNode) {
    return Sets.intersection(
        intersectionInterfaceAstNode.getLeft().accept(this),
        intersectionInterfaceAstNode.getRight().accept(this));
  }

  @Nonnull
  @Override
  public Set<Interface> visitNameInterfaceNode(NameInterfaceAstNode nameInterfaceAstNode) {
    return new NameInterfaceSpecifier(nameInterfaceAstNode.getName()).resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitNameRegexInterfaceAstNode(
      NameRegexInterfaceAstNode nameRegexInterfaceAstNode) {
    return new NameRegexInterfaceSpecifier(nameRegexInterfaceAstNode.getPattern())
        .resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitTypeInterfaceNode(TypeInterfaceAstNode typeInterfaceAstNode) {
    return new TypesInterfaceSpecifier(ImmutableSet.of(typeInterfaceAstNode.getInterfaceType()))
        .resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitUnionInterfaceAstNode(UnionInterfaceAstNode unionInterfaceAstNode) {
    return Sets.union(
        unionInterfaceAstNode.getLeft().accept(this),
        unionInterfaceAstNode.getRight().accept(this));
  }

  @Nonnull
  @Override
  public Set<Interface> visitVrfInterfaceAstNode(VrfInterfaceAstNode vrfInterfaceAstNode) {
    return new VrfNameInterfaceSpecifier(vrfInterfaceAstNode.getVrfName()).resolve(_nodes, _ctxt);
  }

  @Nonnull
  @Override
  public Set<Interface> visitZoneInterfaceAstNode(ZoneInterfaceAstNode zoneInterfaceAstNode) {
    return new ZoneNameInterfaceSpecifier(zoneInterfaceAstNode.getZoneName())
        .resolve(_nodes, _ctxt);
  }
}
