package org.batfish.specifier.parboiled;

import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.specifier.ReferenceAddressGroupIpSpaceSpecifier;
import org.batfish.specifier.SpecifierContext;

final class IpSpaceAstNodeToIpSpace implements IpSpaceAstNodeVisitor<IpSpace> {
  private final SpecifierContext _ctxt;

  IpSpaceAstNodeToIpSpace(SpecifierContext ctxt) {
    _ctxt = ctxt;
  }

  @Override
  public IpSpace visitCommaIpSpaceAstNode(CommaIpSpaceAstNode commaIpSpaceAstNode) {
    return AclIpSpace.union(
        commaIpSpaceAstNode.getLeft().accept(this), commaIpSpaceAstNode.getRight().accept(this));
  }

  @Override
  public IpSpace visitIpAstNode(IpAstNode ipAstNode) {
    return ipAstNode.getIp().toIpSpace();
  }

  @Override
  public IpSpace visitIpRangeAstNode(IpRangeAstNode rangeIpSpaceAstNode) {
    return IpRange.range(rangeIpSpaceAstNode.getLow(), rangeIpSpaceAstNode.getHigh());
  }

  @Override
  public IpSpace visitIpWildcardAstNode(IpWildcardAstNode ipWildcardAstNode) {
    return ipWildcardAstNode.getIpWildcard().toIpSpace();
  }

  @Override
  public IpSpace visitPrefixAstNode(PrefixAstNode prefixAstNode) {
    return prefixAstNode.getPrefix().toIpSpace();
  }

  @Override
  public IpSpace visitAddressGroupAstNode(AddressGroupIpSpaceAstNode addressGroupIpSpaceAstNode) {
    return ReferenceAddressGroupIpSpaceSpecifier.computeIpSpace(
        addressGroupIpSpaceAstNode.getAddressGroup(),
        addressGroupIpSpaceAstNode.getAddressBook(),
        _ctxt);
  }
}
